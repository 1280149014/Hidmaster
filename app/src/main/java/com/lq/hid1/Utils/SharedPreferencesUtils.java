package com.lq.hid1.Utils;

import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

public class SharedPreferencesUtils {

    public static void saveLastDevice(Application context, BluetoothDevice device) {
        SharedPreferences sp = context.getSharedPreferences("mysp", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("device", device.getAddress());
        editor.apply();
    }

    public static String getLastDevice(Application context) {
        SharedPreferences sp = context.getSharedPreferences("mysp", Context.MODE_PRIVATE);
        return sp.getString("device", "");
    }

    public static Map<String, String> read(Application context) {
        Map<String, String> data = new HashMap<String, String>();
        SharedPreferences sp = context.getSharedPreferences("mysp", Context.MODE_PRIVATE);
        data.put("username", sp.getString("username", ""));
        return data;
    }
}
