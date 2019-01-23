package com.pqixing.intellij.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import git4idea.actions.GitMerge;

public class SilentMerge extends GitMerge {

    @Nullable
    @Override
    protected DialogState displayDialog(@NotNull Project project, @NotNull List<VirtualFile> gitRoots, @NotNull VirtualFile defaultRoot) {
        return super.displayDialog(project, gitRoots, defaultRoot);
    }
}
