/**
 * CorePushUtil
 * CorePush-API-Android-Samples
 *
 * Copyright © 2016年 株式会社ブレスサービス. All rights reserved.
 */

package com.coreasp.api.android.samples;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * ユーティリティクラス
 */
public class CorePushUtil {

    /**
     *
     * リクエストパラメータのハッシュからPOSTデータのHTTPBdoyの文字列を構築する
     *
     * @param params
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first) {
                first = false;
            } else {
                result.append("&");
            }

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

}
