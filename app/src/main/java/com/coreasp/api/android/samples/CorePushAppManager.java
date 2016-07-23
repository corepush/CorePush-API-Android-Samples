/**
 * CorePushAppManager
 * CorePush-API-Android-Samples
 *
 * Copyright © 2016年 株式会社ブレスサービス. All rights reserved.
 */

package com.coreasp.api.android.samples;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;

/**
 * 通知用のマネージャークラス
 */
public class CorePushAppManager {

    // CorePush関連の値が保存されているSharedPreference名
    public static final String COREPUSH_SHARED_PREFERENCE_NAME = "CORE_PUSH_PREFERENCE";

    // GCMのトークンが保存されているSharedPreference名
    public static final String GOOGLE_GCM_SHARED_PREFERENCE_NAME = "com.google.android.gcm";

    // FCMのSharedPreferenceのトークン保存用のキー
    public static final String COREPUSH_FCM_TOKEN_KEY = "CORE_PUSH_FCM_TOKEN_KEY";

    // GCMのSharedPreferenceのトークン保存用のキー
    public static final String COREPUSH_GCM_TOKEN_KEY = "regId";

    // トークン登録・削除時完了時のブロードキャスト名
    public static final String COREPUSH_REGISTRATION_COMPLETE = "CORE_PUSH_REGISTRATION_COMPLETE";

    // トークンのサーバー送信済みフラグ用のキー
    public static final String COREPUSH_SENT_TOKEN_TO_SERVER = "COREPUSH_SENT_TOKEN_TO_SERVER";

    // シングルトンインスタンス
    static CorePushAppManager instance_;

    // アプリケーションコンテキスト用のオブジェクト
    private Context mContext;

    public static void initialize(Context context) {
        if (instance_ != null) {
            return;
        }

        instance_ = new CorePushAppManager();
        instance_.mContext = context;
    }

    /**
     * CorePushManagerを取得する.
     *
     * @return CorePushAppManager
     */
    public static CorePushAppManager getInstance() {
        if (instance_ == null) {
            throw new RuntimeException("initialize call is required");
        }
        return instance_;
    }

    private CorePushAppManager() {
    }

    /**
     * 設定キーを取得する．
     *
     * @return configKey 設定キー
     */
    public String getConfigKey() {
        return mContext.getString(R.string.core_push_config_key);
    }

    /**
     * senderIdを取得する．
     *
     * @return senderId
     */
    public String getSenderId() {
        // googele-services.jsonのproject_idの値を取得
        return mContext.getString(R.string.gcm_defaultSenderId);
    }

    /**
     * 通知から起動するアクティビティを取得する．
     *
     * @return activity 通知から起動するアクティビティ
     */
    public Class<?> getActivity() {
        //通知から起動するアクティビティのクラスを指定する
        return MainActivity.class;
    }

    /**
     * 通知アイコンのリソースIDを取得する。
     *
     * @return iconResourceId 通知アイコンのリソースID
     */
    public int getIconResourceId() {
        // 通知アイコンのリソースIDを指定する
        return R.mipmap.ic_launcher;
    }

    /**
     * デバイストークン登録の有無を取得する
     *
     * @return デバイストークン登録の有無
     */
    public boolean isTokenRegistered() {
        final String token = getToken();
        if (token.equals("") || getTokenRefreshNeeds()) {
            return false;
        }

        return true;
    }

    /**
     * FCMのデバイストークン登録
     *
     * @param isForced 強制的にトークンを登録するか
     */
    public void registerToken(boolean isForced) {
        if (isForced || !isTokenRegistered()) {
            Intent intent = new Intent(mContext, CorePushRegistrationIntentService.class);
            intent.putExtra("unregistered", false);
            mContext.startService(intent);
        } else {
            Intent registrationComplete = new Intent(CorePushAppManager.COREPUSH_REGISTRATION_COMPLETE);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(registrationComplete);
        }
    }

    /**
     * FCMのデバイストークン削除
     */
    public void removeToken() {
        if (isTokenRegistered()) {
            Intent intent = new Intent(mContext, CorePushRegistrationIntentService.class);
            intent.putExtra("unregistered", true);
            mContext.startService(intent);
        } else {
            Intent registrationComplete = new Intent(CorePushAppManager.COREPUSH_REGISTRATION_COMPLETE);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(registrationComplete);
        }
    }

    /**
     * 通知センターから起動するActivityでIntentから日時を取得
     *
     * @param intent 通知センターから起動するActivityのIntent
     * @return 通知日時
     */
    public String getDate(Intent intent) {
        return intent.getStringExtra("cp_date");
    }

    /**
     * FCMのデバイストークンを取得
     *
     * @return　デバイストークン
     */
    public String getToken() {
        SharedPreferences prefs = mContext.getSharedPreferences(COREPUSH_SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        return prefs.getString(COREPUSH_FCM_TOKEN_KEY, "");
    }

    /**
     * FCMのデバイストークン更新の必要性の有無を取得する
     *
     * @return　デバイストークンの更新が必要か
     */
    private boolean getTokenRefreshNeeds() {
        SharedPreferences prefs = mContext.getSharedPreferences(COREPUSH_SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        return !prefs.getBoolean(COREPUSH_SENT_TOKEN_TO_SERVER, false);
    }

    /**
     * 通知センターから起動するActivityでIntentからタイトルを取得
     *
     * @param intent 通知センターから起動するActivityのIntent
     * @return 通知タイトル
     */
    public String getTitle(Intent intent) {
        return intent.getStringExtra("cp_title");
    }

    /**
     * 通知センターから起動するActivityでIntentからメッセージを取得
     *
     * @param intent 通知センターから起動するActivityのIntent
     * @return 通知メッセージ
     */
    public String getMessage(Intent intent) {
        return intent.getStringExtra("cp_message");
    }

    /**
     * 通知センターから起動するActivityでIntentからURLを取得
     *
     * @param intent 通知センターから起動するActivityのIntent
     * @return 通知URL
     */
    public String getUrl(Intent intent) {
        return intent.getStringExtra("cp_url");
    }

    /**
     * 通知センターから起動するActivityでIntentから通知IDを取得
     *
     * @param intent 通知センターから起動するActivityのIntent
     * @return 通知ID
     */
    public String getPushId(Intent intent) {
        return intent.getStringExtra("cp_push_id");
    }
}