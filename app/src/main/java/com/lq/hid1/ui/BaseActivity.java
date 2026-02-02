package com.lq.hid1.ui;
import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 动态权限管理基类
 * 使用说明：
 * 1. 继承 BaseActivity
 * 2. 在需要权限的地方调用 requestPermissions() 方法
 * 3. 重写 onPermissionsGranted() 处理权限授予后的逻辑
 * 4. 重写 onPermissionsDenied() 处理权限被拒绝后的逻辑
 */
public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";
    private static final int REQUEST_CODE_PERMISSIONS = 1000;
    private static final int REQUEST_CODE_SETTINGS = 1001;

    public interface PermissionCallback {
        void onGranted();
        void onDenied();
    }

    private Map<Integer, PermissionCallback> permissionCallbacks = new HashMap<>();
    private int requestCodeCounter = 1000;

    // 常用权限组
    public static class Permissions {
        public static final String[] CAMERA = {Manifest.permission.CAMERA};
        public static final String[] LOCATION = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
        public static final String[] STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        public static final String[] CONTACTS = {Manifest.permission.READ_CONTACTS};
        public static final String[] PHONE = {Manifest.permission.CALL_PHONE};
        public static final String[] CALENDAR = {Manifest.permission.READ_CALENDAR};
        public static final String[] MICROPHONE = {Manifest.permission.RECORD_AUDIO};
        public static final String[] SMS = {Manifest.permission.SEND_SMS};
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    public boolean hasPermissions(String[] permissions) {
        for (String permission : permissions) {
            if (!hasPermission(permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 请求权限（简单方式）
     * @param permissions 需要请求的权限数组
     * @param rationale 如果权限被拒绝过，显示的说明文字
     */
    public void requestPermissions(String[] permissions, String rationale) {
        if (hasPermissions(permissions)) {
            onPermissionsGranted(permissions);
            return;
        }

        // 检查是否需要显示权限说明
        List<String> permissionsToRequest = new ArrayList<>();
        List<String> shouldShowRationale = new ArrayList<>();

        for (String permission : permissions) {
            if (!hasPermission(permission)) {
                permissionsToRequest.add(permission);
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    shouldShowRationale.add(permission);
                }
            }
        }

        if (!shouldShowRationale.isEmpty() && rationale != null) {
            // 显示权限说明
            showRationaleDialog(rationale, permissionsToRequest.toArray(new String[0]));
        } else {
            // 直接请求权限
            doRequestPermissions(permissionsToRequest.toArray(new String[0]));
        }
    }

    /**
     * 请求权限（带回调）
     * @param permissions 需要请求的权限数组
     * @param callback 权限请求回调
     */
    public void requestPermissions(String[] permissions, PermissionCallback callback) {
        int requestCode = generateRequestCode();
        permissionCallbacks.put(requestCode, callback);

        requestPermissionsWithCallback(permissions, requestCode);
    }

    public void requestPermissions(String[] permissions, String rationale, PermissionCallback callback) {
        int requestCode = generateRequestCode();
        permissionCallbacks.put(requestCode, callback);

        if (hasPermissions(permissions)) {
            callback.onGranted();
            return;
        }

        // 检查是否需要显示权限说明
        List<String> permissionsToRequest = new ArrayList<>();
        List<String> shouldShowRationale = new ArrayList<>();

        for (String permission : permissions) {
            if (!hasPermission(permission)) {
                permissionsToRequest.add(permission);
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    shouldShowRationale.add(permission);
                }
            }
        }

        if (!shouldShowRationale.isEmpty() && rationale != null) {
            // 显示权限说明
            showRationaleDialog(rationale, permissionsToRequest.toArray(new String[0]), requestCode);
        } else {
            // 直接请求权限
            doRequestPermissions(permissionsToRequest.toArray(new String[0]), requestCode);
        }
    }

    private int generateRequestCode() {
        return requestCodeCounter++;
    }

    private void requestPermissionsWithCallback(String[] permissions, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, permissions, requestCode);
        }
    }

    private void doRequestPermissions(String[] permissions) {
        doRequestPermissions(permissions, REQUEST_CODE_PERMISSIONS);
    }

    private void doRequestPermissions(String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(this, permissions, requestCode);
    }

    private void showRationaleDialog(String message, String[] permissions) {
        showRationaleDialog(message, permissions, REQUEST_CODE_PERMISSIONS);
    }

    private void showRationaleDialog(String message, String[] permissions, int requestCode) {
        new AlertDialog.Builder(this)
                .setTitle("权限说明")
                .setMessage(message)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doRequestPermissions(permissions, requestCode);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // 处理带回调的权限请求
        PermissionCallback callback = permissionCallbacks.get(requestCode);
        if (callback != null) {
            handlePermissionResultWithCallback(requestCode, permissions, grantResults, callback);
            permissionCallbacks.remove(requestCode);
            return;
        }

        // 处理默认的权限请求
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            handlePermissionResult(permissions, grantResults);
        }
    }

    private void handlePermissionResult(String[] permissions, int[] grantResults) {
        boolean allGranted = true;
        List<String> grantedPermissions = new ArrayList<>();
        List<String> deniedPermissions = new ArrayList<>();

        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                grantedPermissions.add(permissions[i]);
            } else {
                deniedPermissions.add(permissions[i]);
                allGranted = false;
            }
        }

        if (allGranted) {
            onPermissionsGranted(permissions);
        } else {
            onPermissionsDenied(deniedPermissions.toArray(new String[0]));

            // 检查是否有权限被永久拒绝
            checkPermanentlyDeniedPermissions(deniedPermissions.toArray(new String[0]));
        }
    }

    private void handlePermissionResultWithCallback(int requestCode, String[] permissions,
                                                    int[] grantResults, PermissionCallback callback) {
        boolean allGranted = true;

        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (allGranted) {
            callback.onGranted();
        } else {
            callback.onDenied();

            // 检查是否有权限被永久拒绝
            List<String> deniedPermissions = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permissions[i]);
                }
            }
            checkPermanentlyDeniedPermissions(deniedPermissions.toArray(new String[0]));
        }
    }

    /**
     * 检查是否有权限被永久拒绝
     */
    private void checkPermanentlyDeniedPermissions(String[] deniedPermissions) {
        List<String> permanentlyDenied = new ArrayList<>();

        for (String permission : deniedPermissions) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                permanentlyDenied.add(permission);
            }
        }

        if (!permanentlyDenied.isEmpty()) {
            onPermissionsPermanentlyDenied(permanentlyDenied.toArray(new String[0]));
        }
    }

    /**
     * 跳转到应用设置页面
     */
    public void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, REQUEST_CODE_SETTINGS);
    }

    /**
     * 显示去设置对话框
     */
    protected void showSettingsDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("需要权限")
                .setMessage(message)
                .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openAppSettings();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SETTINGS) {
            onReturnFromSettings();
        }
    }

    protected void onPermissionsGranted(String[] permissions) {
        Log.d(TAG, "Permissions granted: " + String.join(", ", permissions));
    }

    protected void onPermissionsDenied(String[] permissions) {
        Log.d(TAG, "Permissions denied: " + String.join(", ", permissions));
        Toast.makeText(this, "部分权限被拒绝，可能影响功能使用", Toast.LENGTH_SHORT).show();
    }

    protected void onPermissionsPermanentlyDenied(String[] permissions) {
        Log.d(TAG, "Permissions permanently denied: " + String.join(", ", permissions));
        showSettingsDialog("某些权限被永久拒绝，请到应用设置中手动开启权限");
    }

    protected void onReturnFromSettings() {
        Log.d(TAG, "Returned from settings");
    }

    public String getPermissionName(String permission) {
        switch (permission) {
            case Manifest.permission.CAMERA:
                return "相机";
            case Manifest.permission.READ_EXTERNAL_STORAGE:
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return "存储";
            case Manifest.permission.ACCESS_FINE_LOCATION:
            case Manifest.permission.ACCESS_COARSE_LOCATION:
                return "位置";
            case Manifest.permission.RECORD_AUDIO:
                return "麦克风";
            case Manifest.permission.READ_CONTACTS:
                return "通讯录";
            case Manifest.permission.CALL_PHONE:
                return "电话";
            default:
                return permission;
        }
    }
}