package com.pqixing.intellij.actions;

import com.intellij.dvcs.repo.VcsRepositoryManager;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;


public class RunAction extends AnAction {
    private String action;

    public RunAction() {

    }

    public RunAction(String action) {
        this.action = action;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
//        Project project = e.getProject();
//        VirtualFile fileByIoFile = VfsUtil.findFileByIoFile(new File(project.getBasePath(),"templet.java"), false);
        if (action == null) action = Messages.showInputDialog("Input Action Id", "RunAction", null);
        ActionManager.getInstance().getAction(action).actionPerformed(e);
        VcsRepositoryManager instance = VcsRepositoryManager.getInstance(e.getProject());
//        new Task.Backgroundable(e.getProject(), DvcsBundle.message("cloning.repository", "http://192.168.3.200/android/Document.git")) {
//
//            @Override
//            public void run(@NotNull ProgressIndicator indicator) {
//                File dir = new File(e.getProject().getBasePath(), "../test2");
//                dir.mkdirs();
//                GitCommandResult clone = Git4IdeHelper.getGit().clone(e.getProject(), dir, "http://192.168.3.200/android/Document.git", "Document");
//                Git4IdeHelper.getGit().checkout(Git4IdeHelper.getRepo(new File(dir, "Document"), e.getProject()), "origin/TestMaster", "TestMaster", true, false);
//            }
//        }.queue();

//        AbstractVcsHelper.getInstance(project).showMergeDialog(ContainerUtilRt.newArrayList(fileByIoFile), vcs.getMergeProvider());
//        GitVcs instance = GitVcs.getInstance(project);
//        System.out.println(instance.toString());
//        GitMergeDialog dialog = new GitMergeDialog(project, Arrays.asList(fileByIoFile), fileByIoFile);
////
//        dialog.showAndGet();
//        String jsonStr = JSON.toJSONString("jsonStr");
//        String title = TextUtils.INSTANCE.firstUp("this is title");
//        int exitCode = Messages.showOkCancelDialog(jsonStr+","+ Help.INSTANCE.methodTest(), "title", null);
//        if(exitCode!=0) return;
    }
}
