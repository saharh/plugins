package io.flutter.plugins.firebasemessaging;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import java.util.Map;

import androidx.annotation.DrawableRes;
import androidx.core.app.NotificationCompat;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NotificationHandler {

    public boolean handleNotificationData(Context context, Map<String, String> data) {
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

        final PendingIntent contentIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, getDefaultChannel(context))
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(getDefaultIcon(context))
//                .setColor(ContextCompat.getColor(context, R.color.accent))
                .setStyle(style)
                .setAutoCancel(autoCancel)
                .setContentIntent(contentIntent)
//                .setDeleteIntent(dismissPendingIntent)
                .setSound(defaultSound)
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

    @DrawableRes
    private int getDefaultIcon(Context context) {
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
}
