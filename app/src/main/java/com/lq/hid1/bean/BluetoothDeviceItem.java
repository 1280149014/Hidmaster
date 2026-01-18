package com.lq.hid1.bean;

import android.bluetooth.BluetoothDevice;

public class BluetoothDeviceItem {
    private String deviceName;
    private String status;
    private boolean isSupported;
    public int imageResId;
    public BluetoothDevice device;

    // 设备类型常量
    public static final int DEVICE_TYPE_PHONE = 1;      // 手机
    public static final int DEVICE_TYPE_HEADSET = 2;    // 耳机
    public static final int DEVICE_TYPE_COMPUTER = 3;   // 电脑
    public static final int DEVICE_TYPE_CAR = 4;        // 车载设备
    public static final int DEVICE_TYPE_AUDIO = 5;      // 音频设备
    public static final int DEVICE_TYPE_OTHER = 0;      // 其他设备

    public BluetoothDeviceItem(String deviceName, String status,
                               boolean isSupported, int imageResId, BluetoothDevice device) {
        this.deviceName = deviceName;
        this.status = status;
        this.isSupported = isSupported;
        this.imageResId = imageResId;
        this.device = device;
    }

    public String getDeviceName() { return deviceName; }
    public String getStatus() { return status; }
    public boolean isSupported() { return isSupported; }

    public int getImageResId() {
        return imageResId;
    }

    public void setDeviceName(String name) { this.deviceName = name; }
    public void setStatus(String status) { this.status = status; }
    public void setSupported(boolean supported) { isSupported = supported; }
}
