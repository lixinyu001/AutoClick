package com.example.autoclicker.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.autoclicker.R;
import com.example.autoclicker.service.AutoClickService;
import com.example.autoclicker.service.EnhancedFloatingWindowService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_OVERLAY_PERMISSION = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 2;
    
    private LinearLayout clickPointsLayout;
    private EditText repeatCountEdit;
    private EditText intervalEdit;
    private List<AutoClickService.ClickPoint> clickPoints = new ArrayList<>();
    private int pointCounter = 0;
    
    // 临时存储获取的坐标
    private float lastX = 0;
    private float lastY = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 创建简单的UI布局
        createLayout();
        
        // 检查权限
        checkPermissions();
    }

    private void createLayout() {
        // 获取屏幕尺寸
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        
        // 根布局 - 用于滚动整个内容
        ScrollView rootScrollView = new ScrollView(this);
        
        // 主布局
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        // 设置主布局padding为屏幕宽度的2%
        int padding = (int) (screenWidth * 0.02);
        mainLayout.setPadding(padding, padding, padding, padding);
        
        // 标题
        TextView titleText = new TextView(this);
        titleText.setText("自动点击器");
        // 标题字体大小为屏幕宽度的5%
        titleText.setTextSize(screenWidth * 0.05f);
        // 标题padding为屏幕高度的1%
        int titlePadding = (int) (screenHeight * 0.01);
        titleText.setPadding(0, 0, 0, titlePadding);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        mainLayout.addView(titleText, titleParams);
        
        // 添加点击点按钮
        Button addPointBtn = new Button(this);
        addPointBtn.setText("添加点击点");
        // 按钮高度为屏幕高度的6%
        int buttonHeight = (int) (screenHeight * 0.06);
        LinearLayout.LayoutParams addPointParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            buttonHeight
        );
        // 边距为屏幕高度的0.5%
        int margin = (int) (screenHeight * 0.005);
        addPointParams.setMargins(0, 0, 0, margin / 2);
        addPointBtn.setOnClickListener(v -> addClickPoint());
        mainLayout.addView(addPointBtn, addPointParams);
        
        // 获取坐标按钮
        Button getCoordBtn = new Button(this);
        getCoordBtn.setText("获取坐标");
        LinearLayout.LayoutParams getCoordParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            buttonHeight
        );
        getCoordParams.setMargins(0, 0, 0, margin / 2);
        getCoordBtn.setOnClickListener(v -> startCoordinatePicker());
        mainLayout.addView(getCoordBtn, getCoordParams);
        
        // 开始按钮
        Button startBtn = new Button(this);
        startBtn.setText("开始自动点击");
        LinearLayout.LayoutParams startBtnParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            buttonHeight
        );
        startBtnParams.setMargins(0, 0, 0, margin / 2);
        startBtn.setOnClickListener(v -> startAutoClick());
        mainLayout.addView(startBtn, startBtnParams);
        
        // 停止按钮
        Button stopBtn = new Button(this);
        stopBtn.setText("停止自动点击");
        LinearLayout.LayoutParams stopBtnParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            buttonHeight
        );
        stopBtnParams.setMargins(0, 0, 0, margin);
        stopBtn.setOnClickListener(v -> stopAutoClick());
        mainLayout.addView(stopBtn, stopBtnParams);
        
        // 点击点列表
        ScrollView scrollView = new ScrollView(this);
        clickPointsLayout = new LinearLayout(this);
        clickPointsLayout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(clickPointsLayout);
        // 设置ScrollView高度为屏幕高度的25%
        int scrollHeight = (int) (screenHeight * 0.25);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            scrollHeight
        );
        scrollParams.setMargins(0, 0, 0, margin);
        mainLayout.addView(scrollView, scrollParams);
        
        // 重复次数
        TextView repeatLabel = new TextView(this);
        repeatLabel.setText("重复次数:");
        // 标签字体大小为屏幕宽度的3%
        repeatLabel.setTextSize(screenWidth * 0.03f);
        LinearLayout.LayoutParams repeatLabelParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        mainLayout.addView(repeatLabel, repeatLabelParams);
        
        repeatCountEdit = new EditText(this);
        repeatCountEdit.setText("1");
        repeatCountEdit.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        LinearLayout.LayoutParams repeatEditParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            buttonHeight
        );
        repeatEditParams.setMargins(0, 0, 0, margin);
        mainLayout.addView(repeatCountEdit, repeatEditParams);
        
        // 点击间隔
        TextView intervalLabel = new TextView(this);
        intervalLabel.setText("点击间隔(毫秒):");
        intervalLabel.setTextSize(screenWidth * 0.03f);
        LinearLayout.LayoutParams intervalLabelParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        mainLayout.addView(intervalLabel, intervalLabelParams);
        
        intervalEdit = new EditText(this);
        intervalEdit.setText("1000");
        intervalEdit.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        LinearLayout.LayoutParams intervalEditParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            buttonHeight
        );
        intervalEditParams.setMargins(0, 0, 0, margin);
        mainLayout.addView(intervalEdit, intervalEditParams);
        
        // 打开辅助功能设置
        Button settingsBtn = new Button(this);
        settingsBtn.setText("打开辅助功能设置");
        LinearLayout.LayoutParams settingsBtnParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            buttonHeight
        );
        settingsBtnParams.setMargins(0, 0, 0, margin / 2);
        settingsBtn.setOnClickListener(v -> openAccessibilitySettings());
        mainLayout.addView(settingsBtn, settingsBtnParams);
        
        // 显示悬浮窗
        Button floatBtn = new Button(this);
        floatBtn.setText("显示悬浮窗");
        LinearLayout.LayoutParams floatBtnParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            buttonHeight
        );
        floatBtnParams.setMargins(0, 0, 0, margin / 2);
        floatBtn.setOnClickListener(v -> showFloatingWindow());
        mainLayout.addView(floatBtn, floatBtnParams);
        
        // 打开脚本编辑器
        Button scriptBtn = new Button(this);
        scriptBtn.setText("打开脚本编辑器");
        LinearLayout.LayoutParams scriptBtnParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            buttonHeight
        );
        scriptBtnParams.setMargins(0, 0, 0, margin);
        scriptBtn.setOnClickListener(v -> openScriptEditor());
        mainLayout.addView(scriptBtn, scriptBtnParams);
        
        // 将主布局添加到根滚动视图
        rootScrollView.addView(mainLayout);
        
        setContentView(rootScrollView);
    }

    private void addClickPoint() {
        pointCounter++;
        
        // 获取屏幕尺寸
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        
        LinearLayout pointLayout = new LinearLayout(this);
        pointLayout.setOrientation(LinearLayout.HORIZONTAL);
        // 设置padding为屏幕宽度的1%
        int padding = (int) (screenWidth * 0.01);
        pointLayout.setPadding(padding, padding, padding, padding);
        
        // X坐标
        TextView xLabel = new TextView(this);
        xLabel.setText("X:");
        // 标签字体大小为屏幕宽度的2.5%
        xLabel.setTextSize(screenWidth * 0.025f);
        pointLayout.addView(xLabel);
        
        EditText xEdit = new EditText(this);
        xEdit.setText("500");
        // X编辑框宽度为屏幕宽度的15%
        int editWidth = (int) (screenWidth * 0.15);
        xEdit.setWidth(editWidth);
        pointLayout.addView(xEdit);
        
        // Y坐标
        TextView yLabel = new TextView(this);
        yLabel.setText("Y:");
        // 标签字体大小为屏幕宽度的2.5%
        yLabel.setTextSize(screenWidth * 0.025f);
        pointLayout.addView(yLabel);
        
        EditText yEdit = new EditText(this);
        yEdit.setText("500");
        // Y编辑框宽度为屏幕宽度的15%
        yEdit.setWidth(editWidth);
        pointLayout.addView(yEdit);
        
        // 描述
        TextView descLabel = new TextView(this);
        descLabel.setText("描述:");
        // 标签字体大小为屏幕宽度的2.5%
        descLabel.setTextSize(screenWidth * 0.025f);
        pointLayout.addView(descLabel);
        
        EditText descEdit = new EditText(this);
        descEdit.setText("点击点" + pointCounter);
        // 描述编辑框宽度为屏幕宽度的30%
        int descWidth = (int) (screenWidth * 0.3);
        descEdit.setWidth(descWidth);
        pointLayout.addView(descEdit);
        
        // 删除按钮
        Button deleteBtn = new Button(this);
        deleteBtn.setText("删除");
        // 删除按钮宽度为屏幕宽度的12%
        int deleteWidth = (int) (screenWidth * 0.12);
        // 删除按钮高度为屏幕高度的4%
        int deleteHeight = (int) (screenHeight * 0.04);
        LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(
            deleteWidth,
            deleteHeight
        );
        deleteParams.setMargins((int)(screenWidth * 0.01), 0, 0, 0);
        deleteBtn.setLayoutParams(deleteParams);
        deleteBtn.setOnClickListener(v -> {
            clickPointsLayout.removeView(pointLayout);
            updateClickPointsList();
        });
        pointLayout.addView(deleteBtn);
        
        clickPointsLayout.addView(pointLayout);
    }

    private void updateClickPointsList() {
        List<AutoClickService.ClickPoint> newPoints = new ArrayList<>();
        boolean hasError = false;
        
        for (int i = 0; i < clickPointsLayout.getChildCount(); i++) {
            LinearLayout pointLayout = (LinearLayout) clickPointsLayout.getChildAt(i);
            
            EditText xEdit = (EditText) pointLayout.getChildAt(1);
            EditText yEdit = (EditText) pointLayout.getChildAt(3);
            EditText descEdit = (EditText) pointLayout.getChildAt(5);
            
            try {
                float x = Float.parseFloat(xEdit.getText().toString());
                float y = Float.parseFloat(yEdit.getText().toString());
                String desc = descEdit.getText().toString();
                
                newPoints.add(new AutoClickService.ClickPoint(x, y, desc));
            } catch (NumberFormatException e) {
                hasError = true;
                Toast.makeText(this, "坐标格式错误: " + (i + 1), Toast.LENGTH_SHORT).show();
            }
        }
        
        if (!hasError) {
            clickPoints.clear();
            clickPoints.addAll(newPoints);
        }
    }

    private void startAutoClick() {
        AutoClickService service = AutoClickService.getInstance();
        if (service == null) {
            Toast.makeText(this, "请先开启辅助功能服务", Toast.LENGTH_LONG).show();
            openAccessibilitySettings();
            return;
        }

        updateClickPointsList();
        
        if (clickPoints.isEmpty()) {
            Toast.makeText(this, "请先添加点击点", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int repeatCount = Integer.parseInt(repeatCountEdit.getText().toString());
            long interval = Long.parseLong(intervalEdit.getText().toString());
            
            service.setClickPoints(clickPoints);
            service.setRepeatCount(repeatCount);
            service.setInterval(interval);
            service.startAutoClick();
            
            Toast.makeText(this, "自动点击开始", Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "参数格式错误", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopAutoClick() {
        AutoClickService service = AutoClickService.getInstance();
        if (service != null) {
            service.stopAutoClick();
            Toast.makeText(this, "自动点击停止", Toast.LENGTH_SHORT).show();
        }
    }

    private void openAccessibilitySettings() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
        Toast.makeText(this, "请开启自动点击器辅助功能", Toast.LENGTH_LONG).show();
    }

    private void showFloatingWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
                return;
            }
        }
        
        Intent intent = new Intent(this, EnhancedFloatingWindowService.class);
        startService(intent);
        Toast.makeText(this, "增强悬浮窗已显示", Toast.LENGTH_SHORT).show();
    }

    private void checkPermissions() {
        // 检查悬浮窗权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
            }
        }
        
        // 检查存储权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_STORAGE_PERMISSION);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "悬浮窗权限已授予", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "需要悬浮窗权限才能使用", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "存储权限已授予", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void openScriptEditor() {
        Intent intent = new Intent(this, ScriptEditorActivity.class);
        startActivity(intent);
    }
    
    private void startCoordinatePicker() {
        Toast.makeText(this, "点击屏幕任意位置获取坐标", Toast.LENGTH_LONG).show();
        
        // 创建一个透明的覆盖层来捕获点击
        View overlayView = new View(this);
        overlayView.setBackgroundColor(0x80000000);
        
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        );
        
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.addView(overlayView, params);
        
        overlayView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                lastX = event.getX();
                lastY = event.getY();
                
                windowManager.removeView(overlayView);
                
                // 自动添加点击点并填入坐标
                addClickPointWithCoordinate(lastX, lastY);
                
                Toast.makeText(this, "已获取坐标: X=" + (int)lastX + ", Y=" + (int)lastY, Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }
    
    private void addClickPointWithCoordinate(float x, float y) {
        pointCounter++;
        
        // 获取屏幕尺寸
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        
        LinearLayout pointLayout = new LinearLayout(this);
        pointLayout.setOrientation(LinearLayout.HORIZONTAL);
        // 设置padding为屏幕宽度的1%
        int padding = (int) (screenWidth * 0.01);
        pointLayout.setPadding(padding, padding, padding, padding);
        
        // X坐标
        TextView xLabel = new TextView(this);
        xLabel.setText("X:");
        // 标签字体大小为屏幕宽度的2.5%
        xLabel.setTextSize(screenWidth * 0.025f);
        pointLayout.addView(xLabel);
        
        EditText xEdit = new EditText(this);
        xEdit.setText(String.valueOf((int)x));
        // X编辑框宽度为屏幕宽度的15%
        int editWidth = (int) (screenWidth * 0.15);
        xEdit.setWidth(editWidth);
        pointLayout.addView(xEdit);
        
        // Y坐标
        TextView yLabel = new TextView(this);
        yLabel.setText("Y:");
        // 标签字体大小为屏幕宽度的2.5%
        yLabel.setTextSize(screenWidth * 0.025f);
        pointLayout.addView(yLabel);
        
        EditText yEdit = new EditText(this);
        yEdit.setText(String.valueOf((int)y));
        // Y编辑框宽度为屏幕宽度的15%
        yEdit.setWidth(editWidth);
        pointLayout.addView(yEdit);
        
        // 描述
        TextView descLabel = new TextView(this);
        descLabel.setText("描述:");
        // 标签字体大小为屏幕宽度的2.5%
        descLabel.setTextSize(screenWidth * 0.025f);
        pointLayout.addView(descLabel);
        
        EditText descEdit = new EditText(this);
        descEdit.setText("点击点" + pointCounter);
        // 描述编辑框宽度为屏幕宽度的30%
        int descWidth = (int) (screenWidth * 0.3);
        descEdit.setWidth(descWidth);
        pointLayout.addView(descEdit);
        
        // 删除按钮
        Button deleteBtn = new Button(this);
        deleteBtn.setText("删除");
        // 删除按钮宽度为屏幕宽度的12%
        int deleteWidth = (int) (screenWidth * 0.12);
        // 删除按钮高度为屏幕高度的4%
        int deleteHeight = (int) (screenHeight * 0.04);
        LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(
            deleteWidth,
            deleteHeight
        );
        deleteParams.setMargins((int)(screenWidth * 0.01), 0, 0, 0);
        deleteBtn.setLayoutParams(deleteParams);
        deleteBtn.setOnClickListener(v -> {
            clickPointsLayout.removeView(pointLayout);
            updateClickPointsList();
        });
        pointLayout.addView(deleteBtn);
        
        clickPointsLayout.addView(pointLayout);
    }
}
