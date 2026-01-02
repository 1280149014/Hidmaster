package com.lq.hid1.ui;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.lq.hid1.R;
import com.lq.hid1.adapter.DeviceListAdapter;
import com.lq.hid1.bt.BluetoothHidService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_BLUETOOTH_CONNECT = 1;
    private static final int REQUEST_CODE_BLUETOOTH_SCAN = 2;
    private static final int REQUEST_CODE_FINE_LOCATION_ACCESS = 3;
    private static final int REQUEST_CODE_POST_NOTIFICATION = 4;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;
    static Vibrator vibrator;

    // 声明Toolbar及内部控件
    private ImageView toolbarMenu;      // 左侧菜单按钮
    private TextView toolbarTitle;      // 中间主标题
    private TextView selectDeviceName;
    private View connectionStatus;     // 中间状态文本
    private ImageView toolbarKeyboard;  // 右侧键盘图标
    private ImageView toolbarSetting;   // 右侧设置图标
    private PopupWindow mDevicePopup;
    private static final int REQUEST_ENABLE_BLUETOOTH = 1001;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice mSelectedDevice;
    private boolean isNotificationRefused = false;
    private ActivityResultLauncher<Intent> launcherEnableBluetooth;

    private View mBluetoothEnableView;
    private View mBtNotConnectedPrompt;
    private View mBtConnectionBtn;
    private Button mBtnEnable;
    BluetoothStateReceiver mBluetoothReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();
        initViews();
        setSupportActionBar(toolbar);
        toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        vibrator = getSystemService(Vibrator.class);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            navigationView.setCheckedItem(R.id.nav_home);
        }
        launcherEnableBluetooth = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), activityResult -> {
            if (activityResult.getResultCode() == -1) { // enabled
                populateBondedDevices();
            } else {
                Toast.makeText(MainActivity.this, "Bluetooth not enabled, exiting now.", Toast.LENGTH_LONG).show();
            }
        });

        if (!bluetoothAdapter.isEnabled()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                        Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_CODE_BLUETOOTH_CONNECT);
            } else {
                enableBT();
            }
        } else {
            populateBondedDevices();
        }
        initBluetoothListener();
    }

    private void initBluetoothListener() {
        // 获取蓝牙适配器（判断设备是否支持蓝牙）
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            return;
        }

        // 创建广播接收器
        mBluetoothReceiver = new BluetoothStateReceiver();
        // 注册广播：监听蓝牙状态变化
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBluetoothReceiver, filter);
    }

    private void populateBondedDevices() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_CODE_BLUETOOTH_CONNECT);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Checking Bluetooth status");
        if (!bluetoothAdapter.isEnabled()) {
            mBluetoothEnableView.setVisibility(VISIBLE);
        } else {
            mBluetoothEnableView.setVisibility(GONE);;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "蓝牙已开启");
            } else {
                Log.d(TAG, "蓝牙未开启");
            }
        }
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        mBluetoothEnableView = findViewById(R.id.bluetooth_enable_prompt);
        mBtnEnable = findViewById(R.id.btn_enable);
        mBtNotConnectedPrompt = findViewById(R.id.bluetooth_Not_Connected_prompt);
        mBtConnectionBtn = findViewById(R.id.btn_connection_setup);
        // 获取Toolbar内部子控件
        toolbarMenu = toolbar.findViewById(R.id.toolbar_menu);
        toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        connectionStatus = toolbar.findViewById(R.id.connection_status);
        selectDeviceName = toolbar.findViewById(R.id.select_device_name);
        toolbarKeyboard = toolbar.findViewById(R.id.toolbar_keyboard);
        toolbarSetting = toolbar.findViewById(R.id.toolbar_setting);

        // ========== 2. 绑定点击事件 ==========
        // 左侧菜单按钮点击
        toolbarMenu.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "打开侧边菜单", Toast.LENGTH_SHORT).show();
            // 可扩展：打开DrawerLayout侧边栏
            // drawerLayout.openDrawer(Gravity.LEFT);
        });

        // 右侧键盘图标点击
        toolbarKeyboard.setOnClickListener(v -> Toast.makeText(MainActivity.this,
                "切换键盘模式", Toast.LENGTH_SHORT).show());

        // 右侧设置图标点击
        toolbarSetting.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "打开设置页面", Toast.LENGTH_SHORT).show();
            // 可扩展：跳转到设置Activity
            // Intent intent = new Intent(MainActivity.this, SettingActivity.class);
            // startActivity(intent);
        });

        connectionStatus.setOnClickListener(this::showDeviceSelectPopup);
        mBtnEnable.setOnClickListener(v -> enableBT());
        mBtConnectionBtn.setOnClickListener(v -> showConnectionActivity());
        updateSelectedDeviceName();

        OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
                if (backStackEntryCount <= 1) {
                    finish();
                }
            }
        };

        dispatcher.addCallback(this, callback);
    }

    private void showConnectionActivity() {
        Intent intent = new Intent(MainActivity.this, NewConnectionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 避免在非 Activity 上下文中崩溃
        startActivity(intent);
    }

    public static Handler handlerUi;

    private void createUIHandler() {
        if (handlerUi == null) {
            handlerUi = new Handler(getMainLooper()) {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    super.handleMessage(msg);
                    switch (msg.what) {
                        case BluetoothHidService.STATUS.BLUETOOTH_DISCONNECTED:{
                            Log.d(TAG, "Bluetooth disconnected");
                            break;
                        }
                        case BluetoothHidService.STATUS.BLUETOOTH_CONNECTING : {
                            Log.d(TAG, "Bluetooth connecting");
                            break;
                        }
                        case BluetoothHidService.STATUS.BLUETOOTH_CONNECTED :{
                            Log.d(TAG, "Bluetooth connected");
                            break;
                        }
                    }
                }
            };
        }
    }

    // 显示设备选择弹窗
    private void showDeviceSelectPopup(View anchorView) {
        // 1. 获取 BluetoothAdapter
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. 加载弹窗布局
        View popupView = getLayoutInflater().inflate(R.layout.popup_device_list, null);
        RecyclerView rvDeviceList = popupView.findViewById(R.id.rv_device_list);
        Button btnSetupRemote = popupView.findViewById(R.id.btn_setup_remote);

        // 3. 获取已配对的蓝牙设备列表（只取名称）
        updateSelectedDeviceName();
        List<BluetoothDevice> deviceList = getStrings(bluetoothAdapter);
        // 4. 设置 RecyclerView
        rvDeviceList.setLayoutManager(new LinearLayoutManager(this));
        DeviceListAdapter adapter = new DeviceListAdapter(
                this, deviceList, (device, position) -> {
            Log.d(TAG, "选中了：" + device);
            if (mDevicePopup != null && mDevicePopup.isShowing()) {
                mDevicePopup.dismiss();
            }
            mSelectedDevice = device;
            startService(device, isNotificationRefused);

            // TODO: 你可以在这里通过位置或名称反查 BluetoothDevice 对象（见下方说明）
        });
        rvDeviceList.setAdapter(adapter);

        // 5. 底部“添加设备”按钮点击事件
        btnSetupRemote.setOnClickListener(v -> {
            Toast.makeText(this, "开始添加远程设备", Toast.LENGTH_SHORT).show();
            if (mDevicePopup != null && mDevicePopup.isShowing()) {
                mDevicePopup.dismiss();
            }
            Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 避免在非 Activity 上下文中崩溃
            startActivity(intent);
        });

        // 6. 创建并显示 PopupWindow
        mDevicePopup = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );
        mDevicePopup.setOutsideTouchable(true);
        mDevicePopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // 可选：解决部分机型点击外部无效
        mDevicePopup.showAsDropDown(anchorView, 0, 0, Gravity.START);
    }

    private void updateSelectedDeviceName() {
        try {
            List<BluetoothDevice> deviceList = getStrings(bluetoothAdapter);
            if (deviceList.isEmpty()) {
                selectDeviceName.setText(R.string.not_connected);
                mBtNotConnectedPrompt.setVisibility(VISIBLE);
            } else {
                selectDeviceName.setText(deviceList.get(0).getName());
                mBtNotConnectedPrompt.setVisibility(GONE);
            }
        } catch (SecurityException e) {
            Log.w(TAG, "updateSelectedDeviceName:", e);
        }
    }

    private void enableBT() {
        if (!bluetoothAdapter.isEnabled()) {
            launcherEnableBluetooth.launch(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
        }
    }

    private static boolean isHidHostSupported(BluetoothDevice device) {
        if (device == null) return false;
        try {
            android.os.ParcelUuid[] uuids = device.getUuids();
            if (uuids == null) return false;
            android.os.ParcelUuid hidHostUuid = android.os.ParcelUuid.fromString("00001124-0000-1000-8000-00805f9b34fb");
            for (android.os.ParcelUuid u : uuids) {
                Log.d(TAG, "Device UUID: " + u.toString());
                if (hidHostUuid.equals(u)) return true;
            }
        } catch (SecurityException e) {
            Log.w(TAG, "isHidHostSupported: missing permission to read device uuids", e);
            return false;
        }
        return false;
    }

    @NonNull
    private static List<BluetoothDevice> getStrings(BluetoothAdapter bluetoothAdapter) {
        List<BluetoothDevice> deviceList = new ArrayList<>();
        try {
            Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
            if (bondedDevices != null && !bondedDevices.isEmpty()) {
                for(BluetoothDevice device : bondedDevices) {
                    Log.e(TAG, "Found bonded device: " + device.getName() + " - " + device.getAddress()
                        + " - UUIDs: " + (device.getUuids() != null ? device.getUuids().toString() : "null"));
                    if (isHidHostSupported(device)) {
                        deviceList.add(device);
                    }
                }
            }
        } catch (SecurityException e) {
            Log.w(TAG, "isHidHostSupported: missing permission to read device uuids", e);
        }
        return deviceList;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Fragment fragment = null;
        String fragmentTag = "";

        if (id == R.id.nav_home) {
            fragment = new HomeFragment();
            fragmentTag = "HOME_FRAGMENT";
        } else if (id == R.id.nav_profile) {
            fragment = new ProfileFragment();
            fragmentTag = "PROFILE_FRAGMENT";
        } else if (id == R.id.nav_settings) {
            fragment = new SettingsFragment();
            fragmentTag = "SETTINGS_FRAGMENT";
        }

        if (fragment != null) {
            loadFragment(fragment, fragmentTag);
        }

        // 关闭抽屉
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadFragment(Fragment fragment) {
        loadFragment(fragment, null);
    }

    private void loadFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // 替换Fragment
        transaction.replace(R.id.fragment_container, fragment, tag);

        // 添加到返回栈
        transaction.addToBackStack(tag);

        transaction.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 取消广播注册
        if (mBluetoothReceiver != null) {
            unregisterReceiver(mBluetoothReceiver);
        }
    }

    public static void vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK));
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE));
            }
        }
    }

    private void startService(BluetoothDevice device, boolean isNotificationRefused) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED &&
                !isNotificationRefused) {
            ActivityCompat.requestPermissions(
                    MainActivity.this, new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_CODE_POST_NOTIFICATION);
        } else {
            Intent serviceIntent = new Intent(this, BluetoothHidService.class);
            BluetoothHidService.bluetoothDevice = device;
            createUIHandler();
            if (BluetoothHidService.bluetoothDevice != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent);
                }
            } else {
                Toast.makeText(MainActivity.this, "Device not supported!", Toast.LENGTH_LONG).show();
            }
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
                    Toast.makeText(MainActivity.this, "Fine location access not granted", Toast.LENGTH_LONG).show();
                }
                break;
            }
            case REQUEST_CODE_BLUETOOTH_SCAN: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //showBluetoothDiscoveryPopup();
                } else {
                    Toast.makeText(MainActivity.this, "Bluetooth Scan not granted", Toast.LENGTH_LONG).show();
                }
                break;
            }

            case REQUEST_CODE_BLUETOOTH_CONNECT: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "permisson BLUETOOTH_CONNECT granted");
                    enableBT();

                } else {
                    Toast.makeText(MainActivity.this, "Fine location access not granted, exit now ", Toast.LENGTH_LONG).show();
                    //finish();
                    Log.d(TAG, "permisson BLUETOOTH_CONNECT denied");
                    Intent intent = new Intent(MainActivity.this, BluetoothPermissionActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 避免在非 Activity 上下
                    startActivity(intent);
                }
                break;
            }

            case REQUEST_CODE_POST_NOTIFICATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startService(mSelectedDevice, false);
                } else {
                    isNotificationRefused = true;
                    Toast.makeText(MainActivity.this, "No notification buttons will be displayed!", Toast.LENGTH_LONG).show();
                    startService(mSelectedDevice, true);
                }
                break;
            }
        }
    }

    // 蓝牙状态广播接收器
    private class BluetoothStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                // 获取当前蓝牙状态
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                // 获取之前的蓝牙状态（可选）
                int previousState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, BluetoothAdapter.ERROR);

                switch (state) {
                    case BluetoothAdapter.STATE_TURNING_ON:
                        // 蓝牙正在开启中
                        Log.d("Bluetooth", "正在开启蓝牙");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        // 蓝牙已成功开启
                        Log.d("Bluetooth", "蓝牙已开启");
                        //Toast.makeText(context, "蓝牙已开启", Toast.LENGTH_SHORT).show();
                        mBluetoothEnableView.setVisibility(GONE);
                        mBtNotConnectedPrompt.setVisibility(GONE);
                        connectionStatus.setVisibility(VISIBLE);
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        // 蓝牙正在关闭中
                        Log.d("Bluetooth", "正在关闭蓝牙");
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        // 蓝牙已成功关闭
                        Log.d("Bluetooth", "蓝牙已关闭");
                        mBluetoothEnableView.setVisibility(VISIBLE);
                        mBtNotConnectedPrompt.setVisibility(GONE);
                        connectionStatus.setVisibility(GONE);
                        //Toast.makeText(context, "蓝牙已关闭", Toast.LENGTH_SHORT).show();
                        break;
                }
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
                    if (bondState == BluetoothDevice.BOND_BONDED && device != null) {
                        String deviceName = device.getName();
                        String deviceHardwareAddress = device.getAddress(); // MAC address
                        Log.d(TAG, "Bonding completed with " + deviceName + ", " + deviceHardwareAddress);
                        //TODO: Connect device right away
//                        debug("Starting Service with device " + deviceName);
//                        populateBondedDevices();
//                        cmbBondedDevices.setSelection(0);
//                        startService(device);
//                        bluetoothAdapter.getProfileProxy(MainActivity.this, MainActivity.this, BluetoothProfile.HID_DEVICE);
                    }
            }
        }
    }
}