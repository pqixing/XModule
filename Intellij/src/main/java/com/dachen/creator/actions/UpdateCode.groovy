package com.dachen.creator.actions

import com.dachen.creator.Conts
import com.dachen.creator.GradleCallBack
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
import com.intellij.openapi.util.Pair
import groovy.json.JsonSlurper

public class UpdateCode extends AnAction {
    private String ID = "UpdateCode"
    private String ID_CLONE = "CloneCode"
    Project project

    @Override
    public void actionPerformed(AnActionEvent e) {
        project = e.getProject()
        Module module = e.getData(LangDataKeys.MODULE)
        def map = ["$Conts.ENV_GIT_TARGET": "all", "$Conts.ENV_RUN_ID": ID]

        GradleUtils.runTask(project, ["UpdateCode"], new GradleCallBack() {
            @Override
            void onFinish(long time, String id, String result) {
                if (!GradleUtils.checkResult(time, id, ID)) {
                    Messages.showInfoMessage("更新代码异常,请解决问题后重试", "错误提示")
                    return
                }
                Map<String, String> resultMap = new JsonSlurper().parseText(result)
                def unCheckList = []
                def unCloneList = []
                def checkList = []
                resultMap.each { m ->
                    if (Conts.TIP_BRANCH_NOT_EXISTS == m.value) {
                        unCheckList.add(m.key)
                    } else if (Conts.TIP_GIT_NOT_EXISTS == m.value) {
                        unCloneList.add(m.key)
                    } else checkList.add(m.key)
                }
                if (unCloneList.isEmpty()) {
                    Messages.showInfoMessage("所有代码已更新", "代码更新完成")
                    return
                }
                def exitCode = Messages.showYesNoCancelDialog("以下工程未下载到本地,是否下载:\n${StringUtils.listString(unCloneList)}", "代码更新完成", null)

                if (exitCode != 0) return

                map = ["$Conts.ENV_GIT_TARGET": "system", "$Conts.ENV_RUN_ID": ID_CLONE, "$Conts.ENV_GIT_NAMES": StringUtils.ls(unCloneList)]

                def dir = module == null ? null : GitUtils.findGitDir(new File(module.moduleFilePath))
                if (dir != null) {
                    map.put(Conts.ENV_GIT_BRANCH, GitUtils.run("git rev-parse --abbrev-ref HEAD", dir)?.last())
                }
                cloneCode(map)
            }
        }, map)
    }
    /**
     * 下载代码
     * @param scriptParameters
     */
    void cloneCode(Map<String, Object> scriptParameters) {

        GradleUtils.runTask(project, ["CloneAll"], new GradleCallBack() {
            @Override
            void onFinish(long time, String id, String result) {
                if (!GradleUtils.checkResult(time, id, ID)) {
                    new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "更新代码异常,请解决问题后重试", "错误提示", NotificationType.WARNING).notify(project)
                    return
                }
                Map<String, String> resultMap = new JsonSlurper().parseText(result)
                def unCloneList = resultMap.findAll { Conts.TIP_GIT_NOT_EXISTS == it.value }

                if (unCloneList.isEmpty()) {
                    Messages.showInfoMessage("所有代码已更新", "完成")
                } else {
                    Messages.showMessageDialog("所有代码已更新,以下工程下载失败,请检查:\n${StringUtils.listString(unCloneList)}", "完成", null)
                }

            }
        }, scriptParameters)
    }
}
