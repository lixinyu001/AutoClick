package com.example.autoclicker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 脚本编辑Activity，用于创建和编辑点击脚本
 */
public class ScriptEditorActivity extends AppCompatActivity {
    private static final String EXTRA_SCRIPT_NAME = "script_name";
    private static final String EXTRA_SCRIPT_DATA = "script_data";
    private static final int REQUEST_ADD_STEP = 1001;

    private ClickScript currentScript;
    private List<ClickScript.ClickStep> stepList;
    private StepAdapter stepAdapter;
    private ListView stepListView;
    private EditText etScriptName;
    private EditText etRepeatCount;
    private EditText etClickInterval;
    private EditText etRandomDelay;
    private EditText etClickDuration;

    public static Intent createIntent(Activity activity, String scriptName) {
        Intent intent = new Intent(activity, ScriptEditorActivity.class);
        intent.putExtra(EXTRA_SCRIPT_NAME, scriptName);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_script_editor);

        // 初始化视图
        initViews();
        
        // 加载或创建脚本
        loadOrCreateScript();
        
        // 设置监听器
        setupListeners();
    }

    private void initViews() {
        etScriptName = findViewById(R.id.et_script_name);
        etRepeatCount = findViewById(R.id.et_repeat_count);
        etClickInterval = findViewById(R.id.et_click_interval);
        etRandomDelay = findViewById(R.id.et_random_delay);
        etClickDuration = findViewById(R.id.et_click_duration);
        
        stepListView = findViewById(R.id.lv_steps);
        stepList = new ArrayList<>();
        stepAdapter = new StepAdapter(this, stepList);
        stepListView.setAdapter(stepAdapter);
        
        // 设置长按监听器
        stepListView.setOnItemLongClickListener((parent, view, position, id) -> {
            showStepMenu(position);
            return true;
        });
    }

    private void loadOrCreateScript() {
        String scriptName = getIntent().getStringExtra(EXTRA_SCRIPT_NAME);
        
        if (scriptName != null) {
            // 加载现有脚本（这里简化处理，实际应该从存储中加载）
            currentScript = new ClickScript(scriptName);
            etScriptName.setText(scriptName);
            loadScriptSteps();
        } else {
            // 创建新脚本
            currentScript = new ClickScript("新脚本");
            etScriptName.setText("新脚本");
        }
        
        // 设置参数
        etRepeatCount.setText(String.valueOf(currentScript.getRepeatCount()));
        etClickInterval.setText(String.valueOf(currentScript.getClickInterval()));
        etRandomDelay.setText(String.valueOf(currentScript.getRandomDelay()));
        etClickDuration.setText(String.valueOf(currentScript.getClickDuration()));
    }

    private void loadScriptSteps() {
        stepList.clear();
        stepList.addAll(currentScript.getSteps());
        stepAdapter.notifyDataSetChanged();
    }

    private void setupListeners() {
        // 添加步骤按钮
        Button btnAddStep = findViewById(R.id.btn_add_step);
        btnAddStep.setOnClickListener(v -> showAddStepDialog());
        
        // 添加智能点击步骤按钮
        Button btnAddSmartStep = findViewById(R.id.btn_add_smart_step);
        if (btnAddSmartStep != null) {
            btnAddSmartStep.setOnClickListener(v -> addSmartClickStep());
        }
        
        // 保存脚本按钮
        Button btnSaveScript = findViewById(R.id.btn_save_script);
        btnSaveScript.setOnClickListener(v -> saveScript());
        
        // 测试脚本按钮
        Button btnTestScript = findViewById(R.id.btn_test_script);
        btnTestScript.setOnClickListener(v -> testScript());
        
        // 清空步骤按钮
        Button btnClearSteps = findViewById(R.id.btn_clear_steps);
        btnClearSteps.setOnClickListener(v -> clearAllSteps());
    }

    private void showAddStepDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_step, null);
        
        Spinner spStepType = dialogView.findViewById(R.id.sp_step_type);
        EditText etX = dialogView.findViewById(R.id.et_x);
        EditText etY = dialogView.findViewById(R.id.et_y);
        EditText etDelay = dialogView.findViewById(R.id.et_delay);
        EditText etRepeat = dialogView.findViewById(R.id.et_repeat);
        EditText etDescription = dialogView.findViewById(R.id.et_description);
        
        // 设置步骤类型
        ArrayAdapter<ClickScript.StepType> typeAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, ClickScript.StepType.values());
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStepType.setAdapter(typeAdapter);
        
        builder.setTitle("添加步骤")
               .setView(dialogView)
               .setPositiveButton("添加", (dialog, which) -> {
                   try {
                       float x = Float.parseFloat(etX.getText().toString());
                       float y = Float.parseFloat(etY.getText().toString());
                       long delay = Long.parseLong(etDelay.getText().toString());
                       int repeat = Integer.parseInt(etRepeat.getText().toString());
                       String description = etDescription.getText().toString();
                       
                       ClickScript.StepType type = 
                           (ClickScript.StepType) spStepType.getSelectedItem();
                       
                       ClickScript.ClickStep step = new ClickScript.ClickStep(
                           x, y, type, delay, description);
                       step.setRepeat(repeat);
                       
                       addStep(step);
                       
                   } catch (NumberFormatException e) {
                       Toast.makeText(this, "请输入有效的数字", Toast.LENGTH_SHORT).show();
                   }
               })
               .setNegativeButton("取消", null)
               .show();
    }

    private void addStep(ClickScript.ClickStep step) {
        stepList.add(step);
        currentScript.addStep(step);
        stepAdapter.notifyDataSetChanged();
        Toast.makeText(this, "步骤已添加", Toast.LENGTH_SHORT).show();
    }

    private void showStepMenu(final int position) {
        String[] options = {"编辑", "上移", "下移", "删除"};
        
        new AlertDialog.Builder(this)
            .setTitle("选择操作")
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: // 编辑
                        editStep(position);
                        break;
                    case 1: // 上移
                        currentScript.moveStepUp(position);
                        loadScriptSteps();
                        break;
                    case 2: // 下移
                        currentScript.moveStepDown(position);
                        loadScriptSteps();
                        break;
                    case 3: // 删除
                        deleteStep(position);
                        break;
                }
            })
            .show();
    }

    private void editStep(int position) {
        ClickScript.ClickStep step = stepList.get(position);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_step, null);
        
        Spinner spStepType = dialogView.findViewById(R.id.sp_step_type);
        EditText etX = dialogView.findViewById(R.id.et_x);
        EditText etY = dialogView.findViewById(R.id.et_y);
        EditText etDelay = dialogView.findViewById(R.id.et_delay);
        EditText etRepeat = dialogView.findViewById(R.id.et_repeat);
        EditText etDescription = dialogView.findViewById(R.id.et_description);
        
        // 设置当前值
        etX.setText(String.valueOf(step.getX()));
        etY.setText(String.valueOf(step.getY()));
        etDelay.setText(String.valueOf(step.getDelay()));
        etRepeat.setText(String.valueOf(step.getRepeat()));
        etDescription.setText(step.getDescription());
        
        // 设置步骤类型
        ArrayAdapter<ClickScript.StepType> typeAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, ClickScript.StepType.values());
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStepType.setAdapter(typeAdapter);
        spStepType.setSelection(step.getType().ordinal());
        
        builder.setTitle("编辑步骤")
               .setView(dialogView)
               .setPositiveButton("保存", (dialog, which) -> {
                   try {
                       float x = Float.parseFloat(etX.getText().toString());
                       float y = Float.parseFloat(etY.getText().toString());
                       long delay = Long.parseLong(etDelay.getText().toString());
                       int repeat = Integer.parseInt(etRepeat.getText().toString());
                       String description = etDescription.getText().toString();
                       
                       ClickScript.StepType type = 
                           (ClickScript.StepType) spStepType.getSelectedItem();
                       
                       step.setX(x);
                       step.setY(y);
                       step.setType(type);
                       step.setDelay(delay);
                       step.setRepeat(repeat);
                       step.setDescription(description);
                       
                       currentScript.updateStep(position, step);
                       stepAdapter.notifyDataSetChanged();
                       
                       Toast.makeText(this, "步骤已更新", Toast.LENGTH_SHORT).show();
                       
                   } catch (NumberFormatException e) {
                       Toast.makeText(this, "请输入有效的数字", Toast.LENGTH_SHORT).show();
                   }
               })
               .setNegativeButton("取消", null)
               .show();
    }

    private void deleteStep(int position) {
        new AlertDialog.Builder(this)
            .setTitle("确认删除")
            .setMessage("确定要删除这个步骤吗？")
            .setPositiveButton("删除", (dialog, which) -> {
                stepList.remove(position);
                currentScript.removeStep(position);
                stepAdapter.notifyDataSetChanged();
                Toast.makeText(this, "步骤已删除", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private void clearAllSteps() {
        new AlertDialog.Builder(this)
            .setTitle("确认清空")
            .setMessage("确定要清空所有步骤吗？")
            .setPositiveButton("清空", (dialog, which) -> {
                stepList.clear();
                currentScript.clearSteps();
                stepAdapter.notifyDataSetChanged();
                Toast.makeText(this, "所有步骤已清空", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private void saveScript() {
        try {
            String name = etScriptName.getText().toString();
            if (name.isEmpty()) {
                Toast.makeText(this, "请输入脚本名称", Toast.LENGTH_SHORT).show();
                return;
            }
            
            long repeatCount = Long.parseLong(etRepeatCount.getText().toString());
            long clickInterval = Long.parseLong(etClickInterval.getText().toString());
            long randomDelay = Long.parseLong(etRandomDelay.getText().toString());
            long clickDuration = Long.parseLong(etClickDuration.getText().toString());
            
            currentScript.setName(name);
            currentScript.setRepeatCount(repeatCount);
            currentScript.setClickInterval(clickInterval);
            currentScript.setRandomDelay(randomDelay);
            currentScript.setClickDuration(clickDuration);
            
            // 这里应该保存到存储中，简化处理
            Toast.makeText(this, "脚本已保存: " + name, Toast.LENGTH_SHORT).show();
            
            // 返回结果
            Intent result = new Intent();
            result.putExtra("script_name", name);
            setResult(RESULT_OK, result);
            finish();
            
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入有效的数字", Toast.LENGTH_SHORT).show();
        }
    }

    private void testScript() {
        if (stepList.isEmpty()) {
            Toast.makeText(this, "请先添加步骤", Toast.LENGTH_SHORT).show();
            return;
        }
        
        AutoClickService service = AutoClickService.getInstance();
        if (service == null) {
            Toast.makeText(this, "请先开启辅助功能服务", Toast.LENGTH_LONG).show();
            openAccessibilitySettings();
            return;
        }
        
        Toast.makeText(this, "开始测试脚本，共 " + stepList.size() + " 个步骤", 
                       Toast.LENGTH_SHORT).show();
        
        service.executeScript(currentScript);
    }

    private void openAccessibilitySettings() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
        Toast.makeText(this, "请开启自动点击器辅助功能", Toast.LENGTH_LONG).show();
    }

    private void addSmartClickStep() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);
        
        TextView titleText = new TextView(this);
        titleText.setText("智能元素识别");
        titleText.setTextSize(18);
        titleText.setPadding(0, 0, 0, 20);
        layout.addView(titleText);
        
        Spinner spSearchType = new Spinner(this);
        String[] searchTypes = {"按文本查找", "按ID查找", "按内容描述查找"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, searchTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSearchType.setAdapter(adapter);
        layout.addView(spSearchType);
        
        EditText etSearchText = new EditText(this);
        etSearchText.setHint("输入搜索内容");
        layout.addView(etSearchText);
        
        Button btnFind = new Button(this);
        btnFind.setText("查找并添加");
        layout.addView(btnFind);
        
        builder.setView(layout);
        
        AlertDialog dialog = builder.create();
        
        btnFind.setOnClickListener(v -> {
            String searchText = etSearchText.getText().toString();
            if (searchText.isEmpty()) {
                Toast.makeText(this, "请输入搜索内容", Toast.LENGTH_SHORT).show();
                return;
            }
            
            int searchType = spSearchType.getSelectedItemPosition();
            findAndAddElement(searchType, searchText);
            dialog.dismiss();
        });
        
        dialog.show();
    }

    private void findAndAddElement(int searchType, String searchText) {
        AutoClickService service = AutoClickService.getInstance();
        if (service == null) {
            Toast.makeText(this, "请先开启辅助功能服务", Toast.LENGTH_LONG).show();
            openAccessibilitySettings();
            return;
        }
        
        switch (searchType) {
            case 0:
                service.smartClickByText(searchText, new AutoClickService.SmartClickCallback() {
                    @Override
                    public void onSuccess(float x, float y, String info) {
                        ClickScript.ClickStep step = new ClickScript.ClickStep(
                            x, y, ClickScript.StepType.CLICK, 0, "智能点击: " + searchText);
                        addStep(step);
                        runOnUiThread(() -> Toast.makeText(ScriptEditorActivity.this, 
                            "找到元素: " + info, Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onFailure(String reason) {
                        runOnUiThread(() -> Toast.makeText(ScriptEditorActivity.this, 
                            "未找到元素: " + reason, Toast.LENGTH_SHORT).show());
                    }
                });
                break;
            case 1:
                service.smartClickById(searchText, new AutoClickService.SmartClickCallback() {
                    @Override
                    public void onSuccess(float x, float y, String info) {
                        ClickScript.ClickStep step = new ClickScript.ClickStep(
                            x, y, ClickScript.StepType.CLICK, 0, "智能点击(ID): " + searchText);
                        addStep(step);
                        runOnUiThread(() -> Toast.makeText(ScriptEditorActivity.this, 
                            "找到元素: " + info, Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onFailure(String reason) {
                        runOnUiThread(() -> Toast.makeText(ScriptEditorActivity.this, 
                            "未找到元素: " + reason, Toast.LENGTH_SHORT).show());
                    }
                });
                break;
            case 2:
                service.smartClickByContentDescription(searchText, new AutoClickService.SmartClickCallback() {
                    @Override
                    public void onSuccess(float x, float y, String info) {
                        ClickScript.ClickStep step = new ClickScript.ClickStep(
                            x, y, ClickScript.StepType.CLICK, 0, "智能点击(描述): " + searchText);
                        addStep(step);
                        runOnUiThread(() -> Toast.makeText(ScriptEditorActivity.this, 
                            "找到元素: " + info, Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onFailure(String reason) {
                        runOnUiThread(() -> Toast.makeText(ScriptEditorActivity.this, 
                            "未找到元素: " + reason, Toast.LENGTH_SHORT).show());
                    }
                });
                break;
        }
    }
}