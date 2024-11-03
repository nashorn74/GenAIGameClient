package com.omworldgame.guardianjourney;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NoticeActivity extends AppCompatActivity {

    private RecyclerView noticeRecyclerView;
    private NoticeAdapter noticeAdapter;
    private List<Notice> noticeList = new ArrayList<>();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);

        // RecyclerView 설정
        noticeRecyclerView = findViewById(R.id.noticeRecyclerView);
        noticeRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        noticeAdapter = new NoticeAdapter(noticeList, this);
        noticeRecyclerView.setAdapter(noticeAdapter);

        // 공지사항 정보 가져오기
        fetchNotices();
    }

    private void fetchNotices() {
        executorService.execute(() -> {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL("http://192.168.0.203:3000/api/notices");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    reader.close();
                } else {
                    Log.e("fetchNotices", "Failed to fetch notices. Response code: " + responseCode);
                }
                urlConnection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }

            handler.post(() -> {
                if (result.length() > 0) {
                    try {
                        JSONArray noticesArray = new JSONArray(result.toString());
                        for (int i = 0; i < noticesArray.length(); i++) {
                            JSONObject noticeObject = noticesArray.getJSONObject(i);
                            String id = noticeObject.getString("_id");
                            String title = noticeObject.getString("title");
                            String contents = noticeObject.getString("contents");
                            String createdAt = noticeObject.getString("createdAt");

                            noticeList.add(new Notice(id, title, contents, createdAt));
                        }
                        noticeAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(NoticeActivity.this, "공지사항을 파싱하는 중 오류 발생", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(NoticeActivity.this, "공지사항을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}