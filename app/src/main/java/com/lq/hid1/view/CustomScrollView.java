package com.lq.hid1.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class CustomScrollView extends View {
    public CustomScrollView(Context context) {
        super(context);
        init();
    }
    public CustomScrollView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    private void init() {
        // 初始化画笔、颜色、滑动参数等
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#005792")); // 深蓝色背景
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        // 绘制滑动指示线、图标等
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height); // 自定义宽高
    }

    private float mLastY; // 上一次触摸Y坐标
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = y;
                return true; // 消费触摸事件
            case MotionEvent.ACTION_MOVE:
                float dy = y - mLastY;
                scrollBy(0, (int) -dy); // 垂直滑动（scrollBy为反向偏移）
                mLastY = y;
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void scrollBy(int x, int y) {
        int maxScrollY = 100; //getContentHeight() - getHeight(); // 内容总高 - View显示高
        int newScrollY = getScrollY() + y;
        if (newScrollY < 0) newScrollY = 0;
        if (newScrollY > maxScrollY) newScrollY = maxScrollY;
        super.scrollBy(x, newScrollY - getScrollY());
    }
}
