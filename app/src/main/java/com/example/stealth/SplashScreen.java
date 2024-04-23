package com.example.stealth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class SplashScreen extends AppCompatActivity {
    VideoView videoView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        videoView = findViewById(R.id.videoView);
        MediaController mediaController = new MediaController(this);
        mediaController.setMediaPlayer(videoView);
        videoView.setMediaController(null);
        videoView.setVideoURI(Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.stealthanimationblack));
        videoView.start();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser != null && currentUser.isEmailVerified()) {
            if (BackendCommon.UserId == null) {
                BackendCommon.UserId = currentUser.getUid();
                BackendCommon.postsManager = new PostsManager(BackendCommon.UserId);
                BackendCommon.pollManager = new PollManager(BackendCommon.UserId);
                BackendCommon.myPosts = new MyPosts(BackendCommon.UserId);
                BackendCommon.myPoll = new MyPoll(BackendCommon.UserId);
            }
        }
        int time = 2500;//time in milliseconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent var = new Intent(SplashScreen.this, SignIn.class);
                startActivity(var);
                finish();
            }
        }, time);
    }
}