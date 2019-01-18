package com.pqixing.intellij.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;
import java.util.Arrays;

import git4idea.GitVcs;
import git4idea.merge.GitMergeDialog;


public class ClearCache extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
//        ActionManager.getInstance().getAction(action).actionPerformed(e);
//        String action = Messages.showInputDialog("输入Action", "", null);

        GitVcs instance = GitVcs.getInstance(project);
        VirtualFile fileByIoFile = VfsUtil.findFileByIoFile(new File(project.getBasePath()), false);
        System.out.println(instance.toString());
        GitMergeDialog dialog = new GitMergeDialog(project, Arrays.asList(fileByIoFile), fileByIoFile);
//
        dialog.showAndGet();
//        String jsonStr = JSON.toJSONString("jsonStr");
//        String title = TextUtils.INSTANCE.firstUp("this is title");
//        int exitCode = Messages.showOkCancelDialog(jsonStr+","+ Help.INSTANCE.methodTest(), "title", null);
//        if(exitCode!=0) return;
    }
}
