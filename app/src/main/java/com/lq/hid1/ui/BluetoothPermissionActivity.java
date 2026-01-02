package com.lq.hid1.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.lq.hid1.R;

public class BluetoothPermissionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_permission);

        // 初始化控件
        Button btnOpenSettings = findViewById(R.id.btn_open_settings);

        // 按钮点击事件：跳转到应用权限设置页面
        btnOpenSettings.setOnClickListener(v -> openAppPermissionSettings());
        findViewById(R.id.back).setOnClickListener(v-> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            finish();
        }
    }

    /**
     * 跳转到当前应用的权限设置页面
     */
    private void openAppPermissionSettings() {
        try {
            Intent intent = new Intent();
            // Android 8.0+ 跳转到应用权限设置
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
            } else {
                // 低版本跳转到应用信息页面
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            // 异常时跳转到系统设置首页
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            startActivity(intent);
        }
    }
}