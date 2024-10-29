package com.omworldgame.guardianjourney;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
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

public class SignUpActivity extends AppCompatActivity {

    private EditText firstNameEditText, lastNameEditText, emailEditText, passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // EditText 설정
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
    }

    // 회원가입 버튼 클릭 시 호출될 함수
    public void onSignUpButtonClicked(View view) {
        final String firstName = firstNameEditText.getText().toString().trim();
        final String lastName = lastNameEditText.getText().toString().trim();
        final String email = emailEditText.getText().toString().trim();
        final String password = passwordEditText.getText().toString().trim();

        // 필수 필드가 비어있을 때 처리
        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) ||
                TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(SignUpActivity.this, "모든 필드를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // API 호출을 위한 스레드
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 회원가입 API 요청 URL
                    URL url = new URL("http://192.168.0.203:3000/api/auth/register");
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    urlConnection.setRequestProperty("Accept", "*/*");
                    urlConnection.setDoOutput(true);

                    // JSON으로 firstName, lastName, 이메일과 암호를 전송
                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("firstName", firstName);
                    jsonParam.put("lastName", lastName);
                    jsonParam.put("email", email);
                    jsonParam.put("password", password);

                    OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
                    out.write(jsonParam.toString());
                    out.flush();
                    out.close();

                    int responseCode = urlConnection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_CREATED) {
                        // 회원가입 성공 처리
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SignUpActivity.this, "회원가입 성공!", Toast.LENGTH_SHORT).show();
                                finish(); // 회원가입 성공 시 현재 액티비티를 종료하고 LoginActivity로 돌아감
                            }
                        });
                    } else {
                        // 회원가입 실패 처리
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SignUpActivity.this, "회원가입 실패: " + responseCode, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    urlConnection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SignUpActivity.this, "회원가입 중 오류 발생", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }
}