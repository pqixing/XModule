package com.dachen.creator.actions

import com.dachen.creator.Conts
import com.dachen.creator.GradleCallBack
import com.dachen.creator.ui.MultiBoxDialog
import com.dachen.creator.utils.GitUtils
import com.dachen.creator.utils.GradleUtils
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile

public class DependentList extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        Module module = e.getData(LangDataKeys.MODULE)
        if (module == null) {
            Messages.showMessageDialog("没有选中模块", "操作失败", null);
            return;
        }

        Set<String> branchs = GitUtils.findBranchs(module?.moduleFilePath)

        MultiBoxDialog.builder(project)
                .setMode(false, true, true)
                .setMsg("选择分支", "选择需要分析依赖的分支")
                .setInput(GitUtils.findBranchName(module?.moduleFilePath))
                .setItems(branchs)
                .setHint("请输入或者勾选需要切换的分支")
                .setListener(new MultiBoxDialog.Listener() {
            @Override
            void onOk(String input, List<String> items, boolean check) {
                if (input.isEmpty()) {
                    new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "输入错误", "分支名不能为空", NotificationType.WARNING).notify(project)
                    return
                }
                def map = ["$Conts.ENV_GIT_BASE_BRANCH": input, "$Conts.ENV_GIT_TARGET": "system", "$Conts.ENV_FOCUS_INCLUDES": module.name, "$Conts.ENV_RUN_ID": "DependentList"]

                GradleUtils.runTask(project, [":" + module.name + ":AllInnerDps"], new GradleCallBack() {
                    @Override
                    void onFinish(long time, String id, String result) {
                        VirtualFile dir = module.getModuleFile().getParent();
                        VirtualFile target = dir;
                        dir = dir.findChild(".modularization");
                        if (dir != null) {
                            target = dir;
                            dir = dir.findChild("dependencies");
                        }
                        if (dir != null) {
                            target = dir;
                            dir = dir.findChild("version.dp");
                        }
                        if (dir != null)  FileEditorManager.getInstance(project).openFile(dir, false);
                    }
                },map)

            }

            @Override
            void onCancel() {

            }
        }).show()

    }
}
