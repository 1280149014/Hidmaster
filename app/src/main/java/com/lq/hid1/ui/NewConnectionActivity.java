package com.lq.hid1.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lq.hid1.R;
import com.lq.hid1.Utils.DeviceTypeUtils;
import com.lq.hid1.Utils.SharedPreferencesUtils;
import com.lq.hid1.adapter.BluetoothDeviceAdapter;
import com.lq.hid1.bean.BluetoothDeviceItem;
import com.lq.hid1.bt.BluetoothHidService;

import java.util.ArrayList;
import java.util.Set;

public class NewConnectionActivity extends AppCompatActivity {
    private BluetoothAdapter bluetoothAdapter;
    private final ArrayList<BluetoothDeviceItem> deviceList = new ArrayList<>();
    private final String TAG = "NewConnectionActivity";
    RecyclerView deviceRecyclerView;
    Button newDeviceBtn;
    View bluetoothDisabledView;
    Button bluetoothSettingsBtn;
    private ActivityResultLauncher<Intent> launcherEnableBluetooth;
    private static final int REQUEST_CODE_BLUETOOTH_CONNECT = 1;
    private static final int REQUEST_CODE_BLUETOOTH_SCAN = 2;
    private static final int REQUEST_CODE_FINE_LOCATION_ACCESS = 3;
    private static final int REQUEST_CODE_POST_NOTIFICATION = 4;

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_connection);
        // 初始化蓝牙适配器
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        TextView title = findViewById(R.id.dialog_title);
        TextView subtitle = findViewById(R.id.dialog_subtitle);
        deviceRecyclerView = findViewById(R.id.device_recycler_view);
        newDeviceBtn = findViewById(R.id.new_device_btn);
        findViewById(R.id.back).setOnClickListener(v-> finish());
        //subtitle.setText("为控制设备，选择现有蓝牙设备或添加新设备");
        bluetoothDisabledView = findViewById(R.id.bluetooth_disabled_layout);
        bluetoothSettingsBtn = findViewById(R.id.open_bt_settings);
        bluetoothSettingsBtn.setOnClickListener(v-> {
            enableBT();
        });
        launcherEnableBluetooth = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), activityResult -> {
            if (activityResult.getResultCode() == -1) { // enabled
                populateBondedDevices();
            } else {
                Toast.makeText(this, "Bluetooth not enabled, exiting now.", Toast.LENGTH_LONG).show();
            }
        });
        // 加载蓝牙设备数据
        loadBluetoothDevices();

        // 创建设备列表适配器

        // 设置RecyclerView
        BluetoothDeviceAdapter adapter = new BluetoothDeviceAdapter(deviceList);
        deviceRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        deviceRecyclerView.setAdapter(adapter);

        // 添加分割线
        deviceRecyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // 设备点击事件
        adapter.setOnItemClickListener(position -> {
            BluetoothDeviceItem device = deviceList.get(position);
//            if (device.isSupported()) {
//                connectToDevice(device);
//            } else {
//                showUnsupportedMessage(device.getDeviceName());
//            }
            connectToDevice(device);
        });

        newDeviceBtn.setOnClickListener(v -> {
            startBluetoothDiscovery();
        });
        bluetoothDisabledView.setVisibility(!bluetoothAdapter.isEnabled() ?View.VISIBLE : View.GONE);

    }


    private void populateBondedDevices() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_CODE_BLUETOOTH_CONNECT);
        }
    }
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private void loadBluetoothDevices() {
        deviceList.clear();
        if (bluetoothAdapter != null) {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (!pairedDevices.isEmpty()) {
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    int deviceType = DeviceTypeUtils.getDeviceType(device);
                    int iconResId = DeviceTypeUtils.getDeviceIconResId(deviceType);
                    String status = DeviceTypeUtils.getConnectionStatus(device);
                    boolean isSupported = DeviceTypeUtils.isProbablySupported(device);
                    deviceList.add(new BluetoothDeviceItem(deviceName,
                            status, isSupported, iconResId, device));
                }
            }
        }
    }

    private void enableBT() {
        if (!bluetoothAdapter.isEnabled()) {
            launcherEnableBluetooth.launch(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
        }
    }

    private void connectToDevice(BluetoothDeviceItem item) {
        BluetoothHidService.bluetoothDevice = item.device;
        SharedPreferencesUtils.saveLastDevice(getApplication(), item.device);
        finish();
    }

    private void showUnsupportedMessage(String deviceName) {
        new AlertDialog.Builder(this)
                .setTitle("设备不支持")
                .setMessage(deviceName + " 可能不被支持")
                .setPositiveButton("确定", null)
                .show();
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private void startBluetoothDiscovery() {
        if (bluetoothAdapter != null) {
            // 请求蓝牙权限
            // 启动设备发现
            bluetoothAdapter.startDiscovery();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_FINE_LOCATION_ACCESS: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //showBluetoothDiscoveryPopup();
                } else {
                    Toast.makeText(this, "Fine location access not granted", Toast.LENGTH_LONG).show();
                }
                break;
            }
            case REQUEST_CODE_BLUETOOTH_SCAN: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //showBluetoothDiscoveryPopup();
                } else {
                    Toast.makeText(this, "Bluetooth Scan not granted", Toast.LENGTH_LONG).show();
                }
                break;
            }

            case REQUEST_CODE_BLUETOOTH_CONNECT: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "permisson BLUETOOTH_CONNECT granted");
                    enableBT();
                } else {
                    Toast.makeText(this, "Fine location access not granted, exit now ", Toast.LENGTH_LONG).show();
                    //finish();
                    Log.d(TAG, "permisson BLUETOOTH_CONNECT denied");
                    Intent intent = new Intent(this, BluetoothPermissionActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 避免在非 Activity 上下
                    startActivity(intent);
                }
                break;
            }
        }
    }
}
