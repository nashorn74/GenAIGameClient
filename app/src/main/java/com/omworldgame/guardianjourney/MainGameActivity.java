package com.omworldgame.guardianjourney;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainGameActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private ImageView characterIcon;
    private TextView userInfo;

    private String userId;
    private String token;
    private String userName;
    private int userRace = 1;
    private int userHp;
    private int userMp;
    private int userGold;
    private int userLevel;
    private int userExp;
    private int attackPoint;
    private int defencePoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_game);

        // Intent에서 userId, token 및 사용자 이름 정보를 받아옴
        userId = getIntent().getStringExtra("userId");
        token = getIntent().getStringExtra("token");
        String firstName = getIntent().getStringExtra("firstName");
        String lastName = getIntent().getStringExtra("lastName");
        userName = firstName + " " + lastName;

        // 배경음악 설정 및 재생
        mediaPlayer = MediaPlayer.create(this, R.raw.bgm02_obsessions_veil);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        // UI 요소 참조
        characterIcon = findViewById(R.id.characterIcon);
        userInfo = findViewById(R.id.userInfo);

        // 사용자 이름 표시 (HP, MP, GOLD 정보는 조회 후 업데이트)
        userInfo.setText(userName);

        // 캐릭터 정보 조회
        fetchCharacterInfo();
    }

    // 공지사항 버튼 클릭 시
    public void onNoticeButtonClicked(View view) {
        Intent intent = new Intent(this, NoticeActivity.class);
        startActivity(intent);
    }

    // 아이템샵 버튼 클릭 시 호출되는 메서드
    public void onItemShopButtonClicked(View view) {
        // ItemShopActivity 실행
        Intent intent = new Intent(this, ItemShopActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("token", token);
        startActivity(intent);
    }

    // 프로필 버튼 onClick 속성에 연결된 메서드
    public void onProfileButtonClicked(View view) {
        Intent intent = new Intent(MainGameActivity.this, ProfileActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("token", token);
        intent.putExtra("userName", userName);
        intent.putExtra("userGold", userGold);
        intent.putExtra("userHp", userHp);
        intent.putExtra("userMp", userMp);
        intent.putExtra("userLevel", userLevel);
        intent.putExtra("userExp", userExp);
        intent.putExtra("attackPoint", attackPoint);
        intent.putExtra("defencePoint", defencePoint);
        startActivity(intent);
    }

    // 캐릭터 정보 조회 메서드
    private void fetchCharacterInfo() {
        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            String result = null;
            try {
                URL url = new URL("http://192.168.0.203:3000/api/users/" + userId + "/character");
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
                    Log.e("fetchCharacterInfo", "Failed to fetch character info. Response code: " + responseCode);
                }
                urlConnection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }

            String finalResult = result;
            handler.post(() -> {
                if (finalResult != null) {
                    try {
                        JSONObject characterJson = new JSONObject(finalResult);
                        userRace = characterJson.getInt("race");
                        userHp = characterJson.getInt("hp");
                        userMp = characterJson.getInt("mp");
                        userGold = characterJson.getInt("gold");
                        userLevel = characterJson.getInt("level");
                        userExp = characterJson.getInt("exp");
                        attackPoint = characterJson.getInt("attack_point");
                        defencePoint = characterJson.getInt("defence_point");

                        // 캐릭터 아이콘 설정 및 사용자 정보 표시
                        setCharacterIcon(userRace);
                        String userInfoText = userName + "\nHP: " + userHp + "\nMP: " + userMp + "\nGOLD: " + userGold;
                        userInfo.setText(userInfoText);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(MainGameActivity.this, "캐릭터 정보를 파싱하는 중 오류 발생", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainGameActivity.this, "캐릭터 정보를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // 종족에 따라 캐릭터 아이콘 설정 메서드
    private void setCharacterIcon(int race) {
        int iconResId;
        switch (race) {
            case 1:
                iconResId = R.drawable.char_icon1;
                break;
            case 2:
                iconResId = R.drawable.char_icon2;
                break;
            case 3:
                iconResId = R.drawable.char_icon3;
                break;
            case 4:
                iconResId = R.drawable.char_icon4;
                break;
            case 5:
                iconResId = R.drawable.char_icon5;
                break;
            case 6:
                iconResId = R.drawable.char_icon6;
                break;
            case 7:
                iconResId = R.drawable.char_icon7;
                break;
            case 8:
                iconResId = R.drawable.char_icon8;
                break;
            default:
                iconResId = R.drawable.char_icon1;
                break;
        }
        characterIcon.setImageResource(iconResId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}