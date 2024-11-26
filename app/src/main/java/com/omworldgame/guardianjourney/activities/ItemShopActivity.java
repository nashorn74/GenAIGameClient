package com.omworldgame.guardianjourney.activities;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.omworldgame.guardianjourney.utils.Config;
import com.omworldgame.guardianjourney.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ItemShopActivity extends AppCompatActivity {

    private ImageView itemImage;
    private TextView itemDescription;
    private List<JSONObject> items = new ArrayList<>();
    private int currentIndex = 0;
    private String userId;  // 예시 사용자 ID
    private String token;  // 예시 토큰
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_shop);

        // Intent에서 userId, token 받아옴
        userId = getIntent().getStringExtra("userId");
        token = getIntent().getStringExtra("token");

        // UI 요소 참조
        itemImage = findViewById(R.id.itemImage);
        itemDescription = findViewById(R.id.itemDescription);

        // 아이템 목록 가져오기
        fetchItems();
    }

    // 아이템 목록을 가져오는 메서드
    private void fetchItems() {
        executorService.execute(() -> {
            String result = null;
            try {
                URL url = new URL(Config.GAME_SERVER_URL + "/api/items");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Authorization", "Bearer " + token);
                urlConnection.connect();

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    result = response.toString();
                } else {
                    Log.e("fetchItems", "Failed to fetch items. Response code: " + responseCode);
                }
                urlConnection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // UI 업데이트는 메인 스레드에서
            String finalResult = result;
            handler.post(() -> {
                if (finalResult != null) {
                    try {
                        JSONArray itemsArray = new JSONArray(finalResult);
                        for (int i = 0; i < itemsArray.length(); i++) {
                            items.add(itemsArray.getJSONObject(i));
                        }
                        displayCurrentItem();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(ItemShopActivity.this, "아이템 정보를 파싱하는 중 오류 발생", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ItemShopActivity.this, "아이템 정보를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // 현재 아이템 정보를 화면에 표시하는 메서드
    private void displayCurrentItem() {
        if (items.size() == 0) return;

        try {
            JSONObject item = items.get(currentIndex);
            String itemId = item.getString("_id");
            String name = item.getString("name");
            int price = item.getInt("price");
            int type = item.getInt("type");
            int attack = item.getInt("attack");
            int defence = item.getInt("defence");
            int hpRecovery = item.getInt("hp_recovery");
            int mpRecovery = item.getInt("mp_recovery");
            int tempAttack = item.getInt("temp_attack");
            int tempDefence = item.getInt("temp_defence");

            // 아이템 설명 설정
            StringBuilder description = new StringBuilder();
            description.append("이름: ").append(name).append("\n");
            description.append("가격: $").append(price / 100.0).append("\n");
            description.append("종류: ").append(getItemType(type)).append("\n");
            if (attack > 0) description.append("공격력: +").append(attack).append("\n");
            if (defence > 0) description.append("방어력: +").append(defence).append("\n");
            if (hpRecovery > 0) description.append("HP 회복: +").append(hpRecovery).append("\n");
            if (mpRecovery > 0) description.append("MP 회복: +").append(mpRecovery).append("\n");
            if (tempAttack > 0) description.append("공격력 일시 증가: +").append(tempAttack).append("\n");
            if (tempDefence > 0) description.append("방어력 일시 증가: +").append(tempDefence).append("\n");

            itemDescription.setText(description.toString());

            // 아이템 이미지 설정
            loadItemImage(itemId);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 아이템 종류를 문자열로 반환하는 메서드
    private String getItemType(int type) {
        switch (type) {
            case 0:
                return "공격용 아이템";
            case 1:
                return "방어용 아이템";
            case 2:
                return "회복용 아이템";
            default:
                return "기타 아이템";
        }
    }

    // 아이템 ID에 해당하는 이미지를 assets 폴더에서 로드하는 메서드
    private void loadItemImage(String itemId) {
        AssetManager assetManager = getAssets();
        try (InputStream inputStream = assetManager.open("items/" + itemId + ".jpg")) {
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            itemImage.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
            // 이미지가 없을 경우 기본 이미지 설정
            itemImage.setImageResource(R.drawable.default_item_image);
        }
    }

    // 이전 버튼 클릭 시
    public void onPrevButtonClicked(View view) {
        if (currentIndex > 0) {
            currentIndex--;
            displayCurrentItem();
        } else {
            Toast.makeText(this, "첫 번째 아이템입니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 다음 버튼 클릭 시
    public void onNextButtonClicked(View view) {
        if (currentIndex < items.size() - 1) {
            currentIndex++;
            displayCurrentItem();
        } else {
            Toast.makeText(this, "마지막 아이템입니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 구입 버튼 클릭 시
    public void onBuyButtonClicked(View view) {
        if (items.size() == 0) return;

        try {
            JSONObject item = items.get(currentIndex);
            String itemId = item.getString("_id");

            executorService.execute(() -> {
                boolean success = false;
                try {
                    URL url = new URL(Config.GAME_SERVER_URL + "/api/users/" + userId + "/items/" + itemId);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Authorization", "Bearer " + token);
                    urlConnection.connect();

                    int responseCode = urlConnection.getResponseCode();
                    success = responseCode == HttpURLConnection.HTTP_CREATED;

                    urlConnection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                boolean finalSuccess = success;
                handler.post(() -> {
                    if (finalSuccess) {
                        Toast.makeText(ItemShopActivity.this, "아이템을 구입했습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ItemShopActivity.this, "아이템 구입에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 취소 버튼 클릭 시
    public void onCancelButtonClicked(View view) {
        finish(); // 액티비티 종료
    }
}