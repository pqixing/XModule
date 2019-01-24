package com.pqixing.intellij.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;

import git4idea.commands.Git;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryImpl;

public class Git4IdeHelper {
    public static final Git getGit() {
        return Git.getInstance();
    }

    public static final GitRepository getRepo(File dir, Project project) {
        VirtualFile gitDir = VfsUtil.findFileByIoFile(dir, false);
        if (gitDir == null) return null;

        return GitRepositoryImpl.getInstance(gitDir, project, false);
    }
}
