/**
 * TokenUnregisterActivity
 * CorePush-API-Android-Samples
 *
 * Copyright © 2016年 株式会社ブレスサービス. All rights reserved.
 */

package com.coreasp.api.android.samples;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * トークン削除画面のアクティビティ
 */
public class TokenUnregisterActivity extends Activity {

    // トークン削除後のコールバック用のレシーバー
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    // トークン削除中のプログレスバー
    private ProgressBar mRegistrationProgressBar;

    // トークン表示用のテキストビュー
    private TextView mInformationTextView;

    // トークン削除用のボタン
    private Button tokenUnregisterButton;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token_unregister);

        // 登録ボタン押下時のトークン削除処理
        tokenUnregisterButton = (Button) findViewById(R.id.tokenUnregisterButton);
        tokenUnregisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CorePushAppManager manager = CorePushAppManager.getInstance();
                mRegistrationProgressBar.setVisibility(ProgressBar.VISIBLE);

                //CORE PUSHにデバイストークンを登録
                manager.removeToken();
            }
        });

        // プログレスバーの初期化処理
        mRegistrationProgressBar = (ProgressBar) findViewById(R.id.registrationProgressBar);
        mRegistrationProgressBar.setVisibility(ProgressBar.GONE);

        // トークン文字列を表示するテキストビューの初期化処理
        mInformationTextView = (TextView) findViewById(R.id.informationTextView);
        mInformationTextView.setVisibility(TextView.VISIBLE);
        updateInformationView();

        // トークン登録処理完了後のコールバック処理
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mRegistrationProgressBar.setVisibility(ProgressBar.GONE);
                updateInformationView();
            }
        };
    }

    /**
     * トークン表示用のテキストビューを更新する
     */
    private void updateInformationView() {
        boolean sentToken = CorePushAppManager.getInstance().isTokenRegistered();

        if (sentToken) {
            String token = CorePushAppManager.getInstance().getToken();
            mInformationTextView.setText("デバイストークン:" + token);
        } else {
            mInformationTextView.setText("デバイストークン: 空");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // トークン削除後のコールバック用のレシーバーを登録
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(CorePushAppManager.COREPUSH_REGISTRATION_COMPLETE));
    }

    @Override
    protected void onPause() {
        // トークン削除後のコールバック用のレシーバーを解除
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }
}