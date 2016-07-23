/**
 * MyApplication
 * CorePush-API-Android-Samples
 *
 * Copyright © 2016年 株式会社ブレスサービス. All rights reserved.
 */

package com.coreasp.api.android.samples;

import android.app.Application;

/**
 * Applicationのカスタムクラス
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // マネージャーの初期化処理
        CorePushAppManager.initialize(this);
    }
}


