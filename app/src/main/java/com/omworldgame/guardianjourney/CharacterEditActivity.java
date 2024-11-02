package com.omworldgame.guardianjourney;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONObject;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class CharacterEditActivity extends AppCompatActivity {

    private Spinner raceSpinner, jobSpinner;
    private TextView statsTextView;
    private String userId, token;
    private int selectedGold, selectedHp, selectedMp, selectedAttack, selectedDefence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character_edit);

        // Intent에서 받은 캐릭터 정보
        String characterData = getIntent().getStringExtra("characterData");
        userId = getIntent().getStringExtra("userId");
        token = getIntent().getStringExtra("token");

        // UI 설정
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

        // 기존 캐릭터 정보를 UI에 설정
        populateCharacterData(characterData);

        // 직업 선택 시 스탯 자동 변경
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

    // 캐릭터 데이터를 UI에 설정하는 함수
    private void populateCharacterData(String characterData) {
        try {
            JSONObject characterJson = new JSONObject(characterData);

            // 종족 및 스탯 설정
            int race = characterJson.getInt("race") - 1;  // 스피너 인덱스는 0부터 시작
            selectedGold = characterJson.getInt("gold");
            selectedHp = characterJson.getInt("hp");
            selectedMp = characterJson.getInt("mp");
            selectedAttack = characterJson.getInt("attack_point");
            selectedDefence = characterJson.getInt("defence_point");

            // 종족 스피너 설정
            raceSpinner.setSelection(race);

            // 서버에서 직업 정보가 없으면 스탯으로 직업 추정
            String job = CommonData.getJobByStats(selectedHp, selectedMp, selectedAttack, selectedDefence);
            if (!job.equals("알 수 없는 직업")) {
                jobSpinner.setSelection(java.util.Arrays.asList(CommonData.JOBS).indexOf(job));
            }

            // 스탯을 TextView에 설정
            String statsText = "Gold: " + selectedGold +
                    "\nHP: " + selectedHp +
                    "\nMP: " + selectedMp +
                    "\nAttack: " + selectedAttack +
                    "\nDefence: " + selectedDefence;
            statsTextView.setText(statsText);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "캐릭터 데이터를 로드하는 중 오류 발생", Toast.LENGTH_SHORT).show();
        }
    }

    // 캐릭터 수정 버튼 클릭 시 호출
    public void onEditCharacterButtonClicked(View view) {
        final int selectedRace = raceSpinner.getSelectedItemPosition() + 1;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://192.168.0.203:3000/api/users/" + userId + "/character");
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("PUT");
                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    urlConnection.setRequestProperty("Authorization", "Bearer " + token);
                    urlConnection.setDoOutput(true);

                    // JSON으로 캐릭터 수정 데이터 전송
                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("race", selectedRace);
                    jsonParam.put("gold", selectedGold);
                    jsonParam.put("hp", selectedHp);
                    jsonParam.put("mp", selectedMp);
                    jsonParam.put("attack_point", selectedAttack);
                    jsonParam.put("defence_point", selectedDefence);

                    OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
                    out.write(jsonParam.toString());
                    out.flush();
                    out.close();

                    int responseCode = urlConnection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        runOnUiThread(() -> {
                            Toast.makeText(CharacterEditActivity.this, "캐릭터 수정 성공!", Toast.LENGTH_SHORT).show();
                            finish();  // 수정 성공 시 액티비티 종료
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(CharacterEditActivity.this, "캐릭터 수정 실패: " + responseCode, Toast.LENGTH_SHORT).show();
                        });
                    }
                    urlConnection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        Toast.makeText(CharacterEditActivity.this, "캐릭터 수정 중 오류 발생", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        }).start();
    }
}