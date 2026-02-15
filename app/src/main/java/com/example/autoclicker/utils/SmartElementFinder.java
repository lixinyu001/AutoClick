package com.example.autoclicker.utils;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;

public class SmartElementFinder {
    private static final String TAG = "SmartElementFinder";
    private AccessibilityService service;

    public SmartElementFinder(AccessibilityService service) {
        this.service = service;
    }

    public interface FindCallback {
        void onElementFound(AccessibilityNodeInfo node, String matchInfo);
        void onElementNotFound(String reason);
    }

    public void findElementByText(String text, FindCallback callback) {
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            callback.onElementNotFound("无法获取根节点");
            return;
        }

        List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByText(text);
        if (nodes.isEmpty()) {
            callback.onElementNotFound("未找到包含文本 \"" + text + "\" 的元素");
            return;
        }

        AccessibilityNodeInfo bestMatch = findBestMatch(nodes, text);
        if (bestMatch != null) {
            Rect bounds = new Rect();
            bestMatch.getBoundsInScreen(bounds);
            String info = String.format("文本: %s, 位置: (%d, %d)", 
                text, bounds.centerX(), bounds.centerY());
            callback.onElementFound(bestMatch, info);
        } else {
            callback.onElementNotFound("未找到最佳匹配");
        }
    }

    public void findElementById(String viewId, FindCallback callback) {
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            callback.onElementNotFound("无法获取根节点");
            return;
        }

        List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByViewId(viewId);
        if (nodes.isEmpty()) {
            callback.onElementNotFound("未找到ID为 \"" + viewId + "\" 的元素");
            return;
        }

        AccessibilityNodeInfo node = nodes.get(0);
        Rect bounds = new Rect();
        node.getBoundsInScreen(bounds);
        String info = String.format("ID: %s, 位置: (%d, %d)", 
            viewId, bounds.centerX(), bounds.centerY());
        callback.onElementFound(node, info);
    }

    public void findElementByContentDescription(String description, FindCallback callback) {
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            callback.onElementNotFound("无法获取根节点");
            return;
        }

        List<AccessibilityNodeInfo> matches = new ArrayList<>();
        findNodesByContentDescription(rootNode, description, matches);

        if (matches.isEmpty()) {
            callback.onElementNotFound("未找到内容描述为 \"" + description + "\" 的元素");
            return;
        }

        AccessibilityNodeInfo bestMatch = findBestMatch(matches, description);
        if (bestMatch != null) {
            Rect bounds = new Rect();
            bestMatch.getBoundsInScreen(bounds);
            String info = String.format("内容描述: %s, 位置: (%d, %d)", 
                description, bounds.centerX(), bounds.centerY());
            callback.onElementFound(bestMatch, info);
        } else {
            callback.onElementNotFound("未找到最佳匹配");
        }
    }

    public void findElementByClassName(String className, FindCallback callback) {
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            callback.onElementNotFound("无法获取根节点");
            return;
        }

        List<AccessibilityNodeInfo> matches = new ArrayList<>();
        findNodesByClassName(rootNode, className, matches);

        if (matches.isEmpty()) {
            callback.onElementNotFound("未找到类名为 \"" + className + "\" 的元素");
            return;
        }

        AccessibilityNodeInfo node = matches.get(0);
        Rect bounds = new Rect();
        node.getBoundsInScreen(bounds);
        String info = String.format("类名: %s, 位置: (%d, %d)", 
            className, bounds.centerX(), bounds.centerY());
        callback.onElementFound(node, info);
    }

    public void findClickableElements(FindCallback callback) {
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            callback.onElementNotFound("无法获取根节点");
            return;
        }

        List<AccessibilityNodeInfo> matches = new ArrayList<>();
        findClickableNodes(rootNode, matches);

        if (matches.isEmpty()) {
            callback.onElementNotFound("未找到可点击的元素");
            return;
        }

        AccessibilityNodeInfo node = matches.get(0);
        Rect bounds = new Rect();
        node.getBoundsInScreen(bounds);
        String info = String.format("可点击元素, 位置: (%d, %d), 共找到 %d 个", 
            bounds.centerX(), bounds.centerY(), matches.size());
        callback.onElementFound(node, info);
    }

    private void findNodesByContentDescription(AccessibilityNodeInfo node, 
            String description, List<AccessibilityNodeInfo> matches) {
        if (node == null) {
            return;
        }

        CharSequence contentDesc = node.getContentDescription();
        if (contentDesc != null && contentDesc.toString().contains(description)) {
            matches.add(node);
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            findNodesByContentDescription(node.getChild(i), description, matches);
        }
    }

    private void findNodesByClassName(AccessibilityNodeInfo node, 
            String className, List<AccessibilityNodeInfo> matches) {
        if (node == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            String nodeClassName = node.getClassName() != null ? 
                node.getClassName().toString() : "";
            if (nodeClassName.contains(className)) {
                matches.add(node);
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            findNodesByClassName(node.getChild(i), className, matches);
        }
    }

    private void findClickableNodes(AccessibilityNodeInfo node, 
            List<AccessibilityNodeInfo> matches) {
        if (node == null) {
            return;
        }

        if (node.isClickable()) {
            matches.add(node);
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            findClickableNodes(node.getChild(i), matches);
        }
    }

    private AccessibilityNodeInfo findBestMatch(List<AccessibilityNodeInfo> nodes, String target) {
        if (nodes.isEmpty()) {
            return null;
        }

        AccessibilityNodeInfo bestMatch = null;
        int bestScore = 0;

        for (AccessibilityNodeInfo node : nodes) {
            int score = calculateMatchScore(node, target);
            if (score > bestScore) {
                bestScore = score;
                bestMatch = node;
            }
        }

        return bestMatch;
    }

    private int calculateMatchScore(AccessibilityNodeInfo node, String target) {
        int score = 0;
        String targetLower = target.toLowerCase();

        CharSequence text = node.getText();
        if (text != null) {
            String nodeText = text.toString().toLowerCase();
            if (nodeText.equals(targetLower)) {
                score += 100;
            } else if (nodeText.contains(targetLower)) {
                score += 50;
            }
        }

        CharSequence contentDesc = node.getContentDescription();
        if (contentDesc != null) {
            String desc = contentDesc.toString().toLowerCase();
            if (desc.equals(targetLower)) {
                score += 80;
            } else if (desc.contains(targetLower)) {
                score += 40;
            }
        }

        if (node.isClickable()) {
            score += 20;
        }

        if (node.isEnabled()) {
            score += 10;
        }

        Rect bounds = new Rect();
        node.getBoundsInScreen(bounds);
        if (bounds.width() > 0 && bounds.height() > 0) {
            score += 5;
        }

        return score;
    }

    public ElementInfo getElementInfo(AccessibilityNodeInfo node) {
        if (node == null) {
            return null;
        }

        ElementInfo info = new ElementInfo();
        
        Rect bounds = new Rect();
        node.getBoundsInScreen(bounds);
        info.setBounds(bounds);
        
        info.setText(node.getText() != null ? node.getText().toString() : "");
        info.setContentDescription(node.getContentDescription() != null ? 
            node.getContentDescription().toString() : "");
        info.setClassName(node.getClassName() != null ? 
            node.getClassName().toString() : "");
        info.setClickable(node.isClickable());
        info.setEnabled(node.isEnabled());
        info.setFocusable(node.isFocusable());
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            info.setViewIdResourceName(node.getViewIdResourceName());
        }

        return info;
    }

    public static class ElementInfo {
        private Rect bounds;
        private String text;
        private String contentDescription;
        private String className;
        private String viewIdResourceName;
        private boolean clickable;
        private boolean enabled;
        private boolean focusable;

        public Rect getBounds() {
            return bounds;
        }

        public void setBounds(Rect bounds) {
            this.bounds = bounds;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getContentDescription() {
            return contentDescription;
        }

        public void setContentDescription(String contentDescription) {
            this.contentDescription = contentDescription;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getViewIdResourceName() {
            return viewIdResourceName;
        }

        public void setViewIdResourceName(String viewIdResourceName) {
            this.viewIdResourceName = viewIdResourceName;
        }

        public boolean isClickable() {
            return clickable;
        }

        public void setClickable(boolean clickable) {
            this.clickable = clickable;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isFocusable() {
            return focusable;
        }

        public void setFocusable(boolean focusable) {
            this.focusable = focusable;
        }

        @Override
        public String toString() {
            return String.format("ElementInfo{text='%s', className='%s', clickable=%s, bounds=%s}", 
                text, className, clickable, bounds);
        }
    }
}
