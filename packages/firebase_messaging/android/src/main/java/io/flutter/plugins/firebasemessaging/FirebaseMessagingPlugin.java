// Copyright 2017 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.firebasemessaging;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.NewIntentListener;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * FirebaseMessagingPlugin
 */
public class FirebaseMessagingPlugin extends BroadcastReceiver
        implements MethodCallHandler, NewIntentListener {
    private final Registrar registrar;
    private final MethodChannel channel;

    private static final String CLICK_ACTION_VALUE = "FLUTTER_NOTIFICATION_CLICK";
    private static final String TAG = "FirebaseMessagingPlugin";

    public static void registerWith(Registrar registrar) {
        final MethodChannel channel =
                new MethodChannel(registrar.messenger(), "plugins.flutter.io/firebase_messaging");
        final FirebaseMessagingPlugin plugin = new FirebaseMessagingPlugin(registrar, channel);
        registrar.addNewIntentListener(plugin);
        channel.setMethodCallHandler(plugin);
    }

    private FirebaseMessagingPlugin(Registrar registrar, MethodChannel channel) {
        this.registrar = registrar;
        this.channel = channel;
        FirebaseApp.initializeApp(registrar.context());

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FlutterFirebaseInstanceIDService.ACTION_TOKEN);
        intentFilter.addAction(FlutterFirebaseMessagingService.ACTION_REMOTE_MESSAGE);
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(registrar.context());
        manager.registerReceiver(this, intentFilter);
    }

    // BroadcastReceiver implementation.
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action == null) {
            return;
        }

        if (action.equals(FlutterFirebaseInstanceIDService.ACTION_TOKEN)) {
            String token = intent.getStringExtra(FlutterFirebaseInstanceIDService.EXTRA_TOKEN);
            channel.invokeMethod("onToken", token);
        } else if (action.equals(FlutterFirebaseMessagingService.ACTION_REMOTE_MESSAGE)) {
            RemoteMessage message =
                    intent.getParcelableExtra(FlutterFirebaseMessagingService.EXTRA_REMOTE_MESSAGE);
            Map<String, Object> content = parseRemoteMessage(message);
            if (handleNotificationData(context, message.getData())) {
                return;
            }
            channel.invokeMethod("onMessage", content);
        }
    }

    private boolean handleNotificationData(Context context, Map<String, String> data) {
        if (data == null || !"notification".equals(data.get("type"))) {
            return false;
        }
        String body = data.get("body");
        String title = data.get("title");
        String tag = data.get("tag");
        boolean autoCancel = true;
        String autoCancelStr = data.get("auto_cancel");
        if (autoCancelStr != null && autoCancelStr.equals("false")) {
            autoCancel = false;
        }
        showNotification(context, title, body, tag, autoCancel);
        return true;
    }

    private void showNotification(Context context, String title, String body, String tag, boolean autoCancel) {
        //        String contentTitle = context.getString(fcmNotification.getTitleLocKey(), (Object[]) fcmNotification.getTitleLocArgs());
//        String contentText = "";
//        if (fcmNotification.getBodyLocKey() != null) {
//            contentText = context.getString(fcmNotification.getBodyLocKey(), (Object[]) fcmNotification.getBodyLocArgs());
//        }
        NotificationCompat.Style style = new NotificationCompat.BigTextStyle()
                .setBigContentTitle(title)
                .bigText(body);
        PackageManager pm = context.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(context.getPackageName());
//        Intent intent = new Intent(context, MainActivity.class);

        final PendingIntent contentIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, getDefaultChannel(context))
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(getDefaultIcon(context))
//                .setColor(ContextCompat.getColor(context, R.color.accent))
                .setStyle(style)
                .setAutoCancel(autoCancel)
                .setContentIntent(contentIntent)
//                .setDeleteIntent(dismissPendingIntent)
//                .setSound(fcmNotification.getSound())
                .setPriority(Notification.PRIORITY_MAX);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_STATUS);
        }
        Notification notification = builder.build();

        if (autoCancel) {
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
        }
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(tag, 0, notification);
    }

    private String getDefaultChannel(Context context) {
        String fallbackChannelId = "DEFAULT";
        try {
            ApplicationInfo app = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle metaData = app.metaData;
            String defaultChannelId = metaData.getString("com.google.firebase.messaging.default_notification_channel_id");
            Log.d("tag", "defaultChannelId found: " + defaultChannelId);
            return defaultChannelId != null ? defaultChannelId : fallbackChannelId;
        } catch (Exception e) {
            return fallbackChannelId;
        }
    }

    private @DrawableRes
    int getDefaultIcon(Context context) {
        try {
            ApplicationInfo app = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle metaData = app.metaData;
            int defaultNotificationIcon = metaData.getInt("com.google.firebase.messaging.default_notification_icon", 0);
            Log.d("tag", "defaultNotificationIcon found: " + defaultNotificationIcon);
            if (defaultNotificationIcon != 0) {
                return defaultNotificationIcon;
            }
            return getAppIcon(context);
        } catch (Exception e) {
            return getAppIcon(context);
        }
    }

    private int getAppIcon(Context context) {
        try {
            int icon = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0).icon;
            Log.d("tag", "getPackageManager icon found: " + icon);
            return icon;
        } catch (Exception e) {
            return 0;
        }
    }

    @NonNull
    private Map<String, Object> parseRemoteMessage(RemoteMessage message) {
        Map<String, Object> content = new HashMap<>();
        content.put("data", message.getData());

        RemoteMessage.Notification notification = message.getNotification();

        Map<String, Object> notificationMap = new HashMap<>();

        String title = notification != null ? notification.getTitle() : null;
        notificationMap.put("title", title);

        String body = notification != null ? notification.getBody() : null;
        notificationMap.put("body", body);

        content.put("notification", notificationMap);
        return content;
    }

    @Override
    public void onMethodCall(final MethodCall call, final Result result) {
        if ("configure".equals(call.method)) {
            FlutterFirebaseInstanceIDService.broadcastToken(registrar.context());
            if (registrar.activity() != null) {
                sendMessageFromIntent("onLaunch", registrar.activity().getIntent());
            }
            result.success(null);
        } else if ("subscribeToTopic".equals(call.method)) {
            String topic = call.arguments();
            FirebaseMessaging.getInstance().subscribeToTopic(topic);
            result.success(null);
        } else if ("unsubscribeFromTopic".equals(call.method)) {
            String topic = call.arguments();
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
            result.success(null);
        } else if ("getToken".equals(call.method)) {
            FirebaseInstanceId.getInstance()
                    .getInstanceId()
                    .addOnCompleteListener(
                            new OnCompleteListener<InstanceIdResult>() {
                                @Override
                                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                    if (!task.isSuccessful()) {
                                        Log.w(TAG, "getToken, error fetching instanceID: ", task.getException());
                                        result.success(null);
                                        return;
                                    }

                                    result.success(task.getResult().getToken());
                                }
                            });
        } else if ("deleteInstanceID".equals(call.method)) {
            new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                FirebaseInstanceId.getInstance().deleteInstanceId();
                                result.success(true);
                            } catch (IOException ex) {
                                Log.e(TAG, "deleteInstanceID, error:", ex);
                                result.success(false);
                            }
                        }
                    })
                    .start();
        } else if ("autoInitEnabled".equals(call.method)) {
            result.success(FirebaseMessaging.getInstance().isAutoInitEnabled());
        } else if ("setAutoInitEnabled".equals(call.method)) {
            Boolean isEnabled = (Boolean) call.arguments();
            FirebaseMessaging.getInstance().setAutoInitEnabled(isEnabled);
            result.success(null);
        } else if ("getInstanceId".equals(call.method)) {
            String instanceId = FirebaseInstanceId.getInstance().getId();
            result.success(instanceId);
        } else {
            result.notImplemented();
        }
    }

    @Override
    public boolean onNewIntent(Intent intent) {
        boolean res = sendMessageFromIntent("onResume", intent);
        if (res && registrar.activity() != null) {
            registrar.activity().setIntent(intent);
        }
        return res;
    }

    /**
     * @return true if intent contained a message to send.
     */
    private boolean sendMessageFromIntent(String method, Intent intent) {
        if (CLICK_ACTION_VALUE.equals(intent.getAction())
                || CLICK_ACTION_VALUE.equals(intent.getStringExtra("click_action"))) {
            Map<String, String> message = new HashMap<>();
            Bundle extras = intent.getExtras();

            if (extras == null) {
                return false;
            }

            for (String key : extras.keySet()) {
                Object extra = extras.get(key);
                if (extra != null) {
                    message.put(key, extra.toString());
                }
            }

            channel.invokeMethod(method, message);
            return true;
        }
        return false;
    }
}
