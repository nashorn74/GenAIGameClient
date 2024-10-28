package com.omworldgame.guardianjourney;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private ImageView imageView;
    private int[] imageArray = {R.drawable.main01, R.drawable.main02, R.drawable.main03};  // 이미지 배열

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ImageView 설정
        imageView = findViewById(R.id.imageView); // activity_main.xml에 정의된 ImageView를 참조
        showRandomImage(); // 액티비티가 처음 시작될 때 이미지 랜덤으로 표시

        // bgm01_defenders_of_destiny.mp3 파일을 재생하기 위한 MediaPlayer 설정
        mediaPlayer = MediaPlayer.create(this, R.raw.bgm01_defenders_of_destiny);
        mediaPlayer.setLooping(true);  // 배경음악이 반복 재생되도록 설정
        mediaPlayer.start();  // 음악 재생 시작
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 액티비티가 화면에서 사라질 때 음악 일시 중지
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 액티비티가 다시 화면에 나타나면 음악 재개
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }

        // 액티비티가 다시 화면에 나타날 때마다 랜덤 이미지 표시
        showRandomImage();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 액티비티가 종료될 때 MediaPlayer 자원 해제
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    // 랜덤 이미지 표시 함수
    private void showRandomImage() {
        Random random = new Random();
        int randomIndex = random.nextInt(imageArray.length);  // 0, 1, 2 중 하나의 인덱스 반환
        imageView.setImageResource(imageArray[randomIndex]);  // 해당 인덱스의 이미지를 ImageView에 설정
    }

    // 로그인 버튼 클릭 시 호출될 함수 (XML에서 onClick으로 지정된 함수)
    public void onLoginButtonClicked(View view) {
        // LoginActivity로 이동
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}
