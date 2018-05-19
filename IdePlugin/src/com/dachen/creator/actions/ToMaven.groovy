package com.dachen.creator.actions

import com.dachen.creator.Conts
import com.dachen.creator.GradleCallBack
import com.dachen.creator.ui.MultiCheckBoxDialog
import com.dachen.creator.utils.GradleUtils
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.Pair

public class ToMaven extends AnAction {
    Project project

    @Override
    public void actionPerformed(AnActionEvent e) {
        project = e.getData(PlatformDataKeys.PROJECT)
        Module module = e.getData(LangDataKeys.MODULE)
        String moduleName = module == null ? "" : module.getName()
        String projectName = project.getName()
        String place = e.getPlace()

        boolean projectMode = /*"ProjectViewPopup".equals(place)||*/ "MainMenu".equals(place) || module == null || moduleName == project.getName()

        Pair<Boolean, List<String>> result
        //进入Module多选页面
        if (projectMode) {
            List<String> allModules = new ArrayList<>()
            for (Module m : ModuleManager.getInstance(project).modules) {
                if (projectName != m.name) {
                    allModules.add(m.name)
                }
            }
            int selectItem = module == null || project.name == module.name ? -2 : allModules.indexOf(module.name)
            result = MultiCheckBoxDialog.showMultiSelect(project, "ToMaven", "请选择需要上传到Maven仓库的模块", allModules, selectItem)
        } else {

            result = MultiCheckBoxDialog.showTwoStepSelect(project, "ToMaven", "准备上传: " + module.getName(), getDps(module), -1, "上传前先上传依赖模块", false)
            if (result != null) {
                if (!result.getFirst()) result.getSecond().clear()
                result.getSecond().add(module.getName())
            }
        }
        if (result == null) return
        List<String> uploadList = result.getSecond()
        if (uploadList.isEmpty()) {
            Messages.showMessageDialog("没有选中模块，请先选择模块", "ToMaven", null)
            return
        }
        notification = new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "开始上传", "任务执行期间，请勿修改,更新,构建代码", NotificationType.WARNING)
        notification.notify(project)
        startUp(0, uploadList, new ArrayList<>())
    }

    Notification notification

    /**
     * 上传结束
     *
     * @param results
     */
    private void onEnd(List<String> uploadList, List<Pair<String, String>> results) {
        if (notification != null && notification.isExpired()) {
            notification.expire()
        }
        StringBuilder msg = new StringBuilder("全部上传任务" + uploadList.size() + "条:\n" + uploadList)
        msg.append("\n 实际上传" + results.size() + "条:\n")
        for (Pair<String, String> p : results) {
            msg.append(p.getFirst() + " : " + p.getSecond()).append("\n")
        }
        ApplicationManager.getApplication().invokeLater {
            Messages.showInfoMessage(msg.toString(), "ToMaven执行完成")
        }

    }

    /**
     * 检测上传任务执行
     *
     * @param moduleName
     * @return
     */
    private Pair<Boolean, String> checkUpload(String msg) {
        String[] records = msg.split("##")
        if (records.length < 3) return new Pair<Boolean, String>(false, "未知异常")
        boolean success = "Y" == records[1]
        String realMsg = records[2]

        if (realMsg.contains("::NotUpdate::")) {
            success = true
            realMsg = realMsg.replace("::", " ")
        }
        return new Pair<>(success, realMsg)
    }

    /**
     * 开始上传任务
     *
     * @param pos
     * @param uploadList
     */
    private void startUp(int pos, List<String> uploadList, List<Pair<String, String>> results) {
        if (pos >= uploadList.size()) {
            onEnd(uploadList, results)
            return
        }
        final String moduleName = uploadList.get(pos)
        GradleUtils.runTask(project, [":" + moduleName + ":clean", ":" + moduleName + ":ToMaven"], new GradleCallBack() {
            @Override
            void onFinish(long time, String id, String result) {
                def upload = checkUpload(result)
                upload.first = upload.first && time != -1 && id.contains(moduleName)
                results.add(new Pair<>(moduleName, (upload.getFirst() ? " 成功 : " : " 失败 : ") + upload.getSecond()))

                if (!upload.getFirst()) ApplicationManager.getApplication().invokeLater {
                    int exitCode = Messages.showOkCancelDialog(moduleName
                            + "在ToMaven过程中发生异常，是否继续上传其余模块???\n error:->" + upload.getSecond()
                            + "\n下个模块:" + uploadList.get(pos + 1), "上传失败:" + moduleName, null)
                    if (exitCode == 0) startUp(pos + 1, uploadList, results)
                    else onEnd(uploadList, results)
                } else {
                    startUp(pos + 1, uploadList, results)
                }

            }
        }, GradleUtils.getPro(["$Conts.ENV_FOCUS_INCLUDES": ",${moduleName},", "$Conts.ENV_RUN_ID": moduleName]))
    }

    /**
     * 获取依赖的模块名称
     *
     * @param module
     * @return
     */
    private List<String> getDps(Module module) {

        HashMap<String, Integer> levels = new HashMap<>()
        loadModuleByLevel(0, module, levels)
        HashMap<Integer, List<String>> newMaps = new HashMap<>()

        int lastDept = 0
        for (Map.Entry<String, Integer> entry : levels.entrySet()) {
            int value = entry.getValue()
            lastDept = Math.max(lastDept, value)
            List<String> list = newMaps.get(value)
            if (list == null) {
                list = new ArrayList<>()
                newMaps.put(value, list)
            }
            list.add(entry.getKey())
        }
        List<String> sortModles = new ArrayList<>()
        for (int i = lastDept; i >= 0; i--) {
            List<String> strings = newMaps.get(i)
            if (strings != null) sortModles.addAll(strings)
        }
        return sortModles
    }

    private void loadModuleByLevel(int dept, Module module, HashMap<String, Integer> levels) {
        Module[] modules = ModuleRootManager.getInstance(module).getDependencies()
        if (modules == null || modules.length == 0) return
        for (Module m : modules) {
            if (m == null) continue
            String name = m.getName()
            int oldDept = levels.containsKey(name) ? levels.get(name) : 0
            levels.put(name, Math.max(oldDept, dept))
        }

        for (Module m : modules) {
            loadModuleByLevel(dept + 1, m, levels)
        }
    }


}
