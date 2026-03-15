package com.omniconverter.app.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.omniconverter.app.R;
import com.omniconverter.app.MainActivity;

public class SplashActivity extends AppCompatActivity {
    private static final long SPLASH_DISPLAY_LENGTH = 3000; // 3 seconds
    private static final long LOGO_DELAY = 0;
    private static final long TEXT_DELAY = 500;
    private static final long PROGRESS_DELAY = 1200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize views
        ImageView logoBadge = findViewById(R.id.logo_badge_bg);
        ImageView conversionIcon = findViewById(R.id.conversion_icon);
        View glowEffect = findViewById(R.id.glow_effect);
        TextView appName = findViewById(R.id.app_name);
        TextView tagline = findViewById(R.id.tagline);

        // Load animations
        Animation logoEnter = AnimationUtils.loadAnimation(this, R.anim.splash_logo_enter);
        Animation textEnter = AnimationUtils.loadAnimation(this, R.anim.splash_text_enter);
        Animation rotate = AnimationUtils.loadAnimation(this, R.anim.splash_rotate);

        // Apply animations with delays
        new Handler(Looper.getMainLooper()).postDelayed(
                () -> {
                    logoBadge.startAnimation(logoEnter);
                    glowEffect.startAnimation(logoEnter);
                },
                LOGO_DELAY
        );

        new Handler(Looper.getMainLooper()).postDelayed(
                () -> conversionIcon.startAnimation(rotate),
                LOGO_DELAY + 200
        );

        new Handler(Looper.getMainLooper()).postDelayed(
                () -> {
                    appName.startAnimation(textEnter);
                },
                TEXT_DELAY
        );

        new Handler(Looper.getMainLooper()).postDelayed(
                () -> tagline.startAnimation(textEnter),
                TEXT_DELAY + 150
        );

        // Transition to MainActivity after splash duration
        new Handler(Looper.getMainLooper()).postDelayed(
                this::transitionToMainActivity,
                SPLASH_DISPLAY_LENGTH
        );
    }

    private void transitionToMainActivity() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
        // Apply smooth fade transition
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
