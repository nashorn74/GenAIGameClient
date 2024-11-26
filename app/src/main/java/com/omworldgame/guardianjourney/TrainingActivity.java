package com.omworldgame.guardianjourney;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class TrainingActivity extends AppCompatActivity {

    private String userId;
    private String token;

    private Jedis jedis;
    private JedisPubSub jedisPubSub;
    private Executor executor;
    private Handler uiHandler;

    private ProgressBar progressBar;
    private TextView statusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_training);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Intent에서 userId 및 token을 받아옴
        userId = getIntent().getStringExtra("userId");
        token = getIntent().getStringExtra("token");

        // UI 요소 참조
        progressBar = findViewById(R.id.trainingProgressBar);
        statusTextView = findViewById(R.id.statusTextView);

        uiHandler = new Handler(Looper.getMainLooper());
        executor = Executors.newSingleThreadExecutor();

        // Redis 연결 및 구독
        connectToRedis();

        // 육성 시작 요청
        startTraining();
    }

    private void connectToRedis() {
        new Thread(() -> {
            try {
                jedis = new Jedis(Config.REDIS_HOST, Config.REDIS_PORT);

                // 사용자별 채널 구독
                String channelName = "training:" + userId;
                jedisPubSub = new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        uiHandler.post(() -> handleTrainingMessage(message));
                    }
                };
                jedis.subscribe(jedisPubSub, channelName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void startTraining() {
        executor.execute(() -> {
            try {
                // 육성 시작 API 호출
                URL url = new URL(Config.GAME_SERVER_URL + "/api/users/" + userId + "/character/train");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "*/*");
                urlConnection.setDoOutput(true);
                urlConnection.connect();

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                    uiHandler.post(() -> statusTextView.setText(R.string.training_started));
                } else {
                    uiHandler.post(() -> Toast.makeText(TrainingActivity.this, R.string.training_failed, Toast.LENGTH_SHORT).show());
                    finish();
                }
                urlConnection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                uiHandler.post(() -> {
                    Toast.makeText(TrainingActivity.this, R.string.training_error, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void handleTrainingMessage(String message) {
        try {
            JSONObject json = new JSONObject(message);
            String type = json.getString("type");

            switch (type) {
                case "progress":
                    int progress = json.getInt("value");
                    progressBar.setProgress(progress);
                    String progressText = getString(R.string.training_progress, progress);
                    statusTextView.setText(progressText);
                    break;
                case "result":
                    boolean success = json.getBoolean("success");
                    if (success) {
                        JSONObject oldData = json.getJSONObject("oldData");
                        JSONObject newData = json.getJSONObject("newData");

                        String resultText = getString(
                                R.string.training_success,
                                oldData.getInt("level"),
                                newData.getInt("level"),
                                oldData.getInt("exp"),
                                newData.getInt("exp"),
                                oldData.getInt("attack_point"),
                                newData.getInt("attack_point"),
                                oldData.getInt("defence_point"),
                                newData.getInt("defence_point")
                        );
                        statusTextView.setText(resultText);
                    } else {
                        statusTextView.setText(R.string.training_failed);
                    }
                    Button closeButton = findViewById(R.id.closeButton);
                    closeButton.setVisibility(TextView.VISIBLE);
                    disconnectRedis();
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void disconnectRedis() {
        executor.execute(() -> {
            try {
                if (jedisPubSub != null) {
                    jedisPubSub.unsubscribe();
                }
                if (jedis != null) {
                    jedis.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectRedis();
    }

    public void onCloseButtonClicked(View view) {
        finish();
    }
}