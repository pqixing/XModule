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
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.Pair

public class VersionTag extends AnAction implements GradleCallBack {
    Project project;
    private String branchName
    private String ID= "VersionTag"
    @Override
    public void actionPerformed(AnActionEvent e) {
        project = e.getProject()
        Module module = e.getData(LangDataKeys.MODULE);
        Set<String> branchs = GitUtils.findBranchs(module?.moduleFilePath)
        MultiBoxDialog.builder(project)
                .setMode(false, true, true)
                .setMsg("创建版本Tag", "此操作会未指定分支创建版本号tag")
                .setInput("")
                .setItems(branchs)
                .setHint("请输入或者勾选需要创建版本Tag的分支")
                .setListener(new MultiBoxDialog.Listener() {
                    @Override
                    void onOk(String input, List<String> items, boolean check) {
                        branchName = input
                        if (branchName.isEmpty()) {
                            new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "输入错误", "分支名不能为空", NotificationType.WARNING).notify(project)
                            return
                        }
                        Pair<String, Boolean> result = Messages.showInputDialogWithCheckBox("确认为$branchName 创建版本tag(冻结maven版本号)", "创建版本Tag", "只冻结master的分支版本号", true, true,null,branchName, null)
                        if(result.first == null||result.getSecond() == null) return
                        branchName = result.getFirst()
                        boolean onlyMaster = result.getSecond()
                        if(branchName==null||branchName.isEmpty()){
                            new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "输入错误", "分支名不能为空", NotificationType.WARNING).notify(project)
                            return
                        }

                        def map = ["$Conts.ENV_GIT_BRANCH": branchName, "$Conts.ENV_GIT_TARGET": onlyMaster?"master":"all", "$Conts.ENV_RUN_ID": ID]
                        GradleUtils.runTask(project, ["VersionTag"], VersionTag.this, map)
                    }

                    @Override
                    void onCancel() {

                    }
                }).show()
    }

    @Override
    void onFinish(long time, String id, String result) {
        new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "tag创建完成", result, NotificationType.INFORMATION).notify(project)
    }
}
