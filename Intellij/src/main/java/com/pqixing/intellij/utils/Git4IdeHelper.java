package com.pqixing.intellij.utils;

import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.pqixing.tools.FileUtils;
import git4idea.*;
import git4idea.branch.GitBranchUtil;
import git4idea.branch.GitBrancher;
import git4idea.branch.GitBranchesCollection;
import git4idea.changes.GitChangeUtils;
import git4idea.commands.*;
import git4idea.repo.GitRemote;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryImpl;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

import static com.intellij.dvcs.DvcsUtil.findVirtualFilesWithRefresh;
import static com.intellij.util.ObjectUtils.assertNotNull;

public class Git4IdeHelper {
    public static final String LOCAL = "local";
    public static final String REMOTE = "remote";
    public static final String LOSE = "lose";

    public static final Git getGit() {
        return Git.getInstance();
    }

    public static final GitRepository getRepo(File dir, Project project) {
        VirtualFile gitDir = VfsUtil.findFileByIoFile(dir, true);
        if (gitDir == null) return null;

        return GitRepositoryImpl.getInstance(gitDir, project, false);
    }

    /**
     * Clone 指定分支代码
     *
     * @param project
     * @param directory
     * @param url
     * @param cloneBranch
     * @return
     */
    public static GitRepository clone(@NotNull final Project project, @NotNull final File directory, @NotNull final String url, String cloneBranch, GitLineHandlerListener... listeners) {
        File dir = directory.getParentFile();
        if (!dir.exists()) dir.mkdirs();
        FileUtils.delete(directory);
        boolean doClone = getGit().clone(project, dir, url, directory.getName(), listeners).getExitCode() == 0;
        if (!doClone) return null;
        GitRepository repo = getRepo(directory, project);
        if (repo == null) return null;
        if (cloneBranch == null || cloneBranch.equals(repo.getCurrentBranchName())) return repo;
        for (GitRemoteBranch remoteBranch : repo.getBranches().getRemoteBranches()) {
            if (remoteBranch.getName().endsWith("/" + cloneBranch)) {
                Git4IdeHelper.getGit().checkout(repo, remoteBranch.getName(), cloneBranch, true, true, listeners);
                break;
            }
        }
        return repo;
    }

    /**
     * 批量push
     *
     * @param repo
     * @return
     */
    public static String push(Project project, GitRepository repo, GitLineHandlerListener... listeners) {
        String update = update(project, repo, listeners);
        if (!"Up-To-Date".equals(update)) return update;
        GitLocalBranch branch = repo.getCurrentBranch();
        if (branch == null) return "Not Branch";
        GitCommandResult push = getGit().push(repo, GitRemote.ORIGIN, repo.getPresentableUrl(), branch.getName(), branch.findTrackedBranch(repo) == null, listeners);
        if (push.success()) return "Success";
        return push.getErrorOutputAsJoinedString();
    }

    public static String update(Project project, GitRepository repo, GitLineHandlerListener... listeners) {
        Git git = getGit();
        GitRemote remote = GitUtil.findRemoteByName(repo, GitRemote.ORIGIN);
        git.fetch(repo, remote, Arrays.asList(listeners));
        git.remotePrune(repo, remote);
        return merge(project, "FETCH_HEAD", repo, listeners);
    }

    public static Map<String, List<GitRepository>> sortGitRepoByBranch(final String targetBranch, List<GitRepository> repos) {
        final String remoteBranchName = "origin/" + targetBranch;
        ArrayList<GitRepository> localBranchs = new ArrayList<>();
        ArrayList<GitRepository> remoteBranchs = new ArrayList<>();
        ArrayList<GitRepository> loseBranchs = new ArrayList<>();
        for (GitRepository repo : repos) {
            if (targetBranch.equals(repo.getCurrentBranchName())) continue;

            GitBranchesCollection branches = repo.getBranches();
            if (branches.findBranchByName(targetBranch) != null) localBranchs.add(repo);
            else if (branches.findBranchByName(remoteBranchName) != null) localBranchs.add(repo);
            else {
                getGit().fetch(repo, GitUtil.findRemoteByName(repo, GitRemote.ORIGIN), Collections.emptyList());
                if (branches.findBranchByName(targetBranch) != null) localBranchs.add(repo);
                else if (branches.findBranchByName(remoteBranchName) != null)
                    localBranchs.add(repo);
                else loseBranchs.add(repo);
            }
        }
        HashMap<String, List<GitRepository>> map = new HashMap<>();
        map.put(LOCAL, localBranchs);
        map.put(REMOTE, remoteBranchs);
        map.put(LOSE, loseBranchs);
        return map;
    }

    /**
     * 切换分支
     *
     * @param myProject
     * @param targetBranch
     * @param repos
     * @return
     */
    public static List<GitRepository> checkout(@NotNull final Project myProject, final String targetBranch, List<GitRepository> repos, Runnable allInAwtLater) {
        Map<String, List<GitRepository>> map = sortGitRepoByBranch(targetBranch, repos);
        //开始切换分支
        GitBrancher brancher = GitBrancher.getInstance(myProject);
        brancher.checkout(targetBranch, false, map.get(LOCAL), () -> {
            List<GitRepository> remotes = map.get(REMOTE);
            if (remotes.isEmpty()) allInAwtLater.run();
            else
                brancher.checkoutNewBranchStartingFrom(targetBranch, "origin/" + targetBranch, remotes, allInAwtLater);
        });
        return map.get(LOSE);
    }

    /**
     * 合并
     */
    public static String merge(Project project, final String mergeBranch, GitRepository repo, GitLineHandlerListener... listeners) {
        //开始切换分支
        try {
            String msg = System.currentTimeMillis() + "";
            GitCommandResult r = getGit().stashSave(repo, msg);
            boolean saveStash = r.success() && !r.getOutputAsJoinedString().contains("No local changes to save");

            GitSimpleEventDetector conflict = new GitSimpleEventDetector(GitSimpleEventDetector.Event.MERGE_CONFLICT);
            GitSimpleEventDetector updateToDate = new GitSimpleEventDetector(GitSimpleEventDetector.Event.ALREADY_UP_TO_DATE);
            List<GitLineHandlerListener> asList = new ArrayList<>(Arrays.asList(listeners));
            asList.add(conflict);
            asList.add(updateToDate);
            GitCommandResult merge = getGit().merge(repo, mergeBranch, Collections.emptyList(), asList.toArray(listeners));

            boolean hadConflict = false;
            int unMergeSize = 0;
            if (conflict.hasHappened()) {
                hadConflict = true;
                List<VirtualFile> unMergeFiles = findVirtualFilesWithRefresh(GitChangeUtils.getUnmergedFiles(repo));

                do {
                    ApplicationManager.getApplication().invokeAndWait(() -> {
                        List<VirtualFile> files = AbstractVcsHelper.getInstance(project).showMergeDialog(unMergeFiles, GitVcs.getInstance(project).getMergeProvider());
                        unMergeFiles.removeAll(files);//删除所有合并后的文件
                    });
                } while (saveStash && !unMergeFiles.isEmpty() && !abortMerge(repo, "Stash apply request resolve conflict files,or abort merge!!"));
                unMergeSize = unMergeFiles.size();

                //合并完成，提交内容，防止再次
                if (unMergeSize == 0) commitMerge(project, mergeBranch, repo, listeners);
            }
            if (saveStash) {
                getGit().stashPop(repo);
                List<VirtualFile> unMergeFiles = findVirtualFilesWithRefresh(GitChangeUtils.getUnmergedFiles(repo));
                if (!unMergeFiles.isEmpty()) ApplicationManager.getApplication().invokeAndWait(() -> {
                    List<VirtualFile> files = AbstractVcsHelper.getInstance(project).showMergeDialog(unMergeFiles, GitVcs.getInstance(project).getMergeProvider());
                    unMergeFiles.removeAll(files);//删除所有合并后的文件
                });
                unMergeSize = unMergeFiles.size();
            }
            //有冲突未解决
            if (unMergeSize > 0) return "Merge Conflict";
            //没有冲突，并且更新到了最新
            if (!hadConflict && updateToDate.hasHappened()) return "Up-To-Date";
            //有冲突，已解决，或者，直接合并成功返回合并成功
            if (hadConflict || merge.success()) return "Merge Success";
            //其他情况，返回错误情况
            return merge.getErrorOutputAsJoinedString();//返回第一条错误数据
        } catch (Exception e) {
            return e.toString();
        }
    }

    private static void commitMerge(Project project, String mergeBranch, GitRepository repo, GitLineHandlerListener[] listeners) throws VcsException {
        GitLineHandler handler = new GitLineHandler(project, repo.getRoot(), GitCommand.COMMIT);
        handler.setStdoutSuppressed(false);
        File messageFile = repo.getRepositoryFiles().getMergeMessageFile();
        if (!messageFile.exists()) {
            handler.addParameters("-m", "Merge branch '" + mergeBranch + "' of " + repo.getPresentableUrl() + " with conflicts.");
        } else {
            handler.addParameters("-F", messageFile.getAbsolutePath());
        }
        handler.endOptions();
        for (GitLineHandlerListener l : listeners) {
            handler.addLineListener(l);
        }
        getGit().runCommand(handler).throwOnError();
    }

    /**
     * 提示是否需要丢弃合并
     *
     * @param repo
     * @return
     */
    private static boolean abortMerge(GitRepository repo, String msg, GitLineHandlerListener... listeners) {
        final int[] exitCodes = new int[]{0};
        ApplicationManager.getApplication().invokeAndWait(() -> {
            exitCodes[0] = Messages.showYesNoDialog(msg, "Abort Merge", null);
        });
        if (exitCodes[0] != 0) return false;
        for (GitLineHandlerListener l : listeners) {
            l.onLineAvailable("abortMerge ----->", ProcessOutputTypes.STDOUT);
        }
        return getGit().resetMerge(repo, null).success();
    }

//    /**
//     * 合并冲突代码
//     *
//     * @param myProject
//     * @param repos
//     * @return
//     */
//    public static boolean resolveConflict(@NotNull final Project myProject, GitRepository... repos) {
//        GitConflictResolver.Params params = new GitConflictResolver.Params(myProject).
//                setMergeDescription("The following files have unresolved conflicts. You need to resolve them before ").
//                setErrorNotificationTitle("Unresolved files remain.");
//        return new GitConflictResolver(myProject, getGit(), GitUtil.getRootsFromRepositories(Arrays.asList(repos)), params).merge();
//        if (!unMergeFiles.isEmpty()) {
//            AbstractVcsHelper.getInstance(project).showMergeDialog(ContainerUtilRt.newArrayList(fileByIoFile), vcs.getMergeProvider());
//        }
//    }

}
