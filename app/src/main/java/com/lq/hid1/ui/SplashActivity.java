package com.lq.hid1.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.lq.hid1.R;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 必须在super.onCreate()之前设置，否则会闪屏
        //setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        // 启动页逻辑（延迟跳转或直接跳转）
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}