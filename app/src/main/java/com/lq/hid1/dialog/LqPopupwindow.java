//package com.lq.hid1.dialog;
//
//import static android.view.View.GONE;
//import static android.view.View.VISIBLE;
//
//import android.Manifest;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.graphics.Color;
//import android.graphics.drawable.ColorDrawable;
//import android.provider.Settings;
//import android.util.Log;
//import android.view.Gravity;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.PopupWindow;
//import android.widget.Toast;
//
//import androidx.core.app.ActivityCompat;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.lq.hid1.R;
//import com.lq.hid1.adapter.DeviceListAdapter;
//
//import java.util.List;
//
//public class LqPopupwindow {
//
//    private static PopupWindow mDevicePopup;
//    static void init(View anchorView) {
//        // 6. 创建并显示 PopupWindow
//        mDevicePopup = new PopupWindow(
//                popupView,
//                ViewGroup.LayoutParams.WRAP_CONTENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT,
//                true
//        );
//        mDevicePopup.setOutsideTouchable(true);
//        mDevicePopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // 可选：解决部分机型点击外部无效
//        mDevicePopup.showAsDropDown(anchorView, 0, 0, Gravity.START);
//    }
//    }
//
//
//    public static void showDeviceSelectPopup(View anchorView) {
//    // 1. 获取 BluetoothAdapter
//    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//    if (bluetoothAdapter == null) {
//        Toast.makeText(this, "设备不支持蓝牙", Toast.LENGTH_SHORT).show();
//        return;
//    }
//
//    // 2. 加载弹窗布局
//    View popupView = getLayoutInflater().inflate(R.layout.popup_device_list, null);
//    RecyclerView rvDeviceList = popupView.findViewById(R.id.rv_device_list);
//    Button btnSetupRemote = popupView.findViewById(R.id.btn_setup_remote);
//
//    // 3. 获取已配对的蓝牙设备列表（只取名称）
//    List<BluetoothDevice> deviceList = getStrings(bluetoothAdapter);
//    if (deviceList.isEmpty()) {
//        selectDeviceName.setVisibility(GONE);
//    } else {
//        selectDeviceName.setVisibility(VISIBLE);
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        selectDeviceName.setText(deviceList.get(0).getName());
//    }
//    // 4. 设置 RecyclerView
//    rvDeviceList.setLayoutManager(new LinearLayoutManager(this));
//    DeviceListAdapter adapter = new DeviceListAdapter(
//            this, deviceList, (device, position) -> {
//        Log.d(TAG, "选中了：" + device);
//        if (mDevicePopup != null && mDevicePopup.isShowing()) {
//            mDevicePopup.dismiss();
//        }
//        mSelectedDevice = device;
//        startService(device, isNotificationRefused);
//
//        // TODO: 你可以在这里通过位置或名称反查 BluetoothDevice 对象（见下方说明）
//    });
//    rvDeviceList.setAdapter(adapter);
//
//    // 5. 底部“添加设备”按钮点击事件
//    btnSetupRemote.setOnClickListener(v -> {
//        Toast.makeText(this, "开始添加远程设备", Toast.LENGTH_SHORT).show();
//        if (mDevicePopup != null && mDevicePopup.isShowing()) {
//            mDevicePopup.dismiss();
//        }
//        Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 避免在非 Activity 上下文中崩溃
//        startActivity(intent);
//    });
//
//    // 6. 创建并显示 PopupWindow
//    mDevicePopup = new PopupWindow(
//            popupView,
//            ViewGroup.LayoutParams.WRAP_CONTENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT,
//            true
//    );
//    mDevicePopup.setOutsideTouchable(true);
//    mDevicePopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // 可选：解决部分机型点击外部无效
//    mDevicePopup.showAsDropDown(anchorView, 0, 0, Gravity.START);
//}
//
//
//}