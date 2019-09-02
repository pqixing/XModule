package com.pqixing.intellij.utils;

import com.intellij.openapi.progress.ProgressIndicator;
import com.pqixing.intellij.adapter.JListInfo;
import com.pqixing.intellij.ui.NewInstallDialog;
import org.jetbrains.annotations.NotNull;

public interface IInstallListener {
    void onInstall(NewInstallDialog dialog,int model,int type,JListInfo info);
}
