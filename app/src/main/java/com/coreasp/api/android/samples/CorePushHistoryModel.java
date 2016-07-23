/**
 * CorePushHistoryModel
 * CorePush-API-Android-Samples
 *
 * Copyright © 2016年 株式会社ブレスサービス. All rights reserved.
 */

package com.coreasp.api.android.samples;

/**
 * 通知履歴を格納するモデル
 */
public class CorePushHistoryModel {
    String historyId;
    String title;
    String message;
    String url;
    String regDate;

    /**
     * 履歴IDを取得する
     * @return historyId　履歴ID
     */
    public String getHistoryId() {
        return historyId;
    }

    /**
     * 履歴IDを設定する。
     * @param historyId 履歴ID
     */
    public void setHistoryId(String historyId) {
        this.historyId = historyId;
    }

    /**
     * 通知タイトルを取得する
     * @return title 通知タイトル
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * タイトルを設定する
     * @param title タイトル
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 通知メッセージを取得する
     * @return message 通知メッセージ
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * メッセージを設定する
     * @param message メッセージ
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * リッチ通知urlを取得する
     * @return url リッチ通知url
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * リッチ通知urlを設定する。
     * @param url リッチ通知url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 通知日時を取得する
     * @return 通知日時
     */
    public String getRegDate() {
        return regDate;
    }

    /**
     * 通知日時を設定する。
     * @param regDate 通知日時
     */
    public void setRegDate(String regDate) {
        this.regDate = regDate;
    }
}
