package com.lq.hid1.Utils;

// 设备类型判断和图标映射工具类
import android.Manifest;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;

import androidx.annotation.RequiresPermission;

import com.lq.hid1.R;
import com.lq.hid1.bean.BluetoothDeviceItem;

public class DeviceTypeUtils {

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public static int getDeviceType(BluetoothDevice device) {
        if (device == null || device.getBluetoothClass() == null) {
            return BluetoothDeviceItem.DEVICE_TYPE_OTHER;
        }

        BluetoothClass bluetoothClass = device.getBluetoothClass();
        int majorClass = bluetoothClass.getMajorDeviceClass();
        int deviceClass = bluetoothClass.getDeviceClass();

        // 根据主要设备类别判断
        switch (majorClass) {
            case BluetoothClass.Device.Major.PHONE:
                return BluetoothDeviceItem.DEVICE_TYPE_PHONE;

            case BluetoothClass.Device.Major.AUDIO_VIDEO:
                // 进一步判断音频设备的具体类型
                switch (deviceClass) {
                    case BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET:
                    case BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE:
                    case BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES:
                    case BluetoothClass.Device.AUDIO_VIDEO_PORTABLE_AUDIO:
                    case BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO:
                        return BluetoothDeviceItem.DEVICE_TYPE_HEADSET;

                    case BluetoothClass.Device.AUDIO_VIDEO_HIFI_AUDIO:
                    case BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER:
                    case BluetoothClass.Device.AUDIO_VIDEO_MICROPHONE:
                        return BluetoothDeviceItem.DEVICE_TYPE_AUDIO;
                }
                return BluetoothDeviceItem.DEVICE_TYPE_AUDIO;

            case BluetoothClass.Device.Major.COMPUTER:
                return BluetoothDeviceItem.DEVICE_TYPE_COMPUTER;

            case BluetoothClass.Device.Major.IMAGING:
            case BluetoothClass.Device.Major.NETWORKING:
                return BluetoothDeviceItem.DEVICE_TYPE_COMPUTER;

            case BluetoothClass.Device.Major.HEALTH:
            case BluetoothClass.Device.Major.PERIPHERAL:
            case BluetoothClass.Device.Major.WEARABLE:
            case BluetoothClass.Device.Major.TOY:
            case BluetoothClass.Device.Major.UNCATEGORIZED:
            default:
                return BluetoothDeviceItem.DEVICE_TYPE_OTHER;
        }
    }

    public static int getDeviceIconResId(int deviceType) {
        switch (deviceType) {
            case BluetoothDeviceItem.DEVICE_TYPE_PHONE:
                return R.drawable.ic_phone_round;  // 手机图标

            case BluetoothDeviceItem.DEVICE_TYPE_HEADSET:
                return R.drawable.ic_headset;  // 耳机图标

            case BluetoothDeviceItem.DEVICE_TYPE_COMPUTER:
                return R.drawable.ic_computer;  // 电脑图标

            case BluetoothDeviceItem.DEVICE_TYPE_AUDIO:
                return R.drawable.ic_speaker;  // 音频设备图标

            case BluetoothDeviceItem.DEVICE_TYPE_CAR:
                return R.drawable.ic_car;  // 车载设备图标

            default:
                return R.drawable.ic_bluetooth_round;  // 默认蓝牙图标
        }
    }

    public static String getDeviceTypeDescription(int deviceType) {
        switch (deviceType) {
            case BluetoothDeviceItem.DEVICE_TYPE_PHONE:
                return "Phone";
            case BluetoothDeviceItem.DEVICE_TYPE_HEADSET:
                return "Headset";
            case BluetoothDeviceItem.DEVICE_TYPE_COMPUTER:
                return "Computer";
            case BluetoothDeviceItem.DEVICE_TYPE_AUDIO:
                return "Audio Device";
            case BluetoothDeviceItem.DEVICE_TYPE_CAR:
                return "Car Device";
            default:
                return "Bluetooth Device";
        }
    }

    // 判断设备是否可能不支持
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public static boolean isProbablySupported(BluetoothDevice device) {
        if (device == null || device.getBluetoothClass() == null) {
            return true;
        }

        int majorClass = device.getBluetoothClass().getMajorDeviceClass();
        int deviceClass = device.getBluetoothClass().getDeviceClass();

        // 一些常见不支持的设备类型
        switch (majorClass) {
            case BluetoothClass.Device.Major.HEALTH:
                return false;

            case BluetoothClass.Device.Major.AUDIO_VIDEO:
                // 某些特殊音频设备可能不支持
                if (deviceClass == BluetoothClass.Device.AUDIO_VIDEO_VIDEO_DISPLAY_AND_LOUDSPEAKER) {
                    return false;
                }
                return true;

            default:
                return true;
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public static String getConnectionStatus(BluetoothDevice device) {
        boolean isSupported = isProbablySupported(device);
        if (isSupported) {
            return "Connection should be possible";
        } else {
            return "Probably not supported";
        }
    }
}