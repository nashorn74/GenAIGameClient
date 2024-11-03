package com.omworldgame.guardianjourney;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextView profileInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Intent에서 사용자 정보를 받아옴
        String userName = getIntent().getStringExtra("userName");
        int userGold = getIntent().getIntExtra("userGold", 0);
        int userHp = getIntent().getIntExtra("userHp", 0);
        int userMp = getIntent().getIntExtra("userMp", 0);
        int userLevel = getIntent().getIntExtra("userLevel", 0);
        int userExp = getIntent().getIntExtra("userExp", 0);
        int attackPoint = getIntent().getIntExtra("attackPoint", 0);
        int defencePoint = getIntent().getIntExtra("defencePoint", 0);

        // UI 요소 참조
        profileInfo = findViewById(R.id.profileInfo);

        // 사용자 정보 출력
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

    // 닫기 버튼 클릭 시 호출되는 메서드
    public void onCloseButtonClicked(View view) {
        finish();  // 현재 액티비티 종료
    }
}