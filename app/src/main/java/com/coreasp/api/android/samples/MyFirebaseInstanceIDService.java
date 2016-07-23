/**
 * MyFirebaseInstanceIDService
 * CorePush-API-Android-Samples
 *
 * Copyright © 2016年 株式会社ブレスサービス. All rights reserved.
 */


package com.coreasp.api.android.samples;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * FirebaseInstanceIdServiceのカスタムクラス
 *
 * トークンが更新された場合に、onTokenRefreshが呼び出される。
 */
public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String LOG_TAG = "COREPUSH";

    /**
     *  トークン更新時に呼ばれる。
     *  ref. https://developers.google.com/android/reference/com/google/android/gms/iid/InstanceIDListenerService
     */
    @Override
    public void onTokenRefresh() {
        // 更新されたトークンを取得
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(LOG_TAG
                , "Refreshed token: " + refreshedToken);

        // トークンをCorePushサーバに登録する
        CorePushAppManager.getInstance().registerToken(true);
    }
}
