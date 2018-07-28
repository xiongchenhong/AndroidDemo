package com.xch.accessibilityservice;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public abstract class BaseAccessibilityService extends AccessibilityService {

    private AccessibilityManager mAccessibilityManager;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        mAccessibilityManager = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
    }

    /**
     * 检查某个反馈类型为generic的服务是否开启
     */
    public boolean checkAccessibilityEnabled(String serviceName) {
        List<AccessibilityServiceInfo> enabledAccessibilityServiceList
                = mAccessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo info : enabledAccessibilityServiceList) {
            if (info.getId().equals(serviceName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 打开辅助功能设置页面
     */
    public void goEnableService() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
            startActivity(intent);
        }
    }

    public void performViewClick(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo != null && nodeInfo.isClickable()) {
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }

    public void performBackClick() {
        performGlobalAction(GLOBAL_ACTION_BACK);
    }

    /**
     * 找到一个符合text的view
     */
    public AccessibilityNodeInfo findViewByText(String text) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) {
            return null;
        }
        List<AccessibilityNodeInfo> nodeInfos = root.findAccessibilityNodeInfosByText(text);
        if (nodeInfos != null && !nodeInfos.isEmpty()) {
            for (AccessibilityNodeInfo info : nodeInfos) {
                if (info != null) {
                    return info;
                }
            }
        }
        return null;
    }

    /**
     * 点击所有符合text的view
     */
    public void clickViewByText(String text) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        clickViewByText(root, text);
    }

    public void clickViewByText(AccessibilityNodeInfo root, String text) {
        if (root == null) {
            return;
        }
        List<AccessibilityNodeInfo> nodeInfos = root.findAccessibilityNodeInfosByText(text);
        if (nodeInfos != null && !nodeInfos.isEmpty()) {
            for (AccessibilityNodeInfo info : nodeInfos) {
                performViewClick(info);
            }
        }
    }

    /**
     * 根据id找到第一个匹配的view
     */
    public AccessibilityNodeInfo findViewById(String id) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) {
            return null;
        }
        List<AccessibilityNodeInfo> nodeInfos = root.findAccessibilityNodeInfosByViewId(id);
        if (nodeInfos != null && !nodeInfos.isEmpty()) {
            for (AccessibilityNodeInfo info : nodeInfos) {
                if (info != null) {
                    return info;
                }
            }
        }
        return null;
    }

    /**
     * 点击所有符合id的View
     */
    public void clickViewById(String id) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) {
            return;
        }
        List<AccessibilityNodeInfo> nodeInfos = root.findAccessibilityNodeInfosByViewId(id);
        if (nodeInfos != null && !nodeInfos.isEmpty()) {
            for (AccessibilityNodeInfo info : nodeInfos) {
                performViewClick(info);
            }
        }
    }
}
