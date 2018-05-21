package com.dachen.creator.actions

import com.dachen.creator.Conts
import com.dachen.creator.GradleCallBack
import com.dachen.creator.ui.MultiBoxDialog
import com.dachen.creator.utils.GitUtils
import com.dachen.creator.utils.GradleUtils
import com.dachen.creator.utils.StringUtils
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import groovy.json.JsonSlurper

public class CheckOut extends AnAction implements GradleCallBack {
    private String ID_CREATE = "CheckOut"
    private String ID_CHECKOUT = "Create"
    private Project project
    private String branchName

    @Override
    void actionPerformed(AnActionEvent e) {
        project = e.getProject()

        Module module = e.getData(LangDataKeys.MODULE)
//        if(module == null) module = ModuleManager.getInstance(project).getModules()[0]
        Set<String> branchs = GitUtils.findBranchs(module?.moduleFilePath)

        MultiBoxDialog.builder(project)
                .setMode(false, true, true)
                .setMsg("切换分支", "此操作会批量对本地所有分支进行切换")
                .setInput("master")
                .setItems(branchs)
                .setHint("请输入或者勾选需要切换的分支")
                .setListener(new MultiBoxDialog.Listener() {
            @Override
            void onOk(String input, List<String> items, boolean check) {
                branchName = input
                if (branchName.isEmpty()) {
                    new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "输入错误", "分支名不能为空", NotificationType.WARNING).notify(project)
                    return
                }
                def map = ["$Conts.ENV_GIT_BRANCH": branchName, "$Conts.ENV_GIT_TARGET": "all", "$Conts.ENV_RUN_ID": ID_CHECKOUT]

                GradleUtils.runTask(project, ["CheckOut"], CheckOut.this, map)

            }

            @Override
            void onCancel() {

            }
        }).show()
    }

    @Override
    void onFinish(long time, String id, String result) {
        if (time < 0) {
            new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "未知异常", "任务未正常执行完毕", NotificationType.WARNING).notify(project)
            return
        }
        Map<String, String> map = new JsonSlurper().parseText(result)
        def unCheckList = []
        def unCloneList = []
        def checkList = []
        map.each { m ->
            if (Conts.TIP_BRANCH_NOT_EXISTS == m.value) {
                unCheckList.add(m.key)
            } else if (Conts.TIP_GIT_NOT_EXISTS == m.value) {
                unCloneList.add(m.key)
            } else checkList.add(m.key)
        }

        if (ID_CREATE == id) {
            String msg = unCheckList.isEmpty() ? "创建$branchName 分支完成" : "以下工程创建分支失败\n ${StringUtils.listString(unCheckList)}"
            Messages.showInfoMessage(msg + "\n 请注意是否需要重新创建版本标签!!!", "创建$branchName 分支完成")
            return
        } else if (ID_CHECKOUT == id) {
            if (unCheckList.isEmpty()) {
                Messages.showInfoMessage("已切换分支$branchName \n ${unCloneList.isEmpty() ? "" : "以下工程未下载:" + StringUtils.listString(unCloneList)}", "切换分支完成")
                return
            }
            StringBuilder sb = new StringBuilder()
            sb.append("已切换分支: \n").append("-->$checkList")
            if (!unCloneList.isEmpty()) {
                sb.append("\n未下载工程,请下载代码后再进行切换: \n").append("-->${StringUtils.listString(unCloneList)}")
            }
            sb.append("\n缺少${branchName}分支: \n")
                    .append(StringUtils.listString(unCheckList))
                    .append("\n是否创建新分支???")

            def exitCode = Messages.showYesNoCancelDialog(sb.toString(), "创建分支$branchName", null)

            if (exitCode != 0) return

            sb = new StringBuilder(",")
            unCheckList.each { sb.append(it).append(",") }
            sb.append(",")
            def pro = ["$Conts.ENV_GIT_BRANCH": branchName, "$Conts.ENV_GIT_TARGET": "system", "$Conts.ENV_RUN_ID": ID_CREATE, "$Conts.ENV_GIT_NAMES": sb.toString()]

            GradleUtils.runTask(project, ["DeleteBranch"], this, pro)
        }
    }
}
