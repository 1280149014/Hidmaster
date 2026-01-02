package com.lq.hid1.ui;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lq.hid1.R;
import com.lq.hid1.adapter.BluetoothDeviceAdapter;
import com.lq.hid1.bean.BluetoothDeviceItem;

import java.util.ArrayList;
import java.util.Set;

public class NewConnectionActivity extends AppCompatActivity {
    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<BluetoothDeviceItem> deviceList = new ArrayList<>();

    RecyclerView deviceRecyclerView;
    Button newDeviceBtn;

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

        subtitle.setText("为控制设备，选择现有蓝牙设备或添加新设备");

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
            if (device.isSupported()) {
                connectToDevice(device);
            } else {
                showUnsupportedMessage(device.getDeviceName());
            }
        });


        // 新设备按钮点击事件
        newDeviceBtn.setOnClickListener(v -> {
            // 启动蓝牙设备发现
            startBluetoothDiscovery();
        });
    }

    private void loadBluetoothDevices() {
        deviceList.clear();
        try {
            // 从图片中提取的示例设备
//            deviceList.add(new BluetoothDeviceItem("OPPO110",
//                    "Connection should be possible", true));
//            deviceList.add(new BluetoothDeviceItem("VOYAH Second",
//                    "Connection should be possible", true));
//            deviceList.add(new BluetoothDeviceItem("Redmi Buds 5 Pro",
//                    "Probably not supported", false));

            // 如果有真实的蓝牙设备，可以这样获取
            if (bluetoothAdapter != null) {
                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                if (!pairedDevices.isEmpty()) {
                    for (BluetoothDevice device : pairedDevices) {
                        String deviceName = device.getName();
                        String deviceAddress = device.getAddress();
                        BluetoothClass cod = device.getBluetoothClass();
                        // 添加到列表
                        deviceList.add(new BluetoothDeviceItem(deviceName,
                                "Paired Device", true, R.drawable.ic_bluetooth_device));
                    }
                }
            }
        }   catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void connectToDevice(BluetoothDeviceItem device) {
        // 实现蓝牙连接逻辑
        // 这里可以启动蓝牙连接服务
    }

    private void showUnsupportedMessage(String deviceName) {
        new AlertDialog.Builder(this)
                .setTitle("设备不支持")
                .setMessage(deviceName + " 可能不被支持")
                .setPositiveButton("确定", null)
                .show();
    }

    private void startBluetoothDiscovery() {
        if (bluetoothAdapter != null) {
            // 请求蓝牙权限
            // 启动设备发现
            bluetoothAdapter.startDiscovery();
        }
    }
}
