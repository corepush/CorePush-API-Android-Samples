/**
 * CorePushRegistrationIntentService
 * CorePush-API-Android-Samples
 *
 * Copyright © 2016年 株式会社ブレスサービス. All rights reserved.
 */

package com.coreasp.api.android.samples;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

public class CorePushRegistrationIntentService extends IntentService {

    private static final String LOG_TAG = "COREPUSH";

    // GCMからFCMトークンへのマイグレーション判定用のキー
    private static final String COREPUSH_CGM_TO_FCM_TOKEN_MIGRATION_KEY = "COREPUSH_CGM_TO_FCM_TOKEN_MIGRATION_KEY";

    // FCMのインスタンスIDのスコープ
    private static final String FIREBASE_INSTANCE_ID_SCOPE = "FCM";

    // CorePushサーバのトークン登録・削除のAPI
    private static final String CORE_PUSH_REGISTER_API = "https://api.core-asp.com/android_token_regist.php";

    // リトライ処理のパラメータ
    private static final int BACKOFF_MILLI_SECONDS = 2000;
    private static final int MAX_ATTEMPTS = 5;
    private static final Random sRandom = new Random();

    public CorePushRegistrationIntentService() {
        super("CorePushRegistrationIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        boolean unregistered = intent.getBooleanExtra("unregistered", false);

        if (unregistered) {
            // CorePushサーバのトークンの削除処理
            onUnregistered(intent);
        } else {
            // CorePushサーバへのトークンの登録処理
            onRegistered(intent);
        }
    }

    /**
     * トークンの登録処理時に呼ばれる
     *
     * @param intent
     */
    protected void onRegistered(Intent intent) {

        try {
            String oldToken = CorePushAppManager.getInstance().getToken();
            String newToken = getFirebaseInstanceIdToken();
            if (newToken == null) {
                return;
            }

            if (oldToken != null && !oldToken.isEmpty() && !oldToken.equals(newToken)) {
                // トークンが更新された場合
                SharedPreferences prefs = getSharedPreferences(CorePushAppManager.COREPUSH_SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(CorePushAppManager.COREPUSH_SENT_TOKEN_TO_SERVER, false);
                editor.commit();

                // CorePushサーバから古いトークンを削除後に新しいトークンを再登録する
                if (removeTokenFromServer(this, oldToken)) {
                    sendTokenToServer(this, newToken);
                }
            } else {
                // トークンがサーバに未登録の場合
                SharedPreferences preferences = this.getSharedPreferences(CorePushAppManager.COREPUSH_SHARED_PREFERENCE_NAME, Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                boolean isMigrationGcmToFcm = preferences.getBoolean(COREPUSH_CGM_TO_FCM_TOKEN_MIGRATION_KEY, false);
                if (!isMigrationGcmToFcm) {
                    // 以前のGCMのトークンがある場合は、GCMのトークンを削除する。
                    // (gcmのトークンは、com.google.android.gcmのプレファレンス内のregIdキーに保存されている)
                    SharedPreferences gcmPreferences = this.getSharedPreferences(CorePushAppManager.GOOGLE_GCM_SHARED_PREFERENCE_NAME, Activity.MODE_PRIVATE);
                    oldToken = gcmPreferences.getString(CorePushAppManager.COREPUSH_GCM_TOKEN_KEY, "");
                    if (oldToken != null && !oldToken.isEmpty()) {
                        if (removeTokenFromServer(this, oldToken)) {
                            editor.putBoolean(COREPUSH_CGM_TO_FCM_TOKEN_MIGRATION_KEY, true);
                            editor.commit();
                        }
                    } else {
                        editor.putBoolean(COREPUSH_CGM_TO_FCM_TOKEN_MIGRATION_KEY, true);
                        editor.commit();
                    }
                }

                // CorePushサーバに新しいトークンを登録する
                sendTokenToServer(this, newToken);
            }

        } catch (Exception e) {
            Log.d(LOG_TAG, "Failed to complete token registration", e);
        } finally {
            Intent registrationComplete = new Intent(CorePushAppManager.COREPUSH_REGISTRATION_COMPLETE);
            LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
        }
    }

    /**
     * トークンの削除処理時に呼ばれる
     *
     * @param intent
     */
    protected void onUnregistered(Intent intent) {
        try {
            String token = CorePushAppManager.getInstance().getToken();
            if (token == null || token.isEmpty()) {
                return;
            }

            // サーバからトークンを削除する
            if (removeTokenFromServer(this, token)) {
                return;
            }

        } catch (Exception e) {
            Log.d(LOG_TAG, "Failed to complete token unregistration", e);
        } finally {
            Intent registrationComplete = new Intent(CorePushAppManager.COREPUSH_REGISTRATION_COMPLETE);
            LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
        }
    }

    /**
     * FCMサービスのトークンを取得する
     *
     * @return FCMサービスに登録したトークン
     */
    private String getFirebaseInstanceIdToken() {
        try {
            String senderId = CorePushAppManager.getInstance().getSenderId();
            String token = FirebaseInstanceId.getInstance().getToken(senderId, FIREBASE_INSTANCE_ID_SCOPE);
            return token;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * デバイスに登録したトークンを保存する
     *
     * @param context コンテキスト
     */
    private void saveToken(Context context, String token) {
        SharedPreferences preferences = context.getSharedPreferences(CorePushAppManager.COREPUSH_SHARED_PREFERENCE_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        if (token == null || token.isEmpty()) {
            editor.remove(CorePushAppManager.COREPUSH_FCM_TOKEN_KEY);
        } else {
            editor.putString(CorePushAppManager.COREPUSH_FCM_TOKEN_KEY, token);
        }

        editor.commit();
    }

    /**
     * デバイスがGCMサービスに登録後、呼び出される。
     * CorePushサーバにトークンを登録する処理を実装。
     */
    protected boolean sendTokenToServer(Context context, String token) {
        Log.d(LOG_TAG, "device token (onRegistered): " + token);

        SharedPreferences preferences = context.getSharedPreferences(CorePushAppManager.COREPUSH_SHARED_PREFERENCE_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // デバイストークンの登録成功時の動作を記述
        HashMap<String, String> postDataParams = new HashMap<>();

        //config_keyパラメータ(必須)。設定キー。
        postDataParams.put("config_key", CorePushAppManager.getInstance().getConfigKey());

        //device_tokenパラメータ(必須)。デバイストークン(通知送信用のID)。
        postDataParams.put("device_token", token);

        //modeパラメータ。デバイストークン(登録:1/削除:2)
        postDataParams.put("mode", "1");

        long backoff = BACKOFF_MILLI_SECONDS + sRandom.nextInt(1000);
        for (int i = 1; i <= MAX_ATTEMPTS; i++) {

            JSONObject data;
            URL url;
            HttpURLConnection connection = null;
            try {
                String jsonText = "";
                url = new URL(CORE_PUSH_REGISTER_API);
                connection = (HttpURLConnection) url.openConnection();
                connection.setUseCaches(false);
                connection.setReadTimeout(30000);
                connection.setConnectTimeout(30000);
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);

                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(CorePushUtil.getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();
                int responseCode = connection.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        jsonText += line;
                    }
                } else {
                    throw new IOException();
                }

                data = new JSONObject(jsonText);
            } catch (IOException e) {
                if (i == MAX_ATTEMPTS) {
                    break;
                }
                try {
                    Thread.sleep(backoff);
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                    return false;
                }

                backoff *= 2;
                continue;
            } catch (Exception e) {
                return false;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

            int status;
            try {
                status = data.getInt("status");
                if (status == 0) {
                    // トークン送信済みフラグをtrueにする
                    editor.putBoolean(CorePushAppManager.COREPUSH_SENT_TOKEN_TO_SERVER, true);
                    editor.commit();

                    // トークンの登録成功時に プレファレンス内にトークンを保存する
                    saveToken(this, token);
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }

        return false;
    }

    /**
     * CorePushサーバからトークンを削除する処理を実装。
     */
    protected boolean removeTokenFromServer(Context context, String token) {
        Log.d(LOG_TAG, "device token (onUnregistered): " + token);

        SharedPreferences preferences = context.getSharedPreferences(CorePushAppManager.COREPUSH_SHARED_PREFERENCE_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // デバイストークンの削除成功時の動作を記述
        HashMap<String, String> postDataParams = new HashMap<>();

        //config_keyパラメータ(必須)。設定キー。
        postDataParams.put("config_key", CorePushAppManager.getInstance().getConfigKey());

        //device_tokenパラメータ(必須)。デバイストークン(通知送信用のID)。
        postDataParams.put("device_token", token);

        //modeパラメータ。デバイストークン(登録:1/削除:2)
        postDataParams.put("mode", "2");

        JSONObject data;
        URL url;
        HttpURLConnection connection = null;
        try {
            String jsonText = "";
            url = new URL(CORE_PUSH_REGISTER_API);
            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(30000);
            connection.setConnectTimeout(30000);
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            OutputStream os = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(CorePushUtil.getPostDataString(postDataParams));

            writer.flush();
            writer.close();
            os.close();
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = br.readLine()) != null) {
                    jsonText += line;
                }
            } else {
                return false;
            }

            data = new JSONObject(jsonText);
        } catch (Exception e) {
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        int status;
        try {
            status = data.getInt("status");
            if (status == 0) {
                // トークン送信済みフラグをfalseにする
                editor.putBoolean(CorePushAppManager.COREPUSH_SENT_TOKEN_TO_SERVER, false);
                editor.commit();

                // トークンの削除成功時に プレファレンス内のトークンを削除する
                saveToken(this, null);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

}