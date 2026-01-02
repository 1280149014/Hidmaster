package com.lq.hid1.dialog;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AlertDialog;

import com.lq.hid1.R;

public class BluetoothPromptDialog {

    private Dialog dialog;
    private Context context;
    private OnBluetoothActionListener listener;

    public interface OnBluetoothActionListener {
        void onEnableBluetooth();
        void onDismiss();
    }

    public BluetoothPromptDialog(Context context) {
        this.context = context;
    }

    public void setOnBluetoothActionListener(OnBluetoothActionListener listener) {
        this.listener = listener;
    }

    public void show() {
        dismiss(); // 关闭之前可能存在的弹框

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // 加载自定义布局
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_bluetooth_prompt, null);
        builder.setView(dialogView);

        // 设置弹框为可取消
        builder.setCancelable(true);

        // 创建弹框
        dialog = builder.create();

        // 初始化视图
        Button btnEnable = dialogView.findViewById(R.id.btn_enable);
        TextView tvTitle = dialogView.findViewById(R.id.tv_title);
        TextView tvMessage = dialogView.findViewById(R.id.tv_message);

        // 设置按钮点击事件
        btnEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onEnableBluetooth();
                }
                openBluetoothSettings();
                dismiss();
            }
        });

        // 设置弹框显示时的属性
        dialog.setOnShowListener(dialogInterface -> {
            Window window = dialog.getWindow();
            if (window != null) {
                // 设置透明背景
                window.setBackgroundDrawableResource(android.R.color.transparent);

                // 设置弹框位置在顶部
                window.setGravity(android.view.Gravity.TOP);

                // 设置弹框宽高
                android.view.WindowManager.LayoutParams layoutParams = window.getAttributes();
                layoutParams.width = android.view.WindowManager.LayoutParams.MATCH_PARENT;
                layoutParams.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
                window.setAttributes(layoutParams);

                // 添加动画
                window.setWindowAnimations(R.style.DialogTopAnimation);
            }
        });

        // 设置弹框关闭监听
        dialog.setOnCancelListener(dialogInterface -> {
            if (listener != null) {
                listener.onDismiss();
            }
        });

        // 显示弹框
        if (context instanceof Activity && !((Activity) context).isFinishing()) {
            dialog.show();
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private void openBluetoothSettings() {
        try {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            // 如果无法打开蓝牙设置，可以尝试其他方式
            try {
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        dialog = null;
    }

    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }

    public void setTitle(String title) {
        if (dialog != null && dialog.isShowing()) {
            TextView tvTitle = dialog.findViewById(R.id.tv_title);
            if (tvTitle != null) {
                tvTitle.setText(title);
            }
        }
    }

    public void setMessage(String message) {
        if (dialog != null && dialog.isShowing()) {
            TextView tvMessage = dialog.findViewById(R.id.tv_message);
            if (tvMessage != null) {
                tvMessage.setText(message);
            }
        }
    }

    public void setButtonText(String buttonText) {
        if (dialog != null && dialog.isShowing()) {
            Button btnEnable = dialog.findViewById(R.id.btn_enable);
            if (btnEnable != null) {
                btnEnable.setText(buttonText);
            }
        }
    }

    public void onDestroy() {
        dismiss();
        context = null;
        listener = null;
    }
}