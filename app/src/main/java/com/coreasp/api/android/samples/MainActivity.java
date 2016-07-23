/**
 * MainActivity
 * CorePush-API-Android-Samples
 *
 * Copyright © 2016年 株式会社ブレスサービス. All rights reserved.
 */

package com.coreasp.api.android.samples;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * メイン画面のアクティビティ
 */
public class MainActivity extends ListActivity {

    // 一覧のアクティビティ情報の配列
    List<Map<String, Object>> activityMapList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setListAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, new ArrayList<String>()));

        // 一覧データの初期化
        initListData();

        // 通知から起動時に通知パラメータを取得
        handleNotificationLaunchParameter(getIntent());
    }

    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        // 画面遷移
        Map<String, Object> activityMap = activityMapList.get(position);
        Intent intent = new Intent(this, (Class) activityMap.get("class"));
        startActivity(intent);
    }

    /**
     * 一覧データを初期化する
     */
    public void initListData() {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) getListView().getAdapter();
        adapter.clear();

        Map<String, Object> tokenRegisterActivityMap = new HashMap();
        tokenRegisterActivityMap.put("name", "トークン登録");
        tokenRegisterActivityMap.put("class", TokenRegisterActivity.class);

        Map<String, Object> tokenUnregisterActivityMap = new HashMap();
        tokenUnregisterActivityMap.put("name", "トークン削除");
        tokenUnregisterActivityMap.put("class", TokenUnregisterActivity.class);

        Map<String, Object> historyActivityMap = new HashMap();
        historyActivityMap.put("name", "通知履歴");
        historyActivityMap.put("class", HistoryActivity.class);

        activityMapList.add(tokenRegisterActivityMap);
        activityMapList.add(tokenUnregisterActivityMap);
        activityMapList.add(historyActivityMap);

        for (int i = 0; i < activityMapList.size(); i++) {
            Map<String, Object> activityMap = activityMapList.get(i);
            adapter.add(activityMap.get("name").toString());
        }

        adapter.notifyDataSetChanged();
    }

    private void handleNotificationLaunchParameter(Intent intent) {
        // 通知から起動時に通知パラメータを取得
        CorePushAppManager manager = CorePushAppManager.getInstance();
        String date = manager.getDate(intent);
        String title = manager.getTitle(intent);
        String message = manager.getMessage(intent);
        String url = manager.getUrl(intent);
    }
}