package com.lq.hid1.bt;

import android.content.Intent;
import android.service.quicksettings.TileService;

import com.lq.hid1.ui.MainActivity;

public class BluetoothTileService extends TileService {

    @Override
    public void onClick() {
        try {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
