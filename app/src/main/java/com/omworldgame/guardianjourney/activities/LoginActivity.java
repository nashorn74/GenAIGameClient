package com.omworldgame.guardianjourney.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.omworldgame.guardianjourney.utils.Config;
import com.omworldgame.guardianjourney.R;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 이메일과 암호를 입력받는 EditText 참조
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        // 캐릭터 생성/수정 화면에서 돌아올 때 자동으로 메인 화면으로 이동
        boolean autoNavigateToMain = getIntent().getBooleanExtra("autoNavigateToMain", false);
        if (autoNavigateToMain) {
            String userId = getIntent().getStringExtra("userId");
            String token = getIntent().getStringExtra("token");
            navigateToMainGameActivity(userId, token);
        }
    }

    // 로그인 성공 후 게임 메인 화면으로 이동하는 메서드
    private void navigateToMainGameActivity(String userId, String token) {
        Intent intent = new Intent(LoginActivity.this, MainGameActivity.class);
        intent.putExtra("userId", userId);  // 사용자 ID를 넘김
        intent.putExtra("token", token);  // 인증 토큰을 넘김
        startActivity(intent);
        finish();  // 로그인 화면 종료
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public void onSignUpButtonClicked(View view) {
        Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivity(intent);  // 회원가입 화면으로 이동
    }

    public void loadCharacterInfo(String userId, String token) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 캐릭터 정보 GET 요청
                    URL url = new URL(Config.GAME_SERVER_URL + "/api/users/" + userId + "/character");
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setRequestProperty("Authorization", "Bearer " + token); // 토큰을 헤더에 추가

                    int responseCode = urlConnection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // 캐릭터 정보가 있을 경우
                        InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                        String response = convertStreamToString(inputStream);  // 응답을 문자열로 변환
                        Log.d("CharacterResponse", response);  // 응답을 로그로 출력

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 캐릭터 수정 화면으로 이동
                                Intent intent = new Intent(LoginActivity.this, CharacterEditActivity.class);
                                intent.putExtra("characterData", response);  // 캐릭터 데이터를 넘김
                                intent.putExtra("userId", userId);  // 사용자 ID를 넘김
                                intent.putExtra("token", token);  // 인증 토큰을 넘김
                                startActivity(intent);
                            }
                        });
                    } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                        // 캐릭터 정보가 없을 경우
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 캐릭터 생성 화면으로 이동
                                Intent intent = new Intent(LoginActivity.this, CharacterCreateActivity.class);
                                intent.putExtra("userId", userId);  // 사용자 ID를 넘김
                                intent.putExtra("token", token);  // 인증 토큰을 넘김
                                startActivity(intent);
                            }
                        });
                    } else {
                        // 기타 오류 처리
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoginActivity.this, "캐릭터 정보를 가져오는 중 오류 발생: " + responseCode, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    urlConnection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, "캐릭터 정보를 가져오는 중 오류 발생", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    // 로그인 버튼 클릭 시 호출될 메서드
    public void onLoginButtonClicked(View view) {
        final String email = emailEditText.getText().toString().trim();
        final String password = passwordEditText.getText().toString().trim();

        // 이메일과 암호가 비어 있으면 알림 표시
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(LoginActivity.this, "이메일과 암호를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // API 호출을 위한 스레드
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 로그인 API 요청 URL
                    URL url = new URL(Config.GAME_SERVER_URL + "/api/auth/login");
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    urlConnection.setRequestProperty("Accept", "*/*");
                    urlConnection.setDoOutput(true);

                    // JSON으로 이메일과 암호를 전송
                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("email", email);
                    jsonParam.put("password", password);

                    // 디버깅 로그 출력
                    Log.d("LoginRequest", "Request JSON: " + jsonParam.toString());

                    OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
                    out.write(jsonParam.toString());
                    out.flush();
                    out.close();

                    int responseCode = urlConnection.getResponseCode();
                    InputStream inputStream;

                    if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                        inputStream = new BufferedInputStream(urlConnection.getInputStream());
                        String response = convertStreamToString(inputStream);  // 응답 스트림을 문자열로 변환
                        Log.d("Response", response);  // 응답 내용을 로그로 출력

                        // JSON 응답에서 userId와 token을 추출
                        JSONObject responseJson = new JSONObject(response);
                        String userId = responseJson.getJSONObject("user").getString("_id");
                        String token = responseJson.getString("token");

                        Log.d("LoginResult", "UserID: " + userId + ", Token: " + token);  // 로그에 userId와 token 출력

                        // 로그인 성공 처리
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoginActivity.this, "로그인 성공!", Toast.LENGTH_SHORT).show();

                                loadCharacterInfo(userId, token);
                            }
                        });
                    } else {
                        // 로그인 실패 처리
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoginActivity.this, "로그인 실패: "+responseCode, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    urlConnection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, "로그인 중 오류 발생", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }
}