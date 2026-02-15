package com.example.autoclicker.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.autoclicker.R;

import java.util.ArrayList;
import java.util.List;

public class EnhancedFloatingWindowService extends Service {
    private WindowManager windowManager;
    private View floatingView;
    private View editView;
    private WindowManager.LayoutParams params;
    private WindowManager.LayoutParams editParams;
    private AutoClickService service;
    private Handler handler = new Handler(Looper.getMainLooper());
    
    private boolean isEditMode = false;
    private boolean isRecordingMode = false;
    private List<ClickPoint> tempClickPoints = new ArrayList<>();
    private List<View> clickPointViews = new ArrayList<>();
    
    private static final int FLOATING_WINDOW_SIZE = 150;
    private static final int CLICK_POINT_SIZE = 30;

    @Override
    public void onCreate() {
        super.onCreate();
        service = AutoClickService.getInstance();
        createFloatingWindow();
    }

    private void createFloatingWindow() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        floatingView = inflater.inflate(R.layout.floating_window, null);
        
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
        
        windowManager.addView(floatingView, params);
        setupFloatingWindowListeners();
    }

    private void setupFloatingWindowListeners() {
        Button startBtn = floatingView.findViewById(R.id.btn_start);
        startBtn.setOnClickListener(v -> startAutoClick());
        
        Button stopBtn = floatingView.findViewById(R.id.btn_stop);
        stopBtn.setOnClickListener(v -> stopAutoClick());
        
        Button addPointBtn = floatingView.findViewById(R.id.btn_add_point);
        addPointBtn.setOnClickListener(v -> toggleEditMode());
        
        Button recordBtn = floatingView.findViewById(R.id.btn_record);
        if (recordBtn != null) {
            recordBtn.setOnClickListener(v -> toggleRecordingMode());
        }
        
        Button closeBtn = floatingView.findViewById(R.id.btn_close);
        if (closeBtn != null) {
            closeBtn.setOnClickListener(v -> stopSelf());
        }
        
        setupDragListener();
    }

    private void setupDragListener() {
        floatingView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            private boolean isDragging = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
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
                        if (!isDragging && !isEditMode) {
                            showQuickMenu(event.getRawX(), event.getRawY());
                        }
                        return true;
                }
                return false;
            }
        });
    }

    private void showQuickMenu(float x, float y) {
        if (editView != null) {
            windowManager.removeView(editView);
            editView = null;
            return;
        }
        
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        editView = inflater.inflate(R.layout.floating_window, null);
        
        editParams = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? 
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : 
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        );
        
        editParams.gravity = Gravity.TOP | Gravity.START;
        editParams.x = (int) x;
        editParams.y = (int) y;
        
        setupQuickMenuListeners();
        windowManager.addView(editView, editParams);
    }

    private void setupQuickMenuListeners() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(10, 10, 10, 10);
        
        Button addClickPointBtn = new Button(this);
        addClickPointBtn.setText("添加点击点");
        addClickPointBtn.setOnClickListener(v -> {
            addClickPointAtCenter();
            windowManager.removeView(editView);
            editView = null;
        });
        layout.addView(addClickPointBtn);
        
        Button startRecordingBtn = new Button(this);
        startRecordingBtn.setText("开始录制");
        startRecordingBtn.setOnClickListener(v -> {
            toggleRecordingMode();
            windowManager.removeView(editView);
            editView = null;
        });
        layout.addView(startRecordingBtn);
        
        Button executeScriptBtn = new Button(this);
        executeScriptBtn.setText("执行脚本");
        executeScriptBtn.setOnClickListener(v -> {
            executeCurrentScript();
            windowManager.removeView(editView);
            editView = null;
        });
        layout.addView(executeScriptBtn);
        
        Button closeMenuBtn = new Button(this);
        closeMenuBtn.setText("关闭");
        closeMenuBtn.setOnClickListener(v -> {
            windowManager.removeView(editView);
            editView = null;
        });
        layout.addView(closeMenuBtn);
    }

    private void toggleEditMode() {
        isEditMode = !isEditMode;
        
        if (isEditMode) {
            enterEditMode();
            Toast.makeText(this, "进入编辑模式，点击屏幕添加点击点", Toast.LENGTH_SHORT).show();
        } else {
            exitEditMode();
            Toast.makeText(this, "退出编辑模式", Toast.LENGTH_SHORT).show();
        }
    }

    private void enterEditMode() {
        createEditOverlay();
    }

    private void exitEditMode() {
        if (editView != null) {
            windowManager.removeView(editView);
            editView = null;
        }
        clearClickPointViews();
    }

    private void createEditOverlay() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        editView = inflater.inflate(R.layout.floating_window, null);
        
        editParams = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? 
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : 
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        );
        
        editParams.gravity = Gravity.TOP | Gravity.START;
        
        setupEditOverlayListener();
        windowManager.addView(editView, editParams);
    }

    private void setupEditOverlayListener() {
        editView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    addClickPoint(event.getRawX(), event.getRawY());
                    return true;
                }
                return false;
            }
        });
    }

    private void addClickPoint(float x, float y) {
        ClickPoint point = new ClickPoint(x, y, "点击点 " + (tempClickPoints.size() + 1));
        tempClickPoints.add(point);
        
        createClickPointView(x, y);
        
        if (service != null) {
            List<AutoClickService.ClickPoint> servicePoints = new ArrayList<>();
            for (ClickPoint p : tempClickPoints) {
                servicePoints.add(new AutoClickService.ClickPoint(p.x, p.y, p.description));
            }
            service.setClickPoints(servicePoints);
        }
        
        Toast.makeText(this, "添加点击点: (" + (int)x + ", " + (int)y + ")", Toast.LENGTH_SHORT).show();
    }

    private void addClickPointAtCenter() {
        Point size = new Point();
        windowManager.getDefaultDisplay().getSize(size);
        float centerX = size.x / 2f;
        float centerY = size.y / 2f;
        addClickPoint(centerX, centerY);
    }

    private void createClickPointView(float x, float y) {
        View pointView = new View(this);
        pointView.setBackgroundColor(0xFFFF0000);
        
        WindowManager.LayoutParams pointParams = new WindowManager.LayoutParams(
            CLICK_POINT_SIZE,
            CLICK_POINT_SIZE,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? 
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : 
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        );
        
        pointParams.gravity = Gravity.TOP | Gravity.START;
        pointParams.x = (int) (x - CLICK_POINT_SIZE / 2);
        pointParams.y = (int) (y - CLICK_POINT_SIZE / 2);
        
        windowManager.addView(pointView, pointParams);
        clickPointViews.add(pointView);
    }

    private void clearClickPointViews() {
        for (View view : clickPointViews) {
            windowManager.removeView(view);
        }
        clickPointViews.clear();
    }

    private void toggleRecordingMode() {
        isRecordingMode = !isRecordingMode;
        
        if (isRecordingMode) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void startRecording() {
        if (service != null && service.getActionRecorder() != null) {
            service.getActionRecorder().startRecording();
            Toast.makeText(this, "开始录制操作", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        if (service != null && service.getActionRecorder() != null) {
            service.getActionRecorder().stopRecording();
            Toast.makeText(this, "停止录制，共录制 " + 
                service.getActionRecorder().getRecordedActions().size() + " 个动作", 
                Toast.LENGTH_SHORT).show();
        }
    }

    private void startAutoClick() {
        if (service != null) {
            service.startAutoClick();
            Toast.makeText(this, "开始自动点击", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopAutoClick() {
        if (service != null) {
            service.stopAutoClick();
            Toast.makeText(this, "停止自动点击", Toast.LENGTH_SHORT).show();
        }
    }

    private void executeCurrentScript() {
        if (service != null && service.getActionRecorder() != null) {
            List<ActionRecorder.RecordedAction> actions = service.getActionRecorder().getRecordedActions();
            if (!actions.isEmpty()) {
                ClickScript script = service.getActionRecorder().convertToScript("录制的脚本");
                service.executeScript(script);
                Toast.makeText(this, "执行录制的脚本", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "没有录制的脚本", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (windowManager != null) {
            if (floatingView != null) {
                windowManager.removeView(floatingView);
            }
            if (editView != null) {
                windowManager.removeView(editView);
            }
            clearClickPointViews();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static class ClickPoint {
        float x;
        float y;
        String description;

        ClickPoint(float x, float y, String description) {
            this.x = x;
            this.y = y;
            this.description = description;
        }
    }
}
