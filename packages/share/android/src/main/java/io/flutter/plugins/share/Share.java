// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.share;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugins.share.util.FileUtil;

/**
 * Handles share intent.
 */
class Share {

    private Context context;
    private Activity activity;
    private static CallbackManager callbackManager;
    
    /**
     * Constructs a Share object. The {@code context} and {@code activity} are used to start the share
     * intent. The {@code activity} might be null when constructing the {@link Share} object and set
     * to non-null when an activity is available using {@link #setActivity(Activity)}.
     */
    Share(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
        this.callbackManager = CallbackManager.Factory.create();
    }

    /**
     * Sets the activity when an activity is available. When the activity becomes unavailable, use
     * this method to set it to null.
     */
    void setActivity(Activity activity) {
        this.activity = activity;
    }

    void share(String text, String subject) {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Non-empty text expected");
        }

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        shareIntent.setType("text/plain");
        Intent chooserIntent = Intent.createChooser(shareIntent, null /* dialog title optional */);
        startActivity(chooserIntent);
    }

    void shareFiles(List<String> paths, List<String> mimeTypes, String text, String subject)
            throws IOException {
        if (paths == null || paths.isEmpty()) {
            throw new IllegalArgumentException("Non-empty path expected");
        }

        clearExternalShareFolder();
        ArrayList<Uri> fileUris = getUrisForPaths(paths);

        Intent shareIntent = new Intent();
        if (fileUris.isEmpty()) {
            share(text, subject);
            return;
        } else if (fileUris.size() == 1) {
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUris.get(0));
            shareIntent.setType(
                    !mimeTypes.isEmpty() && mimeTypes.get(0) != null ? mimeTypes.get(0) : "*/*");
        } else {
            shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, fileUris);
            shareIntent.setType(reduceMimeTypes(mimeTypes));
        }
        if (text != null) shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        if (subject != null) shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent chooserIntent = Intent.createChooser(shareIntent, null /* dialog title optional */);

        List<ResolveInfo> resInfoList =
                getContext()
                        .getPackageManager()
                        .queryIntentActivities(chooserIntent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            for (Uri fileUri : fileUris) {
                getContext()
                        .grantUriPermission(
                                packageName,
                                fileUri,
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        }

        startActivity(chooserIntent);
    }

    private void startActivity(Intent intent) {
        if (activity != null) {
            activity.startActivity(intent);
        } else if (context != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
            throw new IllegalStateException("Both context and activity are null");
        }
    }

    private ArrayList<Uri> getUrisForPaths(List<String> paths) throws IOException {
        ArrayList<Uri> uris = new ArrayList<>(paths.size());
        for (String path : paths) {
            File file = new File(path);
            if (!fileIsOnExternal(file)) {
                file = copyToExternalShareFolder(file);
            }

            uris.add(
                    FileProvider.getUriForFile(
                            getContext(), getContext().getPackageName() + ".flutter.share_provider", file));
        }
        return uris;
    }

    private String reduceMimeTypes(List<String> mimeTypes) {
        if (mimeTypes.size() > 1) {
            String reducedMimeType = mimeTypes.get(0);
            for (int i = 1; i < mimeTypes.size(); i++) {
                String mimeType = mimeTypes.get(i);
                if (!reducedMimeType.equals(mimeType)) {
                    if (getMimeTypeBase(mimeType).equals(getMimeTypeBase(reducedMimeType))) {
                        reducedMimeType = getMimeTypeBase(mimeType) + "/*";
                    } else {
                        reducedMimeType = "*/*";
                        break;
                    }
                }
            }
            return reducedMimeType;
        } else if (mimeTypes.size() == 1) {
            return mimeTypes.get(0);
        } else {
            return "*/*";
        }
    }

    @NonNull
    private String getMimeTypeBase(String mimeType) {
        if (mimeType == null || !mimeType.contains("/")) {
            return "*";
        }

        return mimeType.substring(0, mimeType.indexOf("/"));
    }

    private boolean fileIsOnExternal(File file) {
        try {
            String filePath = file.getCanonicalPath();
            File externalDir = context.getExternalFilesDir(null);
            return externalDir != null && filePath.startsWith(externalDir.getCanonicalPath());
        } catch (IOException e) {
            return false;
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void clearExternalShareFolder() {
        File folder = getExternalShareFolder();
        if (folder.exists()) {
            for (File file : folder.listFiles()) {
                file.delete();
            }
            folder.delete();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private File copyToExternalShareFolder(File file) throws IOException {
        File folder = getExternalShareFolder();
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File newFile = new File(folder, file.getName());
        copy(file, newFile);
        return newFile;
    }

    @NonNull
    private File getExternalShareFolder() {
        return new File(getContext().getExternalCacheDir(), "share");
    }

    private Context getContext() {
        if (activity != null) {
            return activity;
        }
        if (context != null) {
            return context;
        }

        throw new IllegalStateException("Both context and activity are null");
    }

    private static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }


    /**
     * share to twitter
     *
     * @param url    String
     * @param msg    String
     * @param result Result
     */
    void shareToTwitter(final String url, final String msg, final MethodChannel.Result result) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    TweetComposer.Builder builder = new TweetComposer.Builder(activity)
                            .text(msg);
                    if (url != null && url.length() > 0) {
                        builder.url(new URL(url));
                    }

                    builder.show();
                    result.success("success");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    result.error("error", e.toString(), "");
                }
            }
        });
    }

    /**
     * share to Facebook
     *
     * @param url    String
     * @param msg    String
     * @param result Result
     */
    void shareToFacebook(final String url, final String msg, final MethodChannel.Result result) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ShareDialog shareDialog = new ShareDialog(activity);
                // this part is optional
                shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
                    @Override
                    public void onSuccess(Sharer.Result result) {
                        System.out.println("--------------------success");
                    }

                    @Override
                    public void onCancel() {
                        System.out.println("-----------------onCancel");
                    }

                    @Override
                    public void onError(FacebookException error) {
                        System.out.println("---------------onError");
                    }
                });

                ShareLinkContent content = new ShareLinkContent.Builder()
                        .setContentUrl(Uri.parse(url))
                        .setQuote(msg)
                        .build();
                if (ShareDialog.canShow(ShareLinkContent.class)) {
                    shareDialog.show(content);
                    result.success("success");
                }
            }
        });
    }

    /**
     * share to whatsapp
     *
     * @param msg                String
     * @param result             Result
     * @param shareToWhatsAppBiz boolean
     */
    void shareWhatsApp(final String url, final String msg, final MethodChannel.Result result, final boolean shareToWhatsAppBiz) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                    whatsappIntent.setType("text/plain");
                    whatsappIntent.setPackage(shareToWhatsAppBiz ? "com.whatsapp.w4b" : "com.whatsapp");
                    whatsappIntent.putExtra(Intent.EXTRA_TEXT, msg);

                    if (!TextUtils.isEmpty(url)) {
                        FileUtil fileHelper = new FileUtil(activity, url);
                        if (fileHelper.isFile()) {
                            whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            whatsappIntent.putExtra(Intent.EXTRA_STREAM, fileHelper.getUri());
                            whatsappIntent.setType(fileHelper.getType());
                        }
                    }
                    whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    activity.startActivity(whatsappIntent);
                    result.success("success");
                } catch (Exception var9) {
                    result.error("error", var9.toString(), "");
                }
            }
        });
    }

    void shareWeChat(final String msg, final String title, final MethodChannel.Result result) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    List<ResolveInfo> resInfo = activity.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                    if (!resInfo.isEmpty()) {
                        List<Intent> targetedShareIntents = new ArrayList<>();
                        for (ResolveInfo info : resInfo) {
                            Intent targeted = new Intent(Intent.ACTION_SEND);
                            targeted.setType("text/plain");
                            ActivityInfo activityInfo = info.activityInfo;
                            // Shared content
                            targeted.putExtra(Intent.EXTRA_TEXT, msg);
                            // Shared headlines
//                    targeted.putExtra(Intent.EXTRA_SUBJECT, "theme");
                            targeted.setPackage(activityInfo.packageName);
                            targeted.setClassName(activityInfo.packageName, info.activityInfo.name);
                            PackageManager pm = activity.getApplication().getPackageManager();
                            // Wechat has two distinctions. - Friendship circle and Wechat
                            if (info.activityInfo.applicationInfo.loadLabel(pm).toString().equals("WeChat") || info.activityInfo.packageName.contains("tencent.mm")) {
                                targetedShareIntents.add(targeted);
                            }
                        }
                        if (targetedShareIntents.isEmpty()) {
                            result.success("success");
                            return;
                        }
                        Intent chooserIntent = Intent.createChooser(targetedShareIntents.remove(0), title);
                        if (chooserIntent == null) {
                            result.success("success");
                            return;
                        }
                        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[]{}));
                        activity.startActivity(chooserIntent);
                    }
                    result.success("success");
                } catch (Exception var9) {
                    result.error("error", var9.toString(), "");
                }
            }
        });
    }

}
