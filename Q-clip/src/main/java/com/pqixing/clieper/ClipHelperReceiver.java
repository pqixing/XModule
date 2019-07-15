package com.pqixing.clieper;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.Toast;

public class ClipHelperReceiver extends BroadcastReceiver {
    private static String TAG = "ClipboardReceiver";

    public static String EXTRA_GET_VERSION = "get_version";
    public static String EXTRA_GET_TEXT = "get_text";
    public static String EXTRA_SET_TEXT = "set_text";
    public static String EXTRA_GET_TEXT_EDIT = "get_text_edit";
    public static String EXTRA_SET_TEXT_EDIT = "set_text_edit";

    public void writeResult(String result) {
        setResult(Activity.RESULT_OK, "onIdeResult=" + (TextUtils.isEmpty(result) ? "" : new String(Base64.encode(result.getBytes(), 0))), null);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ClipboardManager clipboar = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        String input = null;

        if (EXTRA_GET_VERSION.equals(input = getString(intent, EXTRA_GET_VERSION))) {
            writeResult(getVersionName(context));
        } else if (!TextUtils.isEmpty(input = getString(intent, EXTRA_GET_TEXT))) {
            String fromClip = "";
            ClipData clip = clipboar.getPrimaryClip();
            if (clip != null && clip.getItemCount() > 0) {
                fromClip = clip.getItemAt(0).coerceToText(context).toString();
            }
            writeResult(fromClip);
        } else if (EXTRA_GET_TEXT_EDIT.equals(input = getString(intent, EXTRA_GET_TEXT_EDIT))) {
            if (!checkIfAccessServiceRunning(context)) writeResult("##permission##");
            else {
                String output = AccessService.getFocusText();
                writeResult(output == null ? "##fail##" : output);
            }
        } else if (!TextUtils.isEmpty(input = getString(intent, EXTRA_SET_TEXT))) {
            clipboar.setPrimaryClip(ClipData.newPlainText(null, input));
            writeResult("##success##");
            Toast.makeText(context, "Copy " + input, Toast.LENGTH_SHORT).show();
        } else if (!TextUtils.isEmpty(input = getString(intent, EXTRA_SET_TEXT_EDIT))) {
            if (!checkIfAccessServiceRunning(context)) writeResult("##permission##");
            else writeResult(AccessService.setFocusText(input) ? "##success##" : "##fail##");
        } else writeResult("##unkonw##");
        Toast.makeText(context, "onReceive" + input, Toast.LENGTH_SHORT).show();
    }

    private String getString(Intent intent, String key) {
        String value = intent.getStringExtra(key);
        if (TextUtils.isEmpty(value)) return null;
        return new String(Base64.decode(value, 0));
    }

    private String getVersionName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "0.0";
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
