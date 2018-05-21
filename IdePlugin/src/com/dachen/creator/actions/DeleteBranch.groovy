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

public class DeleteBranch extends AnAction implements GradleCallBack {
    private String ID = "Delete"
    private Project project
    private List<String> delItems = []

    @Override
    public void actionPerformed(AnActionEvent e) {
        project = e.getProject()
        Module module = e.getData(LangDataKeys.MODULE);
        Set<String> branchs = GitUtils.findBranchs(module?.moduleFilePath)
        MultiBoxDialog.builder(project)
                .setMode(true, true, false)
                .setMsg("删除分支", "此操作会批量删除分支,请慎重操作!!!!!!")
                .setInput("")
                .setItems(branchs)
                .setHint("请输入或者勾选需要切换的分支")
                .setListener(new MultiBoxDialog.Listener() {
            @Override
            void onOk(String input, List<String> items, boolean check) {
                Set<String> temp = new HashSet<>()

                temp.addAll(items)
                if (!input.isEmpty()) temp.add(input)
                if (temp.isEmpty()) {
                    new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "输入错误", "分支名不能为空", NotificationType.WARNING).notify(project)
                    return
                }
                if (temp.contains("master")) {
                    new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "输入错误", "master分支不能删除!!!!", NotificationType.WARNING).notify(project)
                    return
                }
                delItems.addAll(temp)
                int exitCode = Messages.showOkCancelDialog("请再次确认,是否删除分支:$delItems", "确认删除", null)
                if (exitCode != 0) return

                startDel(delItems.remove(0))
            }

            @Override
            void onCancel() {

            }
        }).show()
    }

    void startDel(String name) {
        def map = ["$Conts.ENV_GIT_BRANCH": name, "$Conts.ENV_GIT_TARGET": "all", "$Conts.ENV_RUN_ID": ID]
        GradleUtils.runTask(project, ["DeleteBranch"], this, map)
    }

    @Override
    void onFinish(long time, String id, String result) {
        if (delItems.isEmpty()) {
            new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "删除结束", GradleUtils.checkResult(time, ID, id) ? "删除分支完成" : "操作异常,请检查", NotificationType.INFORMATION).notify(project)
        } else {
            startDel(delItems.remove(0))
        }
    }
}
