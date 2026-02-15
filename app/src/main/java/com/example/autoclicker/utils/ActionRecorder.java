package com.example.autoclicker.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.util.ArrayList;
import java.util.List;

public class ActionRecorder {
    private static final String TAG = "ActionRecorder";
    
    private boolean isRecording = false;
    private List<RecordedAction> recordedActions = new ArrayList<>();
    private long recordingStartTime = 0;
    private long lastActionTime = 0;
    private AccessibilityService service;
    private Handler handler = new Handler(Looper.getMainLooper());
    
    private static final long MIN_ACTION_INTERVAL = 100;
    private static final float MOVE_THRESHOLD = 10.0f;

    public ActionRecorder(AccessibilityService service) {
        this.service = service;
    }

    public void startRecording() {
        if (isRecording) {
            Log.w(TAG, "录制已在进行中");
            return;
        }
        
        isRecording = true;
        recordedActions.clear();
        recordingStartTime = System.currentTimeMillis();
        lastActionTime = recordingStartTime;
        
        Log.d(TAG, "开始录制");
    }

    public void stopRecording() {
        if (!isRecording) {
            Log.w(TAG, "录制未在进行中");
            return;
        }
        
        isRecording = false;
        Log.d(TAG, "停止录制，共录制 " + recordedActions.size() + " 个动作");
    }

    public boolean isRecording() {
        return isRecording;
    }

    public List<RecordedAction> getRecordedActions() {
        return new ArrayList<>(recordedActions);
    }

    public void clearRecording() {
        recordedActions.clear();
        Log.d(TAG, "清空录制");
    }

    public void recordClick(float x, float y) {
        if (!isRecording) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        long delay = currentTime - lastActionTime;
        
        if (delay < MIN_ACTION_INTERVAL) {
            return;
        }

        RecordedAction action = new RecordedAction(
            ActionType.CLICK,
            x, y,
            delay,
            "点击"
        );
        
        recordedActions.add(action);
        lastActionTime = currentTime;
        
        Log.d(TAG, "录制点击: (" + x + ", " + y + ")");
    }

    public void recordLongClick(float x, float y, long duration) {
        if (!isRecording) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        long delay = currentTime - lastActionTime;
        
        if (delay < MIN_ACTION_INTERVAL) {
            return;
        }

        RecordedAction action = new RecordedAction(
            ActionType.LONG_CLICK,
            x, y,
            delay,
            "长按"
        );
        action.setDuration(duration);
        
        recordedActions.add(action);
        lastActionTime = currentTime;
        
        Log.d(TAG, "录制长按: (" + x + ", " + y + "), 时长: " + duration);
    }

    public void recordSwipe(float startX, float startY, float endX, float endY, long duration) {
        if (!isRecording) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        long delay = currentTime - lastActionTime;
        
        if (delay < MIN_ACTION_INTERVAL) {
            return;
        }

        RecordedAction action = new RecordedAction(
            ActionType.SWIPE,
            startX, startY,
            delay,
            "滑动"
        );
        action.setEndX(endX);
        action.setEndY(endY);
        action.setDuration(duration);
        
        recordedActions.add(action);
        lastActionTime = currentTime;
        
        Log.d(TAG, "录制滑动: (" + startX + ", " + startY + ") -> (" + endX + ", " + endY + ")");
    }

    public void recordScroll(float startX, float startY, float endX, float endY) {
        if (!isRecording) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        long delay = currentTime - lastActionTime;
        
        if (delay < MIN_ACTION_INTERVAL) {
            return;
        }

        RecordedAction action = new RecordedAction(
            ActionType.SCROLL,
            startX, startY,
            delay,
            "滚动"
        );
        action.setEndX(endX);
        action.setEndY(endY);
        action.setDuration(500);
        
        recordedActions.add(action);
        lastActionTime = currentTime;
        
        Log.d(TAG, "录制滚动: (" + startX + ", " + startY + ") -> (" + endX + ", " + endY + ")");
    }

    public void recordWait(long delay) {
        if (!isRecording) {
            return;
        }

        RecordedAction action = new RecordedAction(
            ActionType.WAIT,
            0, 0,
            delay,
            "等待"
        );
        
        recordedActions.add(action);
        lastActionTime = System.currentTimeMillis();
        
        Log.d(TAG, "录制等待: " + delay + "ms");
    }

    public void recordElementClick(String text, String description) {
        if (!isRecording) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        long delay = currentTime - lastActionTime;
        
        if (delay < MIN_ACTION_INTERVAL) {
            return;
        }

        RecordedAction action = new RecordedAction(
            ActionType.ELEMENT_CLICK,
            0, 0,
            delay,
            "点击元素: " + text
        );
        action.setElementText(text);
        action.setElementDescription(description);
        
        recordedActions.add(action);
        lastActionTime = currentTime;
        
        Log.d(TAG, "录制元素点击: " + text);
    }

    public void recordElementClickById(String viewId, String description) {
        if (!isRecording) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        long delay = currentTime - lastActionTime;
        
        if (delay < MIN_ACTION_INTERVAL) {
            return;
        }

        RecordedAction action = new RecordedAction(
            ActionType.ELEMENT_CLICK_BY_ID,
            0, 0,
            delay,
            "点击元素(ID): " + viewId
        );
        action.setViewId(viewId);
        action.setElementDescription(description);
        
        recordedActions.add(action);
        lastActionTime = currentTime;
        
        Log.d(TAG, "录制元素点击(ID): " + viewId);
    }

    public ClickScript convertToScript(String scriptName) {
        ClickScript script = new ClickScript(scriptName);
        
        for (RecordedAction action : recordedActions) {
            ClickScript.ClickStep step = convertActionToStep(action);
            if (step != null) {
                script.addStep(step);
            }
        }
        
        return script;
    }

    private ClickScript.ClickStep convertActionToStep(RecordedAction action) {
        ClickScript.StepType stepType;
        
        switch (action.getActionType()) {
            case CLICK:
                stepType = ClickScript.StepType.CLICK;
                break;
            case LONG_CLICK:
                stepType = ClickScript.StepType.LONG_CLICK;
                break;
            case SWIPE:
                stepType = ClickScript.StepType.SWIPE;
                break;
            case SCROLL:
                stepType = ClickScript.StepType.SCROLL;
                break;
            case WAIT:
                stepType = ClickScript.StepType.WAIT;
                break;
            case ELEMENT_CLICK:
            case ELEMENT_CLICK_BY_ID:
                stepType = ClickScript.StepType.CLICK;
                break;
            default:
                return null;
        }
        
        ClickScript.ClickStep step = new ClickScript.ClickStep(
            action.getX(),
            action.getY(),
            stepType,
            action.getDelay(),
            action.getDescription()
        );
        
        if (action.getDuration() > 0) {
            step.setRepeat((int) (action.getDuration() / 100));
        }
        
        return step;
    }

    public void replayRecording() {
        if (recordedActions.isEmpty()) {
            Log.w(TAG, "没有录制的动作");
            return;
        }
        
        Log.d(TAG, "开始回放录制，共 " + recordedActions.size() + " 个动作");
        
        for (int i = 0; i < recordedActions.size(); i++) {
            final RecordedAction action = recordedActions.get(i);
            final int index = i;
            
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    executeAction(action);
                    Log.d(TAG, "回放动作 " + (index + 1) + "/" + recordedActions.size());
                }
            }, action.getDelay());
        }
    }

    private void executeAction(RecordedAction action) {
        switch (action.getActionType()) {
            case CLICK:
                performClick(action.getX(), action.getY());
                break;
            case LONG_CLICK:
                performLongClick(action.getX(), action.getY(), action.getDuration());
                break;
            case SWIPE:
                performSwipe(action.getX(), action.getY(), 
                    action.getEndX(), action.getEndY(), action.getDuration());
                break;
            case SCROLL:
                performSwipe(action.getX(), action.getY(), 
                    action.getEndX(), action.getEndY(), action.getDuration());
                break;
            case WAIT:
                break;
            case ELEMENT_CLICK:
                findAndClickElement(action.getElementText());
                break;
            case ELEMENT_CLICK_BY_ID:
                findAndClickElementById(action.getViewId());
                break;
        }
    }

    private void performClick(float x, float y) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Path path = new Path();
            path.moveTo(x, y);
            
            GestureDescription.Builder builder = new GestureDescription.Builder();
            builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 100));
            
            GestureDescription gesture = builder.build();
            service.dispatchGesture(gesture, null, null);
        }
    }

    private void performLongClick(float x, float y, long duration) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Path path = new Path();
            path.moveTo(x, y);
            
            GestureDescription.Builder builder = new GestureDescription.Builder();
            builder.addStroke(new GestureDescription.StrokeDescription(path, 0, duration));
            
            GestureDescription gesture = builder.build();
            service.dispatchGesture(gesture, null, null);
        }
    }

    private void performSwipe(float startX, float startY, float endX, float endY, long duration) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Path path = new Path();
            path.moveTo(startX, startY);
            path.lineTo(endX, endY);
            
            GestureDescription.Builder builder = new GestureDescription.Builder();
            builder.addStroke(new GestureDescription.StrokeDescription(path, 0, duration));
            
            GestureDescription gesture = builder.build();
            service.dispatchGesture(gesture, null, null);
        }
    }

    private void findAndClickElement(String text) {
        SmartElementFinder finder = new SmartElementFinder(service);
        finder.findElementByText(text, new SmartElementFinder.FindCallback() {
            @Override
            public void onElementFound(android.view.accessibility.AccessibilityNodeInfo node, String matchInfo) {
                Rect bounds = new Rect();
                node.getBoundsInScreen(bounds);
                performClick(bounds.centerX(), bounds.centerY());
            }

            @Override
            public void onElementNotFound(String reason) {
                Log.e(TAG, "找不到元素: " + reason);
            }
        });
    }

    private void findAndClickElementById(String viewId) {
        SmartElementFinder finder = new SmartElementFinder(service);
        finder.findElementById(viewId, new SmartElementFinder.FindCallback() {
            @Override
            public void onElementFound(android.view.accessibility.AccessibilityNodeInfo node, String matchInfo) {
                Rect bounds = new Rect();
                node.getBoundsInScreen(bounds);
                performClick(bounds.centerX(), bounds.centerY());
            }

            @Override
            public void onElementNotFound(String reason) {
                Log.e(TAG, "找不到元素: " + reason);
            }
        });
    }

    public enum ActionType {
        CLICK,
        LONG_CLICK,
        SWIPE,
        SCROLL,
        WAIT,
        ELEMENT_CLICK,
        ELEMENT_CLICK_BY_ID
    }

    public static class RecordedAction {
        private ActionType actionType;
        private float x;
        private float y;
        private float endX;
        private float endY;
        private long delay;
        private long duration;
        private String description;
        private String elementText;
        private String viewId;
        private String elementDescription;

        public RecordedAction(ActionType actionType, float x, float y, 
                long delay, String description) {
            this.actionType = actionType;
            this.x = x;
            this.y = y;
            this.delay = delay;
            this.description = description;
            this.duration = 0;
        }

        public ActionType getActionType() {
            return actionType;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public float getEndX() {
            return endX;
        }

        public void setEndX(float endX) {
            this.endX = endX;
        }

        public float getEndY() {
            return endY;
        }

        public void setEndY(float endY) {
            this.endY = endY;
        }

        public long getDelay() {
            return delay;
        }

        public long getDuration() {
            return duration;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }

        public String getDescription() {
            return description;
        }

        public String getElementText() {
            return elementText;
        }

        public void setElementText(String elementText) {
            this.elementText = elementText;
        }

        public String getViewId() {
            return viewId;
        }

        public void setViewId(String viewId) {
            this.viewId = viewId;
        }

        public String getElementDescription() {
            return elementDescription;
        }

        public void setElementDescription(String elementDescription) {
            this.elementDescription = elementDescription;
        }
    }
}
