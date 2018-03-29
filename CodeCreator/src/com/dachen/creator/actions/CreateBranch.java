package com.dachen.creator.actions;

import com.dachen.creator.utils.GradleUtils;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.jarRepository.services.artifactory.Endpoint;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode;
import com.intellij.openapi.externalSystem.task.TaskCallback;
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil;
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.refactoring.inline.InlineMethodProcessor;

import java.util.Arrays;
import java.util.List;

public class CreateBranch extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        String branchName = Messages.showInputDialog(project, "请输入待创建的的分支名称", "新建分支", null, "master", null);
        if(branchName == null) return;
        int i = Messages.showOkCancelDialog("请确认本地是最新的（没有未push的代码），此操作会Clone所有代码批量创建，请内心等待", "创建" + branchName + "分支", null);
        if(i!=0) return;

        List<Pair<String, Object>> properties = GradleUtils.getDefaultProperties();
        properties.add(new Pair<>("branchName",branchName));
        properties.add(new Pair<>("target","all"));
        GradleUtils.addFocusInclude(project,"empty");

        GradleUtils.addProperties(project,properties);
        GradleUtils.runTask(project, Arrays.asList("CreateBranch"), new TaskCallback() {
            @Override
            public void onSuccess() {
                GradleUtils.clear(project);
            }

            @Override
            public void onFailure() {
                onSuccess();
            }
        },ProgressExecutionMode.START_IN_FOREGROUND_ASYNC);

    }
}
