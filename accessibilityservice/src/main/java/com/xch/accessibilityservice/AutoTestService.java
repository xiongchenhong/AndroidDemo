package com.xch.accessibilityservice;

import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class AutoTestService extends BaseAccessibilityService {

    private static final String TAG = "AutoTestService";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(TAG, "onAccessibilityEvent: " + event.toString() + "\n");
        AccessibilityNodeInfo root = getRootInActiveWindow();

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                || event.getEventType() == AccessibilityEvent.TYPE_WINDOWS_CHANGED
                && event.getPackageName().equals("com.nearme.instant.platform")) {
            clickViewByText(root, "允许");
            clickViewByText(root, "创建");
            clickViewByText(root, "添加");
            clickViewByText(root, "知道了");
        }

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                || event.getEventType() == AccessibilityEvent.TYPE_WINDOWS_CHANGED
                && event.getPackageName().equals("com.android.packageinstaller")) {
            clickViewByText(root, "安装");
            clickViewByText(root, "完成");
        }
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "onServiceConnected: ");
    }
}
