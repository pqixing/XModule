package com.pqixing.clieper;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.Toast;

public class ClipHelperReceiver extends BroadcastReceiver {
    private static String TAG = "ClipboardReceiver";

    public static String EXTRA_TEXT = "text";
    public static String EXTRA_GET_TEXT = "get_text";
    public static String EXTRA_SET_TEXT = "set_text";
    public static String EXTRA_GET_TEXT_EDIT = "get_text_edit";
    public static String EXTRA_SET_TEXT_EDIT = "set_text_edit";

    @Override
    public void onReceive(Context context, Intent intent) {
        String extra = intent.getStringExtra(EXTRA_TEXT);
        ClipboardManager clipboar = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (!TextUtils.isEmpty(extra)) {
            setResult(Activity.RESULT_OK, "result=success", null);
        } else if (!TextUtils.isEmpty(intent.getStringExtra(EXTRA_GET_TEXT))) {
            extra = clipboar.getText() + "";
            setResult(Activity.RESULT_OK, "result=success" + new String(Base64.encode(extra.getBytes(), 0)), null);
        } else if (!TextUtils.isEmpty(extra = intent.getStringExtra(EXTRA_SET_TEXT))) {
            extra = new String(Base64.decode(extra, 0));
            clipboar.setPrimaryClip(ClipData.newPlainText(null, extra));
            Toast.makeText(context, "copy to clip boar:\n" + extra, Toast.LENGTH_SHORT).show();
            setResult(Activity.RESULT_OK, "result=success", null);
            Toast.makeText(context, "Copy " + extra, Toast.LENGTH_SHORT).show();

        } else if (!TextUtils.isEmpty(intent.getStringExtra(EXTRA_GET_TEXT_EDIT))) {
            if (!checkIfAccessServiceRunning(context)) {
                setResult(Activity.RESULT_OK, "result=notpermission", null);
            } else {
                extra = AccessService.getFocusText() + "";
                setResult(Activity.RESULT_OK, "result=success" + (extra.isEmpty()?"":new String(Base64.encode(extra.getBytes(), 0))), null);
            }
        } else if (!TextUtils.isEmpty(extra = intent.getStringExtra(EXTRA_SET_TEXT_EDIT))) {
            if (!checkIfAccessServiceRunning(context)) {
                setResult(Activity.RESULT_OK, "result=notpermission", null);
            } else {
                boolean setFocusText = AccessService.setFocusText(new String(Base64.decode(extra, 0)));
                setResult(Activity.RESULT_OK, "result="+(setFocusText?"success":"fail"), null);
            }
        }else  setResult(Activity.RESULT_OK, "result=fail", null);

//        Toast.makeText(context, "onRecevice " + extra, Toast.LENGTH_SHORT).show();
    }

    private boolean checkIfAccessServiceRunning(Context context) {
        boolean isRuning = AccessService.service != null;
        if (!isRuning) try {
            Toast.makeText(context, "Clip Helper accessibility service is not running , please enable first", Toast.LENGTH_SHORT).show();
            context.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (Exception e) {
            context.startActivity(new Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            e.printStackTrace();
        }
        return isRuning;
    }
}
