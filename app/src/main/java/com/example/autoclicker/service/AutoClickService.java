package com.example.autoclicker.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.example.autoclicker.model.ClickScript;
import com.example.autoclicker.utils.SmartElementFinder;
import com.example.autoclicker.utils.ActionRecorder;

import java.util.ArrayList;
import java.util.List;

public class AutoClickService extends AccessibilityService {
    private static final String TAG = "AutoClickService";
    private static AutoClickService instance;
    private boolean isRunning = false;
    private Handler handler = new Handler(Looper.getMainLooper());
    private List<ClickPoint> clickPoints = new ArrayList<>();
    private int currentPointIndex = 0;
    private int repeatCount = 1;
    private int currentRepeat = 0;
    private long interval = 1000;
    private SmartElementFinder elementFinder;
    private ActionRecorder actionRecorder;
    private ClickScript currentScript;
    private int currentStepIndex = 0;
    private boolean isExecutingScript = false; // 默认间隔1秒

    public static AutoClickService getInstance() {
        return instance;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 处理辅助功能事件
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "服务中断");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        elementFinder = new SmartElementFinder(this);
        actionRecorder = new ActionRecorder(this);
        Log.d(TAG, "服务创建");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
        stopAutoClick();
        Log.d(TAG, "服务销毁");
    }

    /**
     * 设置点击点列表
     */
    public void setClickPoints(List<ClickPoint> points) {
        this.clickPoints = points;
        this.currentPointIndex = 0;
        this.currentRepeat = 0;
    }

    /**
     * 设置重复次数
     */
    public void setRepeatCount(int count) {
        this.repeatCount = count;
        this.currentRepeat = 0;
    }

    /**
     * 设置点击间隔（毫秒）
     */
    public void setInterval(long interval) {
        this.interval = interval;
    }

    /**
     * 开始自动点击
     */
    public void startAutoClick() {
        if (clickPoints.isEmpty()) {
            Log.e(TAG, "没有设置点击点");
            return;
        }

        if (isRunning) {
            Log.w(TAG, "自动点击已在运行中");
            return;
        }

        isRunning = true;
        currentPointIndex = 0;
        currentRepeat = 0;
        
        Log.d(TAG, "开始自动点击");
        performNextClick();
    }

    /**
     * 停止自动点击
     */
    public void stopAutoClick() {
        isRunning = false;
        handler.removeCallbacksAndMessages(null);
        Log.d(TAG, "停止自动点击");
    }

    /**
     * 执行下一个点击
     */
    private void performNextClick() {
        if (!isRunning) {
            return;
        }

        if (clickPoints.isEmpty()) {
            Log.e(TAG, "点击点列表为空");
            stopAutoClick();
            return;
        }

        if (currentPointIndex >= clickPoints.size()) {
            currentPointIndex = 0;
            currentRepeat++;
            
            if (currentRepeat >= repeatCount) {
                Log.d(TAG, "自动点击完成");
                stopAutoClick();
                return;
            }
        }

        ClickPoint point = clickPoints.get(currentPointIndex);
        performClick(point.x, point.y);
        
        currentPointIndex++;
        
        // 安排下一次点击
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                performNextClick();
            }
        }, interval);
    }

    /**
     * 执行点击操作
     */
    public boolean performClick(float x, float y) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Path path = new Path();
            path.moveTo(x, y);
            
            GestureDescription.Builder builder = new GestureDescription.Builder();
            builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 100));
            
            GestureDescription gesture = builder.build();
            boolean result = dispatchGesture(gesture, null, null);
            
            if (result) {
                Log.d(TAG, "点击成功: (" + x + ", " + y + ")");
            } else {
                Log.e(TAG, "点击失败: (" + x + ", " + y + ")");
            }
            
            return result;
        }
        return false;
    }

    /**
     * 执行滑动操作
     */
    public boolean performSwipe(float startX, float startY, float endX, float endY, long duration) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Path path = new Path();
            path.moveTo(startX, startY);
            path.lineTo(endX, endY);
            
            GestureDescription.Builder builder = new GestureDescription.Builder();
            builder.addStroke(new GestureDescription.StrokeDescription(path, 0, duration));
            
            GestureDescription gesture = builder.build();
            return dispatchGesture(gesture, null, null);
        }
        return false;
    }

    /**
     * 执行长按操作
     */
    public boolean performLongClick(float x, float y, long duration) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Path path = new Path();
            path.moveTo(x, y);
            
            GestureDescription.Builder builder = new GestureDescription.Builder();
            builder.addStroke(new GestureDescription.StrokeDescription(path, 0, duration));
            
            GestureDescription gesture = builder.build();
            return dispatchGesture(gesture, null, null);
        }
        return false;
    }

    /**
     * 查找指定文本的节点
     */
    public AccessibilityNodeInfo findNodeByText(String text) {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) {
            return null;
        }

        List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByText(text);
        if (!nodes.isEmpty()) {
            return nodes.get(0);
        }

        return null;
    }

    /**
     * 查找指定ID的节点
     */
    public AccessibilityNodeInfo findNodeById(String viewId) {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) {
            return null;
        }

        List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByViewId(viewId);
        if (!nodes.isEmpty()) {
            return nodes.get(0);
        }

        return null;
    }

    /**
     * 点击指定节点
     */
    public boolean clickNode(AccessibilityNodeInfo node) {
        if (node == null) {
            return false;
        }

        Rect bounds = new Rect();
        node.getBoundsInScreen(bounds);
        float x = bounds.centerX();
        float y = bounds.centerY();
        
        return performClick(x, y);
    }

    /**
     * 获取屏幕尺寸
     */
    public int[] getScreenSize() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
            getDisplay().getRealMetrics(metrics);
            return new int[]{
                metrics.widthPixels,
                metrics.heightPixels
            };
        }
        return new int[]{1080, 1920}; // 默认值
    }

    /**
     * 检查服务是否正在运行
     */
    public boolean isServiceRunning() {
        return isRunning;
    }

    public SmartElementFinder getElementFinder() {
        return elementFinder;
    }

    public ActionRecorder getActionRecorder() {
        return actionRecorder;
    }

    public void executeScript(ClickScript script) {
        if (script == null || script.getStepCount() == 0) {
            Log.e(TAG, "脚本为空或没有步骤");
            return;
        }

        if (isExecutingScript) {
            Log.w(TAG, "脚本正在执行中");
            return;
        }

        currentScript = script;
        currentStepIndex = 0;
        isExecutingScript = true;
        
        Log.d(TAG, "开始执行脚本: " + script.getName());
        executeNextScriptStep();
    }

    private void executeNextScriptStep() {
        if (!isExecutingScript) {
            return;
        }

        if (currentScript == null || currentScript.getStepCount() == 0) {
            Log.e(TAG, "脚本为空或没有步骤");
            stopScriptExecution();
            return;
        }

        if (currentStepIndex >= currentScript.getStepCount()) {
            Log.d(TAG, "脚本执行完成");
            stopScriptExecution();
            return;
        }

        ClickScript.ClickStep step = currentScript.getStep(currentStepIndex);
        executeScriptStep(step);
        
        currentStepIndex++;
        
        long delay = step.getDelay() > 0 ? step.getDelay() : currentScript.getClickInterval();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                executeNextScriptStep();
            }
        }, delay);
    }

    private void executeScriptStep(ClickScript.ClickStep step) {
        switch (step.getType()) {
            case CLICK:
                performClick(step.getX(), step.getY());
                break;
            case LONG_CLICK:
                performLongClick(step.getX(), step.getY(), currentScript.getClickDuration());
                break;
            case SWIPE:
                performSwipe(step.getX(), step.getY(), step.getX(), step.getY() - 500, 500);
                break;
            case SCROLL:
                performSwipe(step.getX(), step.getY(), step.getX(), step.getY() - 500, 500);
                break;
            case WAIT:
                break;
        }
        
        Log.d(TAG, "执行步骤 " + currentStepIndex + ": " + step.getDescription());
    }

    public void stopScriptExecution() {
        isExecutingScript = false;
        handler.removeCallbacksAndMessages(null);
        currentScript = null;
        currentStepIndex = 0;
        Log.d(TAG, "停止脚本执行");
    }

    public void smartClickByText(String text, final SmartClickCallback callback) {
        elementFinder.findElementByText(text, new SmartElementFinder.FindCallback() {
            @Override
            public void onElementFound(AccessibilityNodeInfo node, String matchInfo) {
                Rect bounds = new Rect();
                node.getBoundsInScreen(bounds);
                boolean success = performClick(bounds.centerX(), bounds.centerY());
                
                if (callback != null) {
                    if (success) {
                        callback.onSuccess(bounds.centerX(), bounds.centerY(), matchInfo);
                    } else {
                        callback.onFailure("点击失败");
                    }
                }
            }

            @Override
            public void onElementNotFound(String reason) {
                if (callback != null) {
                    callback.onFailure(reason);
                }
            }
        });
    }

    public void smartClickById(String viewId, final SmartClickCallback callback) {
        elementFinder.findElementById(viewId, new SmartElementFinder.FindCallback() {
            @Override
            public void onElementFound(AccessibilityNodeInfo node, String matchInfo) {
                Rect bounds = new Rect();
                node.getBoundsInScreen(bounds);
                boolean success = performClick(bounds.centerX(), bounds.centerY());
                
                if (callback != null) {
                    if (success) {
                        callback.onSuccess(bounds.centerX(), bounds.centerY(), matchInfo);
                    } else {
                        callback.onFailure("点击失败");
                    }
                }
            }

            @Override
            public void onElementNotFound(String reason) {
                if (callback != null) {
                    callback.onFailure(reason);
                }
            }
        });
    }

    public void smartClickByContentDescription(String description, final SmartClickCallback callback) {
        elementFinder.findElementByContentDescription(description, new SmartElementFinder.FindCallback() {
            @Override
            public void onElementFound(AccessibilityNodeInfo node, String matchInfo) {
                Rect bounds = new Rect();
                node.getBoundsInScreen(bounds);
                boolean success = performClick(bounds.centerX(), bounds.centerY());
                
                if (callback != null) {
                    if (success) {
                        callback.onSuccess(bounds.centerX(), bounds.centerY(), matchInfo);
                    } else {
                        callback.onFailure("点击失败");
                    }
                }
            }

            @Override
            public void onElementNotFound(String reason) {
                if (callback != null) {
                    callback.onFailure(reason);
                }
            }
        });
    }

    public interface SmartClickCallback {
        void onSuccess(float x, float y, String info);
        void onFailure(String reason);
    }

    /**
     * 点击点数据类
     */
    public static class ClickPoint {
        public float x;
        public float y;
        public String description;

        public ClickPoint(float x, float y, String description) {
            this.x = x;
            this.y = y;
            this.description = description;
        }
    }
}