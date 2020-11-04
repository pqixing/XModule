package com.pqixing.intellij.git.uitils;

import com.intellij.dvcs.DvcsUtil;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import com.pqixing.tools.FileUtils;
import git4idea.GitLocalBranch;
import git4idea.GitUtil;
import git4idea.GitVcs;
import git4idea.branch.GitBrancher;
import git4idea.commands.*;
import git4idea.repo.GitRemote;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import git4idea.util.StringScanner;
import kotlin.Triple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public class GitHelper {
    public static final String LOCAL = "local";
    public static final String REMOTE = "remote";
    public static final String LOSE = "lose";

    public static final Git getGit() {
        return Git.getInstance();
    }

    public static final GitRepository getRepo(File dir, Project project) {
        if (!GitUtil.isGitRoot(dir)) return null;
        VirtualFile gitDir = VfsUtil.findFileByIoFile(dir, true);
        if (gitDir == null) return null;

        return GitRepositoryManager.getInstance(project).getRepositoryForFileQuick(gitDir);
    }

    /**
     * Clone 指定分支代码
     *
     * @param project
     * @param directory
     * @param url
     * @return
     */
    public static boolean clone(@NotNull final Project project, @NotNull final File directory, @NotNull final String url, GitLineHandlerListener... listeners) {
        File dir = directory.getParentFile();
        if (!dir.exists()) dir.mkdirs();
        FileUtils.delete(directory);
        return getGit().clone(project, dir, url, directory.getName(), listeners).getExitCode() == 0;
    }

    /**
     * 批量push
     *
     * @param repo
     * @return
     */
    public static String push(Project project, GitRepository repo, GitLineHandlerListener... listeners) {
        callListener("Prepare push for " + repo.getRoot().getName(), listeners);
        String update = update(project, repo, listeners);
        if (!"Already up to date".equals(update)) return update;
        GitLocalBranch branch = repo.getCurrentBranch();
        if (branch == null) return "None";
        GitCommandResult push = getGit().push(repo, GitRemote.ORIGIN, repo.getPresentableUrl(), branch.getName(), branch.findTrackedBranch(repo) == null, listeners);
        if (push.success()) return "Success";
        return push.getErrorOutputAsJoinedString();
    }

    public static String update(Project project, GitRepository repo, GitLineHandlerListener... listeners) {
        callListener("Prepare update for " + repo.getRoot().getName(), listeners);
        Git git = getGit();
        GitRemote remote = findRemoteByName(repo, GitRemote.ORIGIN);
        git.fetch(repo, remote, Arrays.asList(listeners));
        git.remotePrune(repo, remote);
        return merge(project, "FETCH_HEAD", repo, listeners);
    }

    public static Triple sortGitRepoByBranch(final String targetBranch, List<GitRepository> repos) {
        ArrayList<GitRepository> localBranchs = new ArrayList<>();
        ArrayList<GitRepository> remoteBranchs = new ArrayList<>();
        ArrayList<GitRepository> loseBranchs = new ArrayList<>();
        for (GitRepository repo : repos) {
            String localBranch = repo.getCurrentBranchName();
            if (targetBranch.equals(localBranch)) continue;
            if (checkBranchExists(repo, findLocalBranchName(targetBranch))) localBranchs.add(repo);
            else if (checkBranchExists(repo, findRemoteBranchName(targetBranch)))
                remoteBranchs.add(repo);
            else loseBranchs.add(repo);
        }
        return new Triple(localBranchs, remoteBranchs, loseBranchs);
    }

    /**
     * 切换分支
     *
     * @param myProject
     * @param targetBranch
     * @param repos
     * @return
     */
    public static void checkout(@NotNull final Project myProject, final String targetBranch, List<GitRepository> repos, Runnable allInAwtLater) {
        Triple triple = sortGitRepoByBranch(targetBranch, repos);
        //开始切换分支
        GitBrancher brancher = GitBrancher.getInstance(myProject);
        Runnable runRemote = () -> {
            List<GitRepository> remotes = (List<GitRepository>) triple.getSecond();
            if (remotes.isEmpty()) allInAwtLater.run();
            else
                brancher.checkoutNewBranchStartingFrom(targetBranch, "origin/" + targetBranch, remotes, allInAwtLater);
        };
        List<GitRepository> locals = (List<GitRepository>) triple.getFirst();
        if (locals.isEmpty()) runRemote.run();
        else brancher.checkout(targetBranch, false, locals, () -> {
            runRemote.run();
        });
    }

    /**
     * 合并
     */
    public static String merge(Project project, final String mergeBranch, GitRepository repo, GitLineHandlerListener... listeners) {
//        if (!checkBranchExists(repo, mergeBranch)) return "Branch Not Exists";
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
                List<VirtualFile> unMergeFiles = DvcsUtil.findVirtualFilesWithRefresh(getUnmergedFiles(repo));

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
                List<VirtualFile> unMergeFiles = DvcsUtil.findVirtualFilesWithRefresh(getUnmergedFiles(repo));
                if (!unMergeFiles.isEmpty())
                    ApplicationManager.getApplication().invokeAndWait(() -> {
                        List<VirtualFile> files = AbstractVcsHelper.getInstance(project).showMergeDialog(unMergeFiles, GitVcs.getInstance(project).getMergeProvider());
                        unMergeFiles.removeAll(files);//删除所有合并后的文件
                    });
                unMergeSize = unMergeFiles.size();
            }
            //有冲突未解决
            if (unMergeSize > 0) return "Conflict";
            //没有冲突，并且更新到了最新
            if (!hadConflict && updateToDate.hasHappened()) return "UpToDate";
            //有冲突，已解决，或者，直接合并成功返回合并成功
            if (hadConflict || merge.success()) return "Success";
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
        addListener(handler, listeners);
        getGit().runCommand(handler);
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
        callListener("abortMerge ----->", listeners);
        return getGit().resetMerge(repo, null).success();
    }

    @NotNull
    public static String delete(@Nullable Project project, @NotNull String targetBranch, GitRepository repo, GitLineHandlerListener... listeners) {
        callListener("Prepare delete " + targetBranch + " for " + repo.getRoot().getName(), listeners);
        //删除本地存在的分支
        String localBranchName = findLocalBranchName(targetBranch);
        if (checkBranchExists(repo, localBranchName))
            getGit().branchDelete(repo, findLocalBranchName(targetBranch), true, listeners);
        String remoteBranchName = findRemoteBranchName(targetBranch);
        if (checkBranchExists(repo, remoteBranchName)) {
            GitEventDetector fail = new GitEventDetector("remote ref does not exist", "failed to push some refs to");
            GitCommandResult result = getGit().runCommand(() -> {
                final GitLineHandler h = new GitLineHandler(repo.getProject(), repo.getRoot(), GitCommand.PUSH);
                h.setUrls(Collections.singleton(repo.getPresentableUrl()));
                h.setSilent(false);
                h.setStdoutSuppressed(false);
                addListener(h, listeners);
                h.addLineListener(fail);
                //push删除指令
                h.addParameters("origin", "--porcelain", "--delete", findLocalBranchName(targetBranch));
                return h;
            });
            fail.detector(result.getErrorOutput());
            if (fail.hasHappened()) return "None";
            return result.success() ? "Success" : result.getErrorOutputAsJoinedString();
        }
        return "None";
    }

    public static boolean checkBranchExists(GitRepository repo, String name) {
        boolean b = repo.getBranches().findBranchByName(name) != null;
        if (!b && name.contains("origin")) {
            getGit().fetch(repo, findRemoteByName(repo, GitRemote.ORIGIN), Collections.emptyList());
            b = repo.getBranches().findBranchByName(name) != null;
        }
        return b;
    }

    public static GitRemote findRemoteByName(GitRepository repo, String name) {
        return ContainerUtil.find(repo.getRemotes(), remote -> remote.getName().equals(name));
    }


    public static String findLocalBranchName(String name) {
        return name.contains("/") ? name.substring(name.lastIndexOf("/") + 1) : name;
    }

    public static String findRemoteBranchName(String name) {
        return "origin/" + findLocalBranchName(name);
    }

    @NotNull
    public static String create(@NotNull Project project, @NotNull String targetBranch, @Nullable GitRepository repo, GitLineHandlerListener... listeners) {
        callListener("Prepare create " + targetBranch + " for " + repo.getRoot().getName(), listeners);
        String remoteBranchName = findRemoteBranchName(targetBranch);
        if (checkBranchExists(repo, remoteBranchName)) return "Exists";
        String localBranchName = findLocalBranchName(targetBranch);
        boolean createByMe = false;
        if (!checkBranchExists(repo, localBranchName)) {
            callListener("Create " + localBranchName + " from " + repo.getCurrentBranchName(), listeners);
            //创建本地分支
            getGit().branchCreate(repo, findLocalBranchName(targetBranch), repo.getCurrentBranchName());
            createByMe = true;
        }

        GitCommandResult result = getGit().runCommand(() -> {
            final GitLineHandler h = new GitLineHandler(repo.getProject(), repo.getRoot(), GitCommand.PUSH);
            h.setUrls(Collections.singleton(repo.getPresentableUrl()));
            h.setSilent(false);
            h.setStdoutSuppressed(false);
            addListener(h, listeners);
            //push删除指令
            h.addParameters("origin", "--porcelain", localBranchName + ":" + localBranchName);
            h.addParameters("--set-upstream");
            h.addParameters("--force");
            h.addParameters("--no-verify");
            return h;
        });
        //创建成功，删除本地分支
        if (createByMe)
            getGit().branchDelete(repo, findLocalBranchName(targetBranch), true, listeners);
        return result.success() ? "Success" : result.getErrorOutputAsJoinedString();

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

    public static final void callListener(String s, GitLineHandlerListener... listeners) {
        for (GitLineHandlerListener l : listeners) l.onLineAvailable(s, ProcessOutputTypes.STDOUT);
    }

    public static final void addListener(GitLineHandler handler, GitLineHandlerListener... listeners) {
        for (GitLineHandlerListener l : listeners) handler.addLineListener(l);
    }

    @NotNull
    public static String addAndCommit(@NotNull Project project, @Nullable GitRepository repo, @NotNull String commitMsg, GitLineHandlerListener... listeners) {
        callListener("Prepare commit " + commitMsg + " for " + repo.getRoot().getName(), listeners);
        GitLineHandler handler = new GitLineHandler(project, repo.getRoot(), GitCommand.ADD);
        handler.addParameters("--ignore-errors", "-A");
        addListener(handler, listeners);
        GitCommandResult result = Git.getInstance().runCommand(handler);
        if (!result.success()) return result.getErrorOutputAsJoinedString();

        GitEventDetector detector = new GitEventDetector("nothing to commit", "working tree clean");
        handler = new GitLineHandler(project, repo.getRoot(), GitCommand.COMMIT);
        handler.setStdoutSuppressed(false);
        handler.addParameters("-m", commitMsg.trim().isEmpty() ? "Auto Commit" : commitMsg);
        handler.endOptions();
        addListener(handler, listeners);
        handler.addLineListener(detector);
        result = getGit().runCommand(handler);

        return detector.hasHappened() || result.success() ? "Success" : result.getErrorOutputAsJoinedString();
    }

    public static List<String> state(Project project, GitRepository repo, GitLineHandlerListener... listeners) {
        GitLineHandler handler = new GitLineHandler(project, repo.getRoot(), GitCommand.STATUS);
        handler.addParameters("--porcelain");
        addListener(handler, listeners);
        handler.endOptions();
        handler.setSilent(true);
        GitCommandResult result = getGit().runCommand(handler);
        return result.success() ? result.getOutput() : null;
    }

    @NotNull
    public static List<File> getUnmergedFiles(@NotNull GitRepository repository) throws VcsException {
        GitCommandResult result = getGit().getUnmergedFiles(repository);
        if (!result.success()) {
            throw new VcsException(result.getErrorOutputAsJoinedString());
        }

        String output = StringUtil.join(result.getOutput(), "\n");
        HashSet<String> unmergedPaths = ContainerUtil.newHashSet();
        for (StringScanner s = new StringScanner(output); s.hasMoreData(); ) {
            if (s.isEol()) {
                s.nextLine();
                continue;
            }
            s.boundedToken('\t');
            String relative = s.line();
            unmergedPaths.add(GitUtil.unescapePath(relative));
        }

        VirtualFile root = repository.getRoot();
        return ContainerUtil.map(unmergedPaths, path -> new File(root.getPath(), path));
    }
}
