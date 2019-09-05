/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.inputmethod.pinyin;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import com.android.adbkeyboard.R;

import java.util.List;

/**
 * Setting activity of Pinyin IME.
 */
public class SettingsActivity extends PreferenceActivity implements
        Preference.OnPreferenceChangeListener {

    private static String TAG = "SettingsActivity";

    private CheckBoxPreference mKeySoundPref;
    private CheckBoxPreference mVibratePref;
    private CheckBoxPreference mPredictionPref;


    //输入法设置
    private static String ACTION_INPUT_METHOD_SETTINGS = "android.settings.INPUT_METHOD_SETTINGS";
    //有效输入法
    private static String
            ACTION_INPUT_METHOD_SUBTYPE_SETTINGS = "android.settings.INPUT_METHOD_SUBTYPE_SETTINGS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        PreferenceScreen prefSet = getPreferenceScreen();

        mKeySoundPref = (CheckBoxPreference) prefSet
                .findPreference(getString(R.string.setting_sound_key));
        mVibratePref = (CheckBoxPreference) prefSet
                .findPreference(getString(R.string.setting_vibrate_key));
        mPredictionPref = (CheckBoxPreference) prefSet
                .findPreference(getString(R.string.setting_prediction_key));

        prefSet.setOnPreferenceChangeListener(this);

        Settings.getInstance(PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext()));

        updatePreference(prefSet, getString(R.string.setting_advanced_key));

        updateWidgets();
        //获取系统输入法列表
//        InputMethodUtil.getInputMethodIdList(this);
        if (isActiveIME()) {   //判断当前输入法是否激活

            if (!InputMethodUtil.getDefaultInputMethodPkgName(this).equals(this.getPackageName())) {
                //弹出选择输入法对话框
                ((InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE)).showInputMethodPicker();
            }
            //跳转系统输入法界面 无效
            //openSystemSettingActIME();

        } else {
            //有效
            openSystemSetting();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateWidgets();

    }

    @Override
    protected void onDestroy() {
        Settings.releaseInstance();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Settings.setKeySound(mKeySoundPref.isChecked());
        Settings.setVibrate(mVibratePref.isChecked());
        Settings.setPrediction(mPredictionPref.isChecked());

        Settings.writeBack();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return true;
    }

    private void updateWidgets() {
        mKeySoundPref.setChecked(Settings.getKeySound());
        mVibratePref.setChecked(Settings.getVibrate());
        mPredictionPref.setChecked(Settings.getPrediction());
    }

    public void updatePreference(PreferenceGroup parentPref, String prefKey) {
        Preference preference = parentPref.findPreference(prefKey);
        if (preference == null) {
            return;
        }
        Intent intent = preference.getIntent();
        if (intent != null) {
            PackageManager pm = getPackageManager();
            List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
            int listSize = list.size();
            if (listSize == 0)
                parentPref.removePreference(preference);
        }
    }

    /**
     * 当前输入法是否激活
     *
     * @return
     */
    public boolean isActiveIME() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        for (InputMethodInfo imi : imm.getEnabledInputMethodList()) {
            if (getPackageName().equals(imi.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    public void openSystemSetting() {
        Intent intent = new Intent(ACTION_INPUT_METHOD_SETTINGS);
        startActivity(intent);
    }

    public void openSystemSettingActIME() {
        Intent intent = new Intent(ACTION_INPUT_METHOD_SUBTYPE_SETTINGS);
        startActivity(intent);
    }

}
