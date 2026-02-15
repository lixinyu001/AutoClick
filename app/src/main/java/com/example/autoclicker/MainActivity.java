package com.example.autoclicker;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 创建简单的UI布局
        createLayout();
        
        // 检查权限
        checkPermissions();
    }

    private void createLayout() {
        // 主布局
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(20, 20, 20, 20);
        
        // 标题
        TextView titleText = new TextView(this);
        titleText.setText("自动点击器");
        titleText.setTextSize(24);
        titleText.setPadding(0, 0, 0, 20);
        mainLayout.addView(titleText);
        
        // 添加点击点按钮
        Button addPointBtn = new Button(this);
        addPointBtn.setText("添加点击点");
        addPointBtn.setOnClickListener(v -> addClickPoint());
        mainLayout.addView(addPointBtn);
        
        // 点击点列表
        ScrollView scrollView = new ScrollView(this);
        clickPointsLayout = new LinearLayout(this);
        clickPointsLayout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(clickPointsLayout);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            400
        ));
        mainLayout.addView(scrollView);
        
        // 重复次数
        TextView repeatLabel = new TextView(this);
        repeatLabel.setText("重复次数:");
        mainLayout.addView(repeatLabel);
        
        repeatCountEdit = new EditText(this);
        repeatCountEdit.setText("1");
        repeatCountEdit.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        mainLayout.addView(repeatCountEdit);
        
        // 点击间隔
        TextView intervalLabel = new TextView(this);
        intervalLabel.setText("点击间隔(毫秒):");
        mainLayout.addView(intervalLabel);
        
        intervalEdit = new EditText(this);
        intervalEdit.setText("1000");
        intervalEdit.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        mainLayout.addView(intervalEdit);
        
        // 开始按钮
        Button startBtn = new Button(this);
        startBtn.setText("开始自动点击");
        startBtn.setOnClickListener(v -> startAutoClick());
        mainLayout.addView(startBtn);
        
        // 停止按钮
        Button stopBtn = new Button(this);
        stopBtn.setText("停止自动点击");
        stopBtn.setOnClickListener(v -> stopAutoClick());
        mainLayout.addView(stopBtn);
        
        // 打开辅助功能设置
        Button settingsBtn = new Button(this);
        settingsBtn.setText("打开辅助功能设置");
        settingsBtn.setOnClickListener(v -> openAccessibilitySettings());
        mainLayout.addView(settingsBtn);
        
        // 显示悬浮窗
        Button floatBtn = new Button(this);
        floatBtn.setText("显示悬浮窗");
        floatBtn.setOnClickListener(v -> showFloatingWindow());
        mainLayout.addView(floatBtn);
        
        // 打开脚本编辑器
        Button scriptBtn = new Button(this);
        scriptBtn.setText("打开脚本编辑器");
        scriptBtn.setOnClickListener(v -> openScriptEditor());
        mainLayout.addView(scriptBtn);
        
        setContentView(mainLayout);
    }

    private void addClickPoint() {
        pointCounter++;
        
        LinearLayout pointLayout = new LinearLayout(this);
        pointLayout.setOrientation(LinearLayout.HORIZONTAL);
        pointLayout.setPadding(10, 10, 10, 10);
        
        // X坐标
        TextView xLabel = new TextView(this);
        xLabel.setText("X:");
        pointLayout.addView(xLabel);
        
        EditText xEdit = new EditText(this);
        xEdit.setText("500");
        xEdit.setWidth(100);
        pointLayout.addView(xEdit);
        
        // Y坐标
        TextView yLabel = new TextView(this);
        yLabel.setText("Y:");
        pointLayout.addView(yLabel);
        
        EditText yEdit = new EditText(this);
        yEdit.setText("500");
        yEdit.setWidth(100);
        pointLayout.addView(yEdit);
        
        // 描述
        TextView descLabel = new TextView(this);
        descLabel.setText("描述:");
        pointLayout.addView(descLabel);
        
        EditText descEdit = new EditText(this);
        descEdit.setText("点击点" + pointCounter);
        descEdit.setWidth(200);
        pointLayout.addView(descEdit);
        
        // 删除按钮
        Button deleteBtn = new Button(this);
        deleteBtn.setText("删除");
        deleteBtn.setOnClickListener(v -> {
            clickPointsLayout.removeView(pointLayout);
            updateClickPointsList();
        });
        pointLayout.addView(deleteBtn);
        
        clickPointsLayout.addView(pointLayout);
    }

    private void updateClickPointsList() {
        clickPoints.clear();
        for (int i = 0; i < clickPointsLayout.getChildCount(); i++) {
            LinearLayout pointLayout = (LinearLayout) clickPointsLayout.getChildAt(i);
            
            EditText xEdit = (EditText) pointLayout.getChildAt(1);
            EditText yEdit = (EditText) pointLayout.getChildAt(3);
            EditText descEdit = (EditText) pointLayout.getChildAt(5);
            
            try {
                float x = Float.parseFloat(xEdit.getText().toString());
                float y = Float.parseFloat(yEdit.getText().toString());
                String desc = descEdit.getText().toString();
                
                clickPoints.add(new AutoClickService.ClickPoint(x, y, desc));
            } catch (NumberFormatException e) {
                Toast.makeText(this, "坐标格式错误", Toast.LENGTH_SHORT).show();
            }
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
}