package com.pqixing.intellij.utils;

import com.intellij.openapi.progress.ProgressIndicator;
import com.pqixing.intellij.adapter.JListInfo;
import com.pqixing.intellij.ui.NewInstallDialog;

public interface IInstall {
    void onInstall(JListInfo info, NewInstallDialog dialog, ProgressIndicator indicator, TaskCallBack afterInstall);
}
