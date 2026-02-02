package com.lq.hid1.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
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

    RecyclerView deviceRecyclerView;
    Button newDeviceBtn;

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
}
