package com.dachen.creator.actions;

import com.dachen.creator.ui.TwoStepMultiCheckBoxDialog;
import com.dachen.creator.utils.GradleUtils;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.icons.AllIcons;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode;
import com.intellij.openapi.externalSystem.task.TaskCallback;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ThrowableRunnable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ToMaven extends AnAction {
    Project project;

    @Override
    public void actionPerformed(AnActionEvent e) {
        project = e.getData(PlatformDataKeys.PROJECT);
        Module module = e.getData(DataKey.create("module"));
        String place = e.getPlace();

        boolean projectMode = "ProjectViewPopup".equals(place)||"MainMenu".equals(place)||module == null;

        Pair<Boolean, List<String>> result = null;
        //进入Module多选页面
        if(projectMode){
            List<String> allModules = new ArrayList<>();
            for (Module m : ModuleManager.getInstance(project).getModules()){
                allModules.add(m.getName());
            }
            int selectItem = module==null?-2:allModules.indexOf(module.getName());
            result = TwoStepMultiCheckBoxDialog.showMultiSelect(project, "ToMaven", "请选择需要上传到Maven仓库的模块", allModules, selectItem);
        }else {

            result = TwoStepMultiCheckBoxDialog.showTwoStepSelect(project, "ToMaven", "准备上传: "+module.getName(), getDps(module), -1,"上传前先上传依赖模块",false);
            if(result!=null) result.getSecond().add(module.getName());
        }
        if(result == null) return;
        List<String> uploadList = result.getSecond();
        if(uploadList.isEmpty()){
            Messages.showMessageDialog("没有选中模块，请先选择模块","ToMaven",null);
            return;
        }

        GradleUtils.addProperties(project,true,new Pair<>("dependMode","mavenOnly"));//@TODO
        startUp(0,uploadList,new ArrayList<>());
    }

    /**
     * 上传结束
     * @param results
     */
    private void onEnd(List<String> uploadList,List<Pair<String,String>> results){
        GradleUtils.addProperties(project,true);

        StringBuilder msg = new StringBuilder("全部上传任务"+uploadList.size()+"条:\n"+uploadList);
        msg.append("\n 实际上传"+results.size()+"条:\n");
        for (Pair<String,String> p :results) {
            msg.append(p.getFirst()+" : "+p.getSecond()).append("\n");
        }
        ApplicationManager.getApplication().invokeLater(() -> {
            Messages.showInfoMessage(msg.toString(), "ToMaven执行完成");
        });

    }

    /**
     * 检测上传任务执行
     * @param moduleName
     * @return
     */
    private @Nonnull Pair<Boolean,String> checkUpload(String moduleName){
        return new Pair<>(true,"上传成功,版本号是:2.0");//@TODO
    }
    /**
     * 开始上传任务
     * @param pos
     * @param uploadList
     */
    private void startUp(int pos,List<String> uploadList,List<Pair<String,String>> results){
        if(pos>= uploadList.size()){
            onEnd(uploadList,results);
            return;
        }
        final String moduleName = uploadList.get(pos);
        GradleUtils.runTask(project, Arrays.asList(":"+moduleName+":clean",":"+moduleName+":ToMaven"), new TaskCallback() {
            @Override
            public void onSuccess() {
                Pair<Boolean, String> pair = checkUpload(moduleName);
                results.add(new Pair<>(moduleName,pair.getSecond()));
                if(pair.getFirst()) {
                    startUp(pos + 1, uploadList, results);
                }else ApplicationManager.getApplication().invokeLater(() -> {
                    int exitCode = Messages.showOkCancelDialog("在ToMaven过程中发生异常，是否继续上传其余模块???\n error:->" + pair.getSecond(), "上传失败:" + moduleName, null);
                    if(exitCode == 0) startUp(pos+1,uploadList,results);
                    else onEnd(uploadList,results);
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
     * @param module
     * @return
     */
    private List<String> getDps(Module module){//@TODO
        List<String> allModule = new ArrayList<>();
        Module[] modules = ModuleRootManager.getInstance(module).getDependencies();
        if(modules == null|| modules.length == 0)return allModule;
        for (int i = modules.length-1; i >=0 ; i--) {
            allModule.add(modules[i].getName());
        }
        return allModule;
    }


}
