/**
 * CorePushHistoryManagerListener
 * CorePush-API-Android-Samples
 *
 * Copyright © 2016年 株式会社ブレスサービス. All rights reserved.
 */

package com.coreasp.api.android.samples;

/**
 * CorePushHistoryManager のリスナークラス
 */
public interface CorePushHistoryManagerListener {
    /**
     * 通知履歴の取得の成功時に呼ばれる。
     */
    public void historyManagerSuccess();

    /**
     * 通知履歴の取得の失敗時に呼ばれる。
     */
    public void historyManagerFail();
}