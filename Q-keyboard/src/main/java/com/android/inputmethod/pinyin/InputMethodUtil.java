package com.android.inputmethod.pinyin;

import android.content.Context;
import android.provider.Settings;//导入包
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
// compile 'com.jakewharton.Log:Log:2.7.1'

public class InputMethodUtil {
    private static final String TAG ="InputMethodUtil";
    
        /**
     * 若触宝输入法已安装，则设其为系统默认输入法
     * (写入Android系统数据库)
     */
    public static void setDefaultInputMethod(Context context) {
        //获取系统已安装的输入法ID
        String[] methods = getInputMethodIdList(context);
        if (methods == null || methods.length == 0) {
            Log.w(TAG,String.format("found no input method."));
            return;
        }

        //检查是否安装触宝输入法
        //触宝输入法ID "com.cootek.smartinputv5/com.cootek.smartinput5.TouchPalIME";
        String targetKeyword = "Pinyin";
        String value = "";
        for (String m : methods){
            Log.d(TAG,String.format("find : %s", m));
            if (m.toLowerCase().contains(targetKeyword.toLowerCase())){
                value = m;//找到触宝输入法
            }
        }
        if (value == "") {
            Log.w(TAG,String.format("didn't find " + targetKeyword));
            return;
        }

        //设置默认输入法
        String key = Settings.Secure.DEFAULT_INPUT_METHOD;
        boolean success = Settings.Secure.putString(context.getContentResolver(), key, value);
        Log.d(TAG,String.format("writeDbDefaultInputMethod(%s),result: %s", value,success));

        //读取默认输入法
        String current = Settings.Secure.getString(context.getContentResolver(),key);
        Log.d(TAG,String.format("current default: %s",current));
    }

    public static String getDefaultInputMethodPkgName(Context context) {
        //获取默认输入法
        String key = Settings.Secure.DEFAULT_INPUT_METHOD;
        //读取默认输入法
        String current = Settings.Secure.getString(context.getContentResolver(),key);
        return current.split("/")[0];
    }

    //获取默认输入法包名：
//    public static String getDefaultInputMethodPkgName(Context context) {
//        String mDefaultInputMethodPkg = null;
//
//        String mDefaultInputMethodCls = Settings.Secure.getString(
//                context.getContentResolver(),
//                Settings.Secure.DEFAULT_INPUT_METHOD);
//        //输入法类名信息
//        Log.d(TAG, "mDefaultInputMethodCls=" + mDefaultInputMethodCls);
//        if (!TextUtils.isEmpty(mDefaultInputMethodCls)) {
//            //输入法包名
//            mDefaultInputMethodPkg = mDefaultInputMethodCls.split("/")[0];
//            Log.d(TAG, "mDefaultInputMethodPkg=" + mDefaultInputMethodPkg);
//        }
//        return mDefaultInputMethodPkg;
//    }


    /**
     * 获取系统已安装的输入法ID
     * @param context
     * @return
     */
    public static String[] getInputMethodIdList(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && imm.getInputMethodList() != null) {
            String[] methodIds = new String[imm.getInputMethodList().size()];
            for (int i = 0; i <imm.getInputMethodList().size(); i++) {
                methodIds[i] = imm.getInputMethodList().get(i).getId();
            }
            return methodIds;
        }
        return new String[]{};
    }
}