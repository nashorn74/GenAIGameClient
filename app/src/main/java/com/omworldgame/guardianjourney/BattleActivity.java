package com.omworldgame.guardianjourney;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BattleActivity extends AppCompatActivity {

    private String userId;
    private String token;

    private Jedis jedis;
    private JedisPubSub jedisPubSub;

    private ProgressBar characterHpBar;
    private ProgressBar monsterHpBar;
    private TextView characterHpText;
    private TextView monsterHpText;
    private TextView battleLogTextView;

    private Handler uiHandler;
    private Executor executor;
    private ExecutorService subscribeExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_battle);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userId = getIntent().getStringExtra("userId");
        token = getIntent().getStringExtra("token");

        characterHpBar = findViewById(R.id.characterHpBar);
        monsterHpBar = findViewById(R.id.monsterHpBar);
        characterHpText = findViewById(R.id.characterHpText);
        monsterHpText = findViewById(R.id.monsterHpText);
        battleLogTextView = findViewById(R.id.battleLogTextView);

        uiHandler = new Handler(Looper.getMainLooper());
        executor = Executors.newSingleThreadExecutor();
        subscribeExecutor = Executors.newSingleThreadExecutor();

        // Redis 연결 및 구독
        connectToRedis();

        // 전투 시작 요청
        startBattle();
    }

    private void connectToRedis() {
        subscribeExecutor.execute(() -> {
            try {
                jedis = new Jedis("192.168.0.203", 6379);
                // 필요한 경우 Redis 비밀번호 인증
                // jedis.auth("YOUR_REDIS_PASSWORD");

                String channelName = "battle:" + userId;

                // JedisPubSub 객체 생성 및 참조 유지
                jedisPubSub = new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        if (channel.equals(channelName)) {
                            uiHandler.post(() -> handleBattleMessage(message));
                        }
                    }
                };

                // 채널 구독
                jedis.subscribe(jedisPubSub, channelName);

            } catch (Exception e) {
                e.printStackTrace();
                uiHandler.post(() -> Toast.makeText(BattleActivity.this, "Redis 연결 실패", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void startBattle() {
        executor.execute(() -> {
            try {
                // 전투 시작 API 호출
                URL url = new URL("http://192.168.0.203:3000/api/users/" + userId + "/character/battle");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Authorization", "Bearer " + token);
                urlConnection.connect();

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                    uiHandler.post(() -> battleLogTextView.setText("전투 시작"));
                } else {
                    uiHandler.post(() -> Toast.makeText(BattleActivity.this, "전투 시작 실패", Toast.LENGTH_SHORT).show());
                    finish();
                }
                urlConnection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                uiHandler.post(() -> {
                    Toast.makeText(BattleActivity.this, "전투 시작 중 오류 발생", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void handleBattleMessage(String message) {
        try {
            JSONObject json = new JSONObject(message);
            String type = json.getString("type");

            switch (type) {
                case "battle_start":
                    int characterMaxHp = json.getInt("characterMaxHp");
                    int monsterMaxHp = json.getInt("monsterMaxHp");
                    String monsterName = json.getString("monsterName"); // 몬스터 이름 가져오기

                    characterHpBar.setMax(characterMaxHp);
                    monsterHpBar.setMax(monsterMaxHp);

                    characterHpBar.setProgress(characterMaxHp);
                    monsterHpBar.setProgress(monsterMaxHp);

                    characterHpText.setText(getString(R.string.character_hp, characterMaxHp, characterMaxHp));
                    monsterHpText.setText(getString(R.string.monster_hp, monsterMaxHp, monsterMaxHp));

                    battleLogTextView.append("\n" + getString(R.string.battle_start_with_monster, monsterName));
                    break;

                case "update":
                    int characterHp = json.getInt("characterHp");
                    int monsterHp = json.getInt("monsterHp");
                    String log = json.getString("log");

                    characterHpBar.setProgress(characterHp);
                    monsterHpBar.setProgress(monsterHp);

                    characterHpText.setText(getString(R.string.character_hp, characterHp, characterHpBar.getMax()));
                    monsterHpText.setText(getString(R.string.monster_hp, monsterHp, monsterHpBar.getMax()));

                    battleLogTextView.append("\n" + log);
                    break;

                case "battle_end":
                    boolean isWin = json.getBoolean("win");
                    if (isWin) {
                        battleLogTextView.append("\n" + getString(R.string.battle_win));
                        int expGained = json.getInt("expGained");
                        int goldGained = json.getInt("goldGained");
                        battleLogTextView.append("\n" + getString(R.string.exp_gained, expGained));
                        battleLogTextView.append("\n" + getString(R.string.gold_gained, goldGained));
                    } else {
                        battleLogTextView.append("\n" + getString(R.string.battle_loss));
                    }
                    break;

                default:
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void disconnectRedis() {
        if (jedisPubSub != null) {
            subscribeExecutor.execute(() -> {
                try {
                    jedisPubSub.unsubscribe();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (jedis != null) {
                        try {
                            jedis.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectRedis();
        if (subscribeExecutor != null && !subscribeExecutor.isShutdown()) {
            subscribeExecutor.shutdown();
        }
    }
}