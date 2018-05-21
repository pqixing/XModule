package com.dachen.creator.actions

import com.dachen.creator.Conts
import com.dachen.creator.GradleCallBack
import com.dachen.creator.ui.MultiBoxDialog
import com.dachen.creator.utils.GitUtils
import com.dachen.creator.utils.GradleUtils
import com.dachen.creator.utils.StringUtils
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import groovy.json.JsonSlurper;

public class FastMerge extends AnAction implements GradleCallBack {
    Project project;
    private String branchName
    private String ID = "FastMerge"

    @Override
    public void actionPerformed(AnActionEvent e) {
        project = e.getProject()
        Module module = e.getData(LangDataKeys.MODULE);
        Set<String> branchs = GitUtils.findBranchs(module?.moduleFilePath)
        MultiBoxDialog.builder(project)
                .setMode(false, true, true)
                .setMsg("快速合并分支", "此操作会批量合并指定分支(只能快速合并没有冲突的分支!!!!!)")
                .setInput("")
                .setItems(branchs)
                .setHint("请输入或者勾选需要合并的分支")
                .setListener(new MultiBoxDialog.Listener() {
            @Override
            void onOk(String input, List<String> items, boolean check) {
                branchName = input
                if (branchName.isEmpty()) {
                    new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "输入错误", "分支名不能为空", NotificationType.WARNING).notify(project)
                    return
                }
                int exitCode = Messages.showOkCancelDialog("请再次确认,是否合并:$branchName\n 重要提示:请确认本地所有代码已切换到同一分支!!!!", "温馨提示", null)
                if (exitCode != 0) return

                def map = ["$Conts.ENV_GIT_BRANCH": branchName, "$Conts.ENV_GIT_TARGET": "all", "$Conts.ENV_RUN_ID": ID]

                GradleUtils.runTask(project, ["FastMerge"], FastMerge.this, map)
            }

            @Override
            void onCancel() {

            }
        }).show()


    }

    @Override
    void onFinish(long time, String id, String result) {
        if (!GradleUtils.checkResult(time, id, ID)) {
            new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "未知异常,请解决问题后重试", "合并结束", NotificationType.WARNING).notify(project)
            return
        }

        Map<String, String> map = new JsonSlurper().parseText(result)
        def noBranchList = []
        def unCloneList = []
        def mergeFailList = []
        def otherList = []
        map.each { m ->
            if (Conts.TIP_BRANCH_NOT_EXISTS == m.value) {
                noBranchList.add(m.key)
            } else if (Conts.TIP_GIT_NOT_EXISTS == m.value) {
                unCloneList.add(m.key)
            } else if (Conts.TIP_GIT_MERGE_FAIL == m.value) {
                mergeFailList.add(m.key)
            } else otherList.add(m.key)
        }
        Messages.showMessageDialog("已合并:\n${StringUtils.listString(noBranchList + otherList)}\n以下工程需手动合并:\n 未下载:\n ${StringUtils.listString(unCloneList)} \n冲突:\n ${StringUtils.listString(mergeFailList)}", "合并完成$branchName", null)
    }
}
