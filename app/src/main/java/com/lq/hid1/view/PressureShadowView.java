package com.lq.hid1.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class PressureShadowView extends View {

    private Paint shadowPaint;
    private float lastPressure = 0f;
    private float circleX, circleY;
    private boolean isPressed = false;
    private final float BASE_RADIUS = 50f;
    private final float MAX_PRESSURE_FACTOR = 3f;

    // 定义点击监听器接口[1,4](@ref)
    public interface OnPressureClickListener {
        /**
         * 点击事件回调方法
         * @param x 点击位置的x坐标（相对于View左上角）
         * @param y 点击位置的y坐标（相对于View左上角）
         * @param rawX 点击位置的x坐标（相对于屏幕左上角）
         * @param rawY 点击位置的y坐标（相对于屏幕左上角）
         * @param pressure 按压力度值
         */
        void onPressureClick(float x, float y, float rawX, float rawY, float pressure);
    }

    private OnPressureClickListener pressureClickListener;

    public PressureShadowView(Context context) {
        super(context);
        init();
    }

    public PressureShadowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        shadowPaint = new Paint();
        shadowPaint.setColor(Color.BLACK);
        shadowPaint.setAntiAlias(true);
        setLayerType(LAYER_TYPE_SOFTWARE, shadowPaint);
        shadowPaint.setShadowLayer(20, 0, 0, Color.GRAY);
    }

    // 设置点击监听器[1](@ref)
    public void setOnPressureClickListener(OnPressureClickListener listener) {
        this.pressureClickListener = listener;
    }

    // 移除点击监听器
    public void removeOnPressureClickListener() {
        this.pressureClickListener = null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float currentX = event.getX(); // 相对于View左上角的X坐标[6,8](@ref)
        float currentY = event.getY(); // 相对于View左上角的Y坐标[6,8](@ref)
        float rawX = event.getRawX(); // 相对于屏幕左上角的X坐标[8](@ref)
        float rawY = event.getRawY(); // 相对于屏幕左上角的Y坐标[8](@ref)
        float pressure = event.getPressure();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                isPressed = true;
                circleX = currentX;
                circleY = currentY;
                lastPressure = Math.min(pressure, MAX_PRESSURE_FACTOR);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                // 只有在抬起时才触发回调[5](@ref)
                if (isPressed && pressureClickListener != null) {
                    pressureClickListener.onPressureClick(
                            circleX, circleY, rawX, rawY, lastPressure
                    );
                }
                isPressed = false;
                lastPressure = 0f;
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                isPressed = false;
                lastPressure = 0f;
                invalidate();
                break;
            default:
                return super.onTouchEvent(event);
        }
        return true; // 消费触摸事件[4,5](@ref)
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isPressed) {
            float currentRadius = BASE_RADIUS * lastPressure;
            canvas.drawCircle(circleX, circleY, currentRadius, shadowPaint);
        }
    }
}
