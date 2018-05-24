package com.dachen.creator.actions

import com.dachen.creator.Conts
import com.dachen.creator.GradleCallBack
import com.dachen.creator.ui.MultiBoxDialog
import com.dachen.creator.utils.AndroidUtils
import com.dachen.creator.utils.GradleUtils
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.Pair;

public class RunModule extends AnAction {

    Project project
    Module module

    @Override
    public void actionPerformed(AnActionEvent e) {
        project = e.getProject()

        module = e.getData(LangDataKeys.MODULE)
        if (module == null||module.name == project.name) selectModule()
        else build()
    }

    void selectModule() {
        MultiBoxDialog.builder(project)
                .setMode(false, true, true)
                .setMsg("选择模块", "请选择需要构建的模块")
                .setInput("")
                .setItems(getModules())
                .setHint("请输入或者勾选需要构建的模块")
                .setListener(new MultiBoxDialog.Listener() {
            @Override
            void onOk(String input, List<String> items, boolean check) {

                module = ModuleManager.getInstance(project).findModuleByName(input)
                if (module == null) {
                    new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "输入错误", "没有对应的模块", NotificationType.WARNING).notify(project)
                    return
                }

            }

            @Override
            void onCancel() {

            }
        }).show()
    }

    private void build() {
        Pair<String, Boolean> result = Messages.showInputDialogWithCheckBox("请输入模块构建信息", "构建模块$module.name", "MavenOnly", true, true, null, module.name, null)
        if (result.first == null || result.second == null) return
        String moduleName = module.name
        GradleUtils.runTask(project, [":" + moduleName + ":BuildFirstDebug"], new GradleCallBack() {
            @Override
            void onFinish(long time, String id, String resultStr) {
                String filePath = resultStr.replace("buildApk=","")
                File apk = new File(filePath)
                if(apk.exists()) AndroidUtils.installApk(project,apk)
                else new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "安装失败", "安装包不存在", NotificationType.WARNING).notify(project)
            }
        }, ["$Conts.ENV_FOCUS_INCLUDES": module.name, "$Conts.ENV_RUN_ID": module.name, "$Conts.ENV_BUILD_APP_TYPE": "test", "$Conts.ENV_DEPENDENT_MODEL": result.second ? "MavenOnly" : "localFirst"])

    }

    private List<String> getModules() {
        List<String> allModules = new ArrayList<>()
        for (Module m : ModuleManager.getInstance(project).modules) {
            if (project.name != m.name) {
                allModules.add(m.name)
            }
        }

        return allModules
    }
}
