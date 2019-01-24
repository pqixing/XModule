package com.pqixing.intellij.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import git4idea.actions.GitRepositoryAction;

public class SilentGitActoin extends GitRepositoryAction {

    @NotNull
    @Override
    protected String getActionName() {
        return null;
    }

    @Override
    protected void perform(@NotNull Project project, @NotNull List<VirtualFile> gitRoots, @NotNull VirtualFile defaultRoot) {

    }
}