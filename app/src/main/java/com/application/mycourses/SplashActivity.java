package com.application.mycourses;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import com.application.mycourses.sign.SignInActivity;

public class SplashActivity extends AppCompatActivity {

    private PackageInfo packageInfo;

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TextView tvAppVersion = findViewById(R.id.tv_app_version);

        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(),0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        tvAppVersion.setText(String.format("%d.%s",packageInfo.versionCode, packageInfo.versionName));

        Thread thread = new Thread(){

            @Override
            public void run() {
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    startActivity(new Intent(SplashActivity.this, SignInActivity.class));
                    overridePendingTransition(R.anim.anim_fade_in,R.anim.anim_fade_out);
                    finish();
                }
            }
        };
        thread.start();
    }
}