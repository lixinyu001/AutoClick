package com.example.autoclicker;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

public class FloatingWindowService extends Service {
    private WindowManager windowManager;
    private View floatingView;
    private WindowManager.LayoutParams params;
    private AutoClickService.ClickPoint selectedPoint = null;

    @Override
    public void onCreate() {
        super.onCreate();
        createFloatingWindow();
    }

    private void createFloatingWindow() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        
        // 创建悬浮窗布局
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        floatingView = inflater.inflate(R.layout.floating_window, null);
        
        // 设置悬浮窗参数
        params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? 
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : 
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        );
        
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 100;
        params.y = 100;
        
        // 添加悬浮窗到窗口管理器
        windowManager.addView(floatingView, params);
        
        // 设置触摸事件监听器
        setupTouchListeners();
    }

    private void setupTouchListeners() {
        // 开始按钮
        Button startBtn = floatingView.findViewById(R.id.btn_start);
        startBtn.setOnClickListener(v -> {
            AutoClickService service = AutoClickService.getInstance();
            if (service != null) {
                service.startAutoClick();
            }
        });
        
        // 停止按钮
        Button stopBtn = floatingView.findViewById(R.id.btn_stop);
        stopBtn.setOnClickListener(v -> {
            AutoClickService service = AutoClickService.getInstance();
            if (service != null) {
                service.stopAutoClick();
            }
        });
        
        // 添加点击点按钮
        Button addPointBtn = floatingView.findViewById(R.id.btn_add_point);
        addPointBtn.setOnClickListener(v -> {
            // 切换到添加点击点模式
            toggleAddPointMode();
        });
        
        // 悬浮窗拖动
        floatingView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            private boolean isDragging = false;
            private boolean isAddPointMode = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isAddPointMode) {
                    // 添加点击点模式
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        float x = event.getRawX();
                        float y = event.getRawY();
                        
                        // 添加点击点到服务
                        AutoClickService service = AutoClickService.getInstance();
                        if (service != null) {
                            AutoClickService.ClickPoint point = 
                                new AutoClickService.ClickPoint(x, y, "悬浮窗添加的点");
                            // 这里需要将点添加到列表中，实际使用时需要通过其他方式传递
                        }
                        
                        isAddPointMode = false;
                        return true;
                    }
                    return false;
                }
                
                // 正常拖动模式
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        isDragging = false;
                        return true;
                        
                    case MotionEvent.ACTION_MOVE:
                        float deltaX = event.getRawX() - initialTouchX;
                        float deltaY = event.getRawY() - initialTouchY;
                        
                        if (Math.abs(deltaX) > 10 || Math.abs(deltaY) > 10) {
                            isDragging = true;
                            params.x = initialX + (int) deltaX;
                            params.y = initialY + (int) deltaY;
                            windowManager.updateViewLayout(floatingView, params);
                        }
                        return true;
                        
                    case MotionEvent.ACTION_UP:
                        if (!isDragging) {
                            // 点击事件，可以在这里处理其他操作
                        }
                        return true;
                }
                return false;
            }
        });
    }

    private void toggleAddPointMode() {
        // 切换添加点击点模式
        // 实际实现需要更复杂的逻辑
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (windowManager != null && floatingView != null) {
            windowManager.removeView(floatingView);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}