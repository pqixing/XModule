package com.pqixing.clieper;

import android.accessibilityservice.AccessibilityService;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

public class AccessService extends AccessibilityService {
    public static AccessService service;

    public AccessService() {
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        service = this;
        Toast.makeText(this,"onServiceConnected:",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
       onInterrupt();
    }

    public static String getFocusText() {
        if (service == null) return null;
        AccessibilityNodeInfo window = service.getRootInActiveWindow();
        if(window==null) return null;
        AccessibilityNodeInfo focus = window.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
        if (focus == null) return null;
        return focus.getText()+"";
    }

    public static boolean setFocusText(String text) {
        if (service == null) return false;
        AccessibilityNodeInfo window = service.getRootInActiveWindow();
        if(window==null) return false;
        AccessibilityNodeInfo focus = window.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
        if (focus == null) return false;
        Bundle bundle = new Bundle();
        bundle.putCharSequence(AccessibilityNodeInfo .ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,text);
        focus.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,bundle);
        return true;
    }

    @Override
    public void onInterrupt() {
        service = null;
        Toast.makeText(this,"onServiceDisConnected:",Toast.LENGTH_SHORT).show();
    }

}
