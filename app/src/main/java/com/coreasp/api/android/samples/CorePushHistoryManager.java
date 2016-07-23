/**
 * CorePushHistoryManager
 * CorePush-API-Android-Samples
 *
 * Copyright © 2016年 株式会社ブレスサービス. All rights reserved.
 */

package com.coreasp.api.android.samples;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * 通知履歴取得の通信マネジャークラス。
 * AsyncTaskを継承。
 */
public class CorePushHistoryManager extends AsyncTask<Void, Integer, Boolean> implements OnCancelListener {

    private static final String LOG_TAG = "COREPUSH";
    private Context mContext;
    private CorePushHistoryManagerListener listener;
    private ProgressDialog dialog;
    private boolean isProgressDialog = false;
    private String progressMessage = "読み込み中...";
    private static ArrayList<CorePushHistoryModel> notificationHistoryModelList = new ArrayList<CorePushHistoryModel>();
    private static final String CORE_PUSH_NOTIFY_HISTORY_API = "https://api.core-asp.com/notify_history.php";

    /**
     * コンストラクタ
     * @param context コンストラクタ
     */
    public CorePushHistoryManager(Context context) {
        mContext = context;
    }

    /**
     * CorePushHistoryManagerListerを実装したクラスを設定する。
     * 通知履歴の取得が成功した場合は CorePushHistoryManagerListener#notificationHistoryManagerSuccess が呼ばれる。
     * 通知履歴の取得が失敗した場合は CorePushHistoryManagerListener#notificationHistoryManagerFailが呼ばれる。
     * @param listener CorePushHistoryManagerListerを実装したクラス
     */
    public void setListener(CorePushHistoryManagerListener listener) {
        this.listener = listener;
    }

    /**
     * プログレスダイアログのメッセージを設定する。メッセージを設定しない場合は 「読み込み中...」がデフォルトメッセージとして設定される。
     * @param progressMessage プログレスダイアログのメッセージ
     */
    public void setProgressMessage(String progressMessage) {
        this.progressMessage = progressMessage;
    }

    /**
     * 通知履歴の配列を取得する。配列は CorePushNotificationHistoryModelの形式で格納される。
     * @return notificationHistoryModel 履歴モデル
     */
    public static ArrayList<CorePushHistoryModel> getNotificationHistoryModelList() {
        return notificationHistoryModelList;
    }

    @Override
    protected void onPreExecute() {

        if (isProgressDialog) {
            dialog = new ProgressDialog(mContext);
            dialog.setMessage(this.progressMessage);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCancelable(true);
            dialog.setOnCancelListener(this);
            dialog.show();
        }
    }

    /**
     * プログレスダイアログを表示するか判定する
     * @return プログレスダイアログの表示有無
     */
    public boolean isProgressDialog() {
        return this.isProgressDialog;
    }

    /**
     * プログレスダイアログの表示を設定する。
     * プログレスダイアログを表示する場合は true を設定し、表示しない場合は false を設定する。
     * @param isProgressDialog プログレスダイアログの表示有無
     */
    public void setProgressDialog(boolean isProgressDialog) {
        this.isProgressDialog = isProgressDialog;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        HashMap<String, String> postDataParams = new HashMap<>();
        postDataParams.put("config_type", "2");
        CorePushAppManager manager = CorePushAppManager.getInstance();
        postDataParams.put("config_key", manager.getConfigKey());

        String jsonText = "";
        URL url;
        HttpURLConnection connection = null;
        try {
            url = new URL(CORE_PUSH_NOTIFY_HISTORY_API);
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
                jsonText = "";
            }
        } catch (UnsupportedEncodingException e) {
            return false;
        } catch (IOException e) {
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
                ;
            }
        }

        // jsonのテキストからJSONオブジェクトを生成
        try {
            JSONObject jsonObject = new JSONObject(jsonText);
            String status = jsonObject.getString("status");

            if (status.equalsIgnoreCase("0")) {
                notificationHistoryModelList.clear();
                JSONArray jsonArray = jsonObject.getJSONArray("results");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject result = jsonArray.getJSONObject(i);
                    String historyId = result.getString("history_id");
                    String title = result.getString("title");
                    String message = result.getString("message");
                    String urlStr = result.getString("url");
                    if (urlStr != null && urlStr.equals("null")) {
                        urlStr = null;
                    }

                    String regDate = result.getString("reg_date");

                    CorePushHistoryModel model = new CorePushHistoryModel();
                    model.setHistoryId(historyId);
                    model.setTitle(title);
                    model.setMessage(message);
                    model.setUrl(urlStr);
                    model.setRegDate(regDate);
                    notificationHistoryModelList.add(model);
                }

                return true;
            } else {
                return false;
            }

        } catch (JSONException e) {
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {

        if (dialog != null && dialog.isShowing()) {
            try {
                dialog.dismiss();
            } catch (Exception e) {
            }
        }

        if (success) {
            if (this.listener != null) {
                this.listener.historyManagerSuccess();
            }
        } else {
            if (this.listener != null) {
                this.listener.historyManagerFail();
            }
        }
    }

    @Override
    protected void onCancelled() {
        if (dialog != null && dialog.isShowing()) {
            try {
                dialog.dismiss();
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void onCancel(DialogInterface arg0) {
        this.cancel(true);
    }

}