package com.pqixing.intellij.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.function.Consumer;

import git4idea.GitRemoteBranch;
import git4idea.commands.Git;
import git4idea.commands.GitCommand;
import git4idea.commands.GitCommandResult;
import git4idea.commands.GitLineHandler;
import git4idea.commands.GitLineHandlerListener;
import git4idea.config.GitVersionSpecialty;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryImpl;

public class Git4IdeHelper {
    public static final Git getGit() {
        return Git.getInstance();
    }

    public static final GitRepository getRepo(File dir, Project project) {
        VirtualFile gitDir = VfsUtil.findFileByIoFile(dir, true);
        if (gitDir == null) return null;

        return GitRepositoryImpl.getInstance(gitDir, project, false);
    }

    public static GitRepository clone(@NotNull final Project project, @NotNull final File directory, @NotNull final String url, String cloneBranch) {
        GitCommandResult result = getGit().runCommand(() -> {
            GitLineHandler handler = new GitLineHandler(project, directory.getParentFile(), GitCommand.CLONE);
            handler.setSilent(false);
            handler.setStderrSuppressed(false);
            handler.setUrl(url);
            handler.addParameters("--progress");
            if (GitVersionSpecialty.CLONE_RECURSE_SUBMODULES.existsIn(project) && Registry.is("git.clone.recurse.submodules")) {
                handler.addParameters("--recurse-submodules");
            }
            handler.addParameters(url);
            handler.endOptions();
            handler.addParameters(directory.getName());
            return handler;
        });
        GitRepository repo = Git4IdeHelper.getRepo(directory, project);
        if (repo == null) return null;
        if (cloneBranch == null || cloneBranch.equals(repo.getCurrentBranchName())) return repo;
        for (GitRemoteBranch remoteBranch : repo.getBranches().getRemoteBranches()) {
            if (remoteBranch.getName().endsWith("/" + cloneBranch)) {
                Git4IdeHelper.getGit().checkout(repo, remoteBranch.getName(), cloneBranch, true, true);
                break;
            }
        }
        return repo;
    }
}
