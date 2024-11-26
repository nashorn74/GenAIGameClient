package com.omworldgame.guardianjourney.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.omworldgame.guardianjourney.utils.Config;
import com.omworldgame.guardianjourney.models.Item;
import com.omworldgame.guardianjourney.adapters.ItemAdapter;
import com.omworldgame.guardianjourney.R;

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

public class ProfileActivity extends AppCompatActivity {

    private TextView profileInfo;
    private RecyclerView itemRecyclerView;
    private ItemAdapter itemAdapter;
    private List<Item> itemList = new ArrayList<>();
    private String userId;
    private String token;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Intent에서 userId, token 받아옴
        userId = getIntent().getStringExtra("userId");
        token = getIntent().getStringExtra("token");

        // 사용자 정보 초기화
        initializeProfile();

        // RecyclerView 설정
        itemRecyclerView = findViewById(R.id.itemRecyclerView);
        itemRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemAdapter = new ItemAdapter(itemList, this);
        itemRecyclerView.setAdapter(itemAdapter);

        // 보유 아이템 정보 가져오기
        fetchItems();
    }

    private void initializeProfile() {
        profileInfo = findViewById(R.id.profileInfo);
        String userName = getIntent().getStringExtra("userName");
        int userGold = getIntent().getIntExtra("userGold", 0);
        int userHp = getIntent().getIntExtra("userHp", 0);
        int userMp = getIntent().getIntExtra("userMp", 0);
        int userLevel = getIntent().getIntExtra("userLevel", 0);
        int userExp = getIntent().getIntExtra("userExp", 0);
        int attackPoint = getIntent().getIntExtra("attackPoint", 0);
        int defencePoint = getIntent().getIntExtra("defencePoint", 0);

        String profileText = "이름: " + userName + "\n" +
                "금화: " + userGold + "\n" +
                "HP: " + userHp + "\n" +
                "MP: " + userMp + "\n" +
                "LV: " + userLevel + "\n" +
                "EXP: " + userExp + "\n" +
                "공격력: " + attackPoint + "\n" +
                "방어력: " + defencePoint;
        profileInfo.setText(profileText);
    }

    private void fetchItems() {
        executorService.execute(() -> {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(Config.GAME_SERVER_URL + "/api/users/" + userId + "/items");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Authorization", "Bearer " + token);
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
                    Log.e("fetchItems", "Failed to fetch items. Response code: " + responseCode);
                }
                urlConnection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }

            handler.post(() -> {
                if (result.length() > 0) {
                    try {
                        JSONArray itemsArray = new JSONArray(result.toString());
                        for (int i = 0; i < itemsArray.length(); i++) {
                            JSONObject itemObject = itemsArray.getJSONObject(i);
                            JSONObject itemDetails = itemObject.getJSONObject("itemId");
                            String itemId = itemDetails.getString("_id");
                            String itemName = itemDetails.getString("name");
                            int quantity = itemObject.getInt("quantity");
                            itemList.add(new Item(itemId, itemName, quantity));
                        }
                        itemAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(ProfileActivity.this, "아이템 정보를 파싱하는 중 오류 발생", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "보유한 아이템이 없습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // 닫기 버튼 클릭 시 호출되는 메서드
    public void onCloseButtonClicked(View view) {
        finish();  // 현재 액티비티 종료
    }
}