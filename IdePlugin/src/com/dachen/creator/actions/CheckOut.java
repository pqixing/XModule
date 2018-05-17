package com.dachen.creator.actions;

import com.dachen.creator.utils.GradleUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.externalSystem.task.TaskCallback;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;

import java.util.Arrays;
import java.util.List;

public class CheckOut extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        Pair<String, Boolean> pair = Messages.showInputDialogWithCheckBox("请输入待切换的分支名称,(请确认本地没有修改文件，否则可能导致切换失败！！！)", "切换分支", "切换本地所有工程", true, true, null, "master", null);
        if(pair.getFirst() == null||pair.getSecond() ==null) return;

        List<Pair<String, Object>> properties = GradleUtils.getDefaultProperties();
        properties.add(new Pair<>("branchName",pair.getFirst()));
        if(pair.getSecond()){
            properties.add(new Pair<>("target","all"));
            GradleUtils.addFocusInclude(project,"empty");
        }else {
            properties.add(new Pair<>("target","include"));
        }
        GradleUtils.addProperties(project,properties);
        GradleUtils.runTask(project, Arrays.asList("CheckBranch"), new TaskCallback() {
            @Override
            public void onSuccess() {
                GradleUtils.clear(project);
            }

            @Override
            public void onFailure() {
                onSuccess();
            }
        });
    }
}
