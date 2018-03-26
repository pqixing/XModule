package com.dachen.creator.actions;

import com.dachen.creator.ui.TwoStepMultiCheckBoxDialog;
import com.dachen.creator.utils.FileUtils;
import com.dachen.creator.utils.GradleUtils;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.externalSystem.task.TaskCallback;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ToMaven extends AnAction {
    Project project;

    @Override
    public void actionPerformed(AnActionEvent e) {
        project = e.getData(PlatformDataKeys.PROJECT);
        Module module = (Module) e.getData(DataKey.create("module"));
        String moduleName = module == null ? "" : module.getName();
        String projectName = project.getName();
        String place = e.getPlace();

        boolean projectMode = /*"ProjectViewPopup".equals(place)||*/"MainMenu".equals(place) || module == null || moduleName.equals(project.getName());

        Pair<Boolean, List<String>> result = null;
        //进入Module多选页面
        if (projectMode) {
            List<String> allModules = new ArrayList<>();
            for (Module m : ModuleManager.getInstance(project).getModules()) {
                if (!projectName.equals(m.getName())) {
                    allModules.add(m.getName());
                }
            }
            int selectItem = module == null || project.getName().equals(module.getName()) ? -2 : allModules.indexOf(module.getName());
            result = TwoStepMultiCheckBoxDialog.showMultiSelect(project, "ToMaven", "请选择需要上传到Maven仓库的模块", allModules, selectItem);
        } else {

            result = TwoStepMultiCheckBoxDialog.showTwoStepSelect(project, "ToMaven", "准备上传: " + module.getName(), getDps(module), -1, "上传前先上传依赖模块", false);
            if (result != null) {
                if (!result.getFirst()) result.getSecond().clear();
                result.getSecond().add(module.getName());
            }
        }
        if (result == null) return;
        List<String> uploadList = result.getSecond();
        if (uploadList.isEmpty()) {
            Messages.showMessageDialog("没有选中模块，请先选择模块", "ToMaven", null);
            return;
        }

        GradleUtils.addProperties(project);

        notification = new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "开始上传", "任务执行期间，请勿修改,更新,构建代码", NotificationType.WARNING);
        notification.notify(project);
        startUp(0, uploadList, new ArrayList<>());
    }

    Notification notification;

    /**
     * 上传结束
     *
     * @param results
     */
    private void onEnd(List<String> uploadList, List<Pair<String, String>> results) {
        GradleUtils.clear(project);
        if (notification != null && notification.isExpired()) {
            notification.expire();
        }
        StringBuilder msg = new StringBuilder("全部上传任务" + uploadList.size() + "条:\n" + uploadList);
        msg.append("\n 实际上传" + results.size() + "条:\n");
        for (Pair<String, String> p : results) {
            msg.append(p.getFirst() + " : " + p.getSecond()).append("\n");
        }
        ApplicationManager.getApplication().invokeLater(() -> {
            Messages.showInfoMessage(msg.toString(), "ToMaven执行完成");
        });

    }

    /**
     * 检测上传任务执行
     *
     * @param moduleName
     * @return
     */
    private Pair<Boolean, String> checkUpload(String moduleName) {
        String[] records = FileUtils.readForOne(new File(project.getBasePath(), ".modularization/ide.record")).split("##");
        if (records.length < 3 || System.currentTimeMillis() - Long.parseLong(records[0]) > 1000 * 5 || !moduleName.equals(records[1])) {
            return new Pair<>(false, "未知异常(请查看构建日志)");
        }

        return new Pair<>("Y".equals(records[2]), records[3]);
    }

    /**
     * 开始上传任务
     *
     * @param pos
     * @param uploadList
     */
    private void startUp(int pos, List<String> uploadList, List<Pair<String, String>> results) {
        if (pos >= uploadList.size()) {
            onEnd(uploadList, results);
            return;
        }
        final String moduleName = uploadList.get(pos);
        GradleUtils.addFocusInclude(project,moduleName);
        GradleUtils.runTask(project, Arrays.asList(":" + moduleName + ":clean", ":" + moduleName + ":ToMaven"), new TaskCallback() {
            @Override
            public void onSuccess() {
                Pair<Boolean, String> pair = checkUpload(moduleName);
                results.add(new Pair<>(moduleName, (pair.getFirst() ? " 成功 -> " : " 失败 -> ") + pair.getSecond()));
                if (pair.getFirst() || pos >= uploadList.size() - 1) {
                    startUp(pos + 1, uploadList, results);
                } else ApplicationManager.getApplication().invokeLater(() -> {
                    int exitCode = Messages.showOkCancelDialog(moduleName + "在ToMaven过程中发生异常，是否继续上传其余模块???\n error:->" + pair.getSecond() + "\n下个模块:" + uploadList.get(pos + 1), "上传失败:" + moduleName, null);
                    if (exitCode == 0) startUp(pos + 1, uploadList, results);
                    else onEnd(uploadList, results);
                });
            }

            @Override
            public void onFailure() {
                onSuccess();
            }
        });
    }

    /**
     * 获取依赖的模块名称
     *
     * @param module
     * @return
     */
    private List<String> getDps(Module module) {//@TODO
        List<String> allModule = new ArrayList<>();
        Module[] modules = ModuleRootManager.getInstance(module).getDependencies();
        if (modules == null || modules.length == 0) return allModule;
        for (int i = modules.length - 1; i >= 0; i--) {
            allModule.add(modules[i].getName());
        }
        return allModule;
    }


}
