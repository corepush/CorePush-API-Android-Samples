/**
 * MyFirebaseMessagingService
 * CorePush-API-Android-Samples
 *
 * Copyright © 2016年 株式会社ブレスサービス. All rights reserved.
 */

package com.coreasp.api.android.samples;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * FirebaseMessagingServiceのカスタムクラス。
 *
 * 通知メッセージの受信処理を行う。
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    /** CORE PUSHの通知ペイロードのパラメータ */
    private static final String COREPUSH_DATA_TITLE = "title";
    private static final String COREPUSH_DATA_MESSAGE = "message";
    private static final String COREPUSH_DATA_URL = "url";
    private static final String COREPUSH_DATA_PUSH_ID = "push_id";

    /**
     * 通知メッセージ受信時にコールバックされる。
     *
     * @param remoteMessage
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        sendNotification(data);
    }

    /**
     * FCMのメッセージデータを元に通知を表示する
     *
     * @param data FCMのメッセージデータ
     */
    private void sendNotification(Map<String, String> data) {

        // メッセージを受信した時の動作を記述
        String title = data.get(COREPUSH_DATA_TITLE);
        String message = data.get(COREPUSH_DATA_MESSAGE);
        String url = data.get(COREPUSH_DATA_URL);
        String pushId = data.get(COREPUSH_DATA_PUSH_ID);

        // タイトル、メッセージが空の場合は受信処理を行わない
        if (title == null || title.isEmpty() || message == null || message.isEmpty()) {
            return;
        }

        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        // 通知受信日時のフォーマットを作成
        Date date = new Date(); // Dateオブジェクトを生成
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        String dateStr = sdf.format(date);

        notificationStatusBar(this, dateStr, title, message, url, pushId,
                notificationManager, R.mipmap.ic_launcher, MainActivity.class);

    }

    /**
     * ステータスバーに通知を表示する
     *
     * @param context
     * @param date
     * @param title
     * @param message
     * @param url
     * @param pushId
     * @param notificationManager
     * @param iconResourceId
     * @param activity
     */
    private void notificationStatusBar(Context context, String date, String title,
                                       String message, String url, String pushId,
                                       NotificationManager notificationManager, int iconResourceId,
                                       Class<?> activity) {

        Intent notificationIntent = new Intent(context, activity);

        // 通知から起動するActivityへインテントパラメータを送る
        notificationIntent.putExtra("cp_date", date);
        notificationIntent.putExtra("cp_title", title);
        notificationIntent.putExtra("cp_message", message);
        notificationIntent.putExtra("cp_url", url);
        notificationIntent.putExtra("cp_push_id", pushId);

        PendingIntent pi = PendingIntent.getActivity(context, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context);
        builder.setContentTitle(title);
        builder.setContentText(message);
        builder.setContentIntent(pi);
        builder.setSmallIcon(iconResourceId);
        builder.setWhen(System.currentTimeMillis());
        builder.setAutoCancel(true);
        notificationManager.notify(0, builder.build());
    }
}
