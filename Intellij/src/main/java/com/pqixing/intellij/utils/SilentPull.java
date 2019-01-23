package com.pqixing.intellij.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import git4idea.GitUtil;
import git4idea.actions.GitMerge;
import git4idea.actions.GitPull;
import git4idea.commands.Git;
import git4idea.commands.GitImpl;
import git4idea.commands.GitLineHandler;
import git4idea.i18n.GitBundle;
import git4idea.merge.GitPullDialog;

public class SilentPull extends GitPull {

    VirtualFile gitRoot;

    public SilentPull(VirtualFile gitRoot) {
        this.gitRoot = gitRoot;
        Git instance = Git.getInstance();
    }
//
//    @Nullable
//    @Override
//    protected DialogState displayDialog(@NotNull Project project, @NotNull List<VirtualFile> gitRoots, @NotNull VirtualFile defaultRoot) {
//        GitImpl git = new GitImpl();
//        git.fetch()
//        GitLineHandler listener = new GitLineHandler();
//        GitPullDialog dialog = new GitPullDialog(project, Collections.singletonList(gitRoot), gitRoot);
//        DialogState dialogState = new DialogState(gitRoot, GitBundle.message("merging.title", new Object[]{gitRoot.getPath()}), (Computable<GitLineHandler>) () -> listener);
//        return ;
//    }
}
