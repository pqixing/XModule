package com.pqixing.intellij.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtilRt;

import java.io.File;
import java.util.Arrays;

import git4idea.GitVcs;
import git4idea.merge.GitMergeDialog;


public class ClearCache extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        VirtualFile fileByIoFile = VfsUtil.findFileByIoFile(new File(project.getBasePath(),"ProjectInfo.java"), false);
//        ActionManager.getInstance().getAction(action).actionPerformed(e);
//        String action = Messages.showInputDialog("输入Action", "", null);
        GitVcs vcs = GitVcs.getInstance(project);
        AbstractVcsHelper.getInstance(project).showMergeDialog(ContainerUtilRt.newArrayList(fileByIoFile), vcs.getMergeProvider());
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
