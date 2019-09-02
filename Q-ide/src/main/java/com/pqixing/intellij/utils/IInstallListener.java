package com.pqixing.intellij.utils;

import com.intellij.openapi.progress.ProgressIndicator;
import com.pqixing.intellij.adapter.JListInfo;
import com.pqixing.intellij.ui.NewInstallDialog;
import org.jetbrains.annotations.NotNull;

public interface IInstallListener {
    void beforeStartInstall(int model);

    void afterStartInstall(int model);

    void onInstall(int i, @NotNull JListInfo info);
}
