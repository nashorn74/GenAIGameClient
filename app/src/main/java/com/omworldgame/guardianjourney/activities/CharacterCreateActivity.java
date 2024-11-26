package com.omworldgame.guardianjourney.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.omworldgame.guardianjourney.utils.CommonData;
import com.omworldgame.guardianjourney.utils.Config;
import com.omworldgame.guardianjourney.R;

import org.json.JSONObject;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class CharacterCreateActivity extends AppCompatActivity {

    private Spinner raceSpinner, jobSpinner;
    private TextView statsTextView;
    private EditText characterNameInput; // 캐릭터 이름 입력 필드
    private String userId, token;
    private int selectedGold, selectedHp, selectedMp, selectedAttack, selectedDefence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character_create);

        // Intent에서 받은 캐릭터 정보
        userId = getIntent().getStringExtra("userId");
        token = getIntent().getStringExtra("token");

        // UI 요소 초기화
        characterNameInput = findViewById(R.id.characterNameInput);
        raceSpinner = findViewById(R.id.raceSpinner);
        jobSpinner = findViewById(R.id.jobSpinner);
        statsTextView = findViewById(R.id.statsTextView);

        // 종족 스피너 설정
        ArrayAdapter<String> raceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, CommonData.RACES);
        raceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        raceSpinner.setAdapter(raceAdapter);

        // 직업 스피너 설정
        ArrayAdapter<String> jobAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, CommonData.JOBS);
        jobAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        jobSpinner.setAdapter(jobAdapter);

        // 직업 선택 시 스탯 자동 설정
        jobSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                int[] stats = CommonData.JOB_STATS[position];

                selectedGold = stats[0];
                selectedHp = stats[1];
                selectedMp = stats[2];
                selectedAttack = stats[3];
                selectedDefence = stats[4];

                // 스탯 정보를 텍스트뷰에 표시
                String statsText = "Gold: " + selectedGold +
                        "\nHP: " + selectedHp +
                        "\nMP: " + selectedMp +
                        "\nAttack: " + selectedAttack +
                        "\nDefence: " + selectedDefence;
                statsTextView.setText(statsText);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // 아무것도 선택되지 않았을 때
            }
        });
    }

    // 캐릭터 생성 버튼 클릭 시 호출
    public void onCreateCharacterButtonClicked(View view) {
        final int selectedRace = raceSpinner.getSelectedItemPosition(); // 종족 인덱스 (0부터 시작)
        final int selectedJob = jobSpinner.getSelectedItemPosition();   // 직업 인덱스 (0부터 시작)
        final String characterName = characterNameInput.getText().toString();

        if (characterName.isEmpty()) {
            Toast.makeText(this, "캐릭터 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(Config.GAME_SERVER_URL + "/api/users/" + userId + "/character");
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    urlConnection.setRequestProperty("Authorization", "Bearer " + token);
                    urlConnection.setDoOutput(true);

                    // JSON으로 캐릭터 생성 데이터 전송
                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("name", characterName);
                    jsonParam.put("job", selectedJob);
                    jsonParam.put("race", selectedRace);
                    jsonParam.put("gold", selectedGold);
                    jsonParam.put("hp", selectedHp);
                    jsonParam.put("mp", selectedMp);
                    jsonParam.put("attack_point", selectedAttack);
                    jsonParam.put("defence_point", selectedDefence);
                    jsonParam.put("exp", 0);      // 초기 경험치 설정
                    jsonParam.put("level", 1);    // 초기 레벨 설정

                    OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
                    out.write(jsonParam.toString());
                    out.flush();
                    out.close();

                    int responseCode = urlConnection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_CREATED) {
                        runOnUiThread(() -> {
                            Toast.makeText(CharacterCreateActivity.this, "캐릭터 생성 성공!", Toast.LENGTH_SHORT).show();

                            // LoginActivity로 돌아가고 autoNavigateToMain 플래그 설정
                            Intent intent = new Intent(CharacterCreateActivity.this, LoginActivity.class);
                            intent.putExtra("autoNavigateToMain", true);
                            intent.putExtra("userId", userId);
                            intent.putExtra("token", token);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();  // 생성 성공 시 현재 액티비티 종료
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(CharacterCreateActivity.this, "캐릭터 생성 실패: " + responseCode, Toast.LENGTH_SHORT).show();
                        });
                    }
                    urlConnection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        Toast.makeText(CharacterCreateActivity.this, "캐릭터 생성 중 오류 발생", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        }).start();
    }
}