// Copyright 2017 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.firebasemessaging;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FlutterFirebaseMessagingService extends FirebaseMessagingService {

    public static final String ACTION_REMOTE_MESSAGE =
            "io.flutter.plugins.firebasemessaging.NOTIFICATION";
    public static final String EXTRA_REMOTE_MESSAGE = "notification";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String data = null, notification = null;
        if (remoteMessage.getData() != null) {
            data = remoteMessage.getData().toString();
        }
        if (remoteMessage.getNotification() != null) {
            notification = "{ body: " + remoteMessage.getNotification().getBody() + ", title: " + remoteMessage.getNotification().getTitle() + " }";
        }
        Log.d("tag", "onMessageReceived: data: " + data + ", notification: " + notification);
        Intent intent = new Intent(ACTION_REMOTE_MESSAGE);
        intent.putExtra(EXTRA_REMOTE_MESSAGE, remoteMessage);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
