package com.pqixing.adbkeyboard;

import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.Toast;

public class AdbReceiver extends BroadcastReceiver {
    private final AdbIME adbIME;
    private static String TAG = "AdbReceiver";
    private final static String PKG = "com.pqixing.adbkeyboard";
    public final static String ACTION_GET_VERSION = PKG + ".get_version";
    public final static String ACTION_GET_TEXT = PKG + ".get_text";
    public final static String ACTION_SET_TEXT = PKG + ".set_text";
    public final static String ACTION_GET_TEXT_EDIT = PKG + "get_text_edit";
    public final static String ACTION_SET_TEXT_EDIT = PKG + "set_text_edit";

    public AdbReceiver(AdbIME adbIME) {
        this.adbIME = adbIME;
    }

    public void writeResult(String result) {
        setResult(Activity.RESULT_OK, "onIdeResult=" + (TextUtils.isEmpty(result) ? "" : new String(Base64.encode(result.getBytes(), 0))), null);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if (action == null) action = "";
        String input = getString(intent, TAG);
        switch (action) {
            case ACTION_GET_VERSION:
                writeResult(getVersionName(context));
                break;
            case ACTION_GET_TEXT:
                writeResult(getClipText(context));
                break;
            case ACTION_GET_TEXT_EDIT:
                String output = adbIME.getInputText();
                writeResult(output == null ? "##fail##" : output);
                break;
            case ACTION_SET_TEXT:
                setClipText(context, input);
                writeResult("##success##");
                break;
            case ACTION_SET_TEXT_EDIT:
                writeResult(adbIME.setInputText(input) ? "##success##" : "##fail##");
                break;
            default:
                writeResult("##unkonw##");
                break;
        }
        Toast.makeText(context, "onReceive:" + action + (input == null ? "" : (" -> " + input)), Toast.LENGTH_SHORT).show();
    }

    static void setClipText(Context context, String text) {
        if(text==null) text = "";
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText(null, text));
    }

    static String getClipText(Context context) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        String fromClip = "";
        ClipData clip = clipboard.getPrimaryClip();
        if (clip != null && clip.getItemCount() > 0) {
            fromClip = clip.getItemAt(0).coerceToText(context).toString();
        }
        return fromClip;
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
}
