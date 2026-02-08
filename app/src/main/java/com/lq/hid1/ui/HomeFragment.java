package com.lq.hid1.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.lq.hid1.R;
import com.lq.hid1.bt.MouseHelper;
import com.lq.hid1.view.PressureShadowView;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private ImageView ivMousePointer;
    //private View scrollbar;
    private MaterialButton btnLeftClick;
    private MaterialButton btnRightClick;
    private PressureShadowView mouseArea;
    private View mUpView;
    private View mDownView;
    private View mLineView;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 初始化视图
        initViews(view);

        // 设置点击监听器
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        ivMousePointer = view.findViewById(R.id.iv_mouse_pointer);
//        scrollbar = view.findViewById(R.id.scrollbar);
        btnLeftClick = view.findViewById(R.id.btn_left_click);
        btnRightClick = view.findViewById(R.id.btn_right_click);
        mouseArea = view.findViewById(R.id.mouse_area);
        mUpView = view.findViewById(R.id.arrow_up);
        mDownView = view.findViewById(R.id.arrow_down);
        mLineView = view.findViewById(R.id.arrow_line);
    }

    private void setupClickListeners() {
        // 左键点击
        btnLeftClick.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Left Click", Toast.LENGTH_SHORT).show();
            boolean sent =
                    MouseHelper.sendData(true, false, false, 0, 0, 0);
            Log.d(TAG, "left sent = " + sent);
            // 这里可以添加实际的左键点击逻辑
        });

        // 右键点击
        btnRightClick.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Right Click", Toast.LENGTH_SHORT).show();
            boolean sent =
                    MouseHelper.sendData(false, true, false, 0, 0, 0);
            Log.d(TAG, "right sent = " + sent);
            // 这里可以添加实际的右键点击逻辑
        });



        // 鼠标区域触摸监听（模拟鼠标移动）

        if (mouseArea != null) {
            mouseArea.setOnPressureClickListener(
                    (x, y, rawX, rawY, pressure) -> {
                        // 在这里处理接收到的坐标和压力信息[1,4](@ref)
                        String message = String.format(
                                "点击位置: (%.1f, %.1f)\n屏幕位置: (%.1f, %.1f)\n压力值: %.2f",
                                x, y, rawX, rawY, pressure
                        );

                        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();

                        // 或者进行其他业务逻辑处理
                        Log.d("PressureClick", message);
            });
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        // 移除所有回调
        handler.removeCallbacksAndMessages(null);
    }
}
