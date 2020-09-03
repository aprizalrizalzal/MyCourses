package com.application.mycourses.ui.main.courses;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.application.mycourses.R;

public class JoinActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.anim_fade_in,R.anim.anim_fade_out);
    }
}