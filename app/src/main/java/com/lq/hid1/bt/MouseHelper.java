package com.lq.hid1.bt;

import android.annotation.SuppressLint;
import android.util.Log;

public class MouseHelper {
    private static final String TAG = MouseHelper.class.getSimpleName();
    public @interface MouseButton {
        int LEFT = 0;
        int RIGHT = 1;
        int MIDDLE = 2;
    }

    @SuppressLint("MissingPermission")
    public static boolean sendData(boolean left, boolean right, boolean middle, int x, int y, int wheel) {
        if (BluetoothHidService.bluetoothHidDevice != null && BluetoothHidService.isHidDeviceConnected) {
            return BluetoothHidService.bluetoothHidDevice.sendReport(BluetoothHidService.bluetoothDevice, Constants.ID_MOUSE, MouseReport.getReport(left, right, middle, x, y, wheel));
        } else {
            Log.d(TAG, "hid is not connected");
        }
        return false;
    }
}
