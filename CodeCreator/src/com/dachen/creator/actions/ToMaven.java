package com.dachen.creator.actions;

import com.dachen.creator.ui.TwoStepMultiCheckBoxDialog;
import com.dachen.creator.utils.GradleUtils;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.icons.AllIcons;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.externalSystem.task.TaskCallback;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ToMaven extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        Module module = e.getData(DataKey.create("module"));
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        String place = e.getPlace();

        TwoStepMultiCheckBoxDialog boxDialog = null;
         List<String> allModules = new ArrayList<>();
        boolean projectMode = "MainMenu".equals(place)||module == null;
//        new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID,"哈哈哈","嘻嘻嘻",NotificationType.INFORMATION).notify(project);

        //进入Module多选页面
        if(projectMode){
            for (Module m : ModuleManager.getInstance(project).getModules()){
                allModules.add(m.getName());
            }
            boxDialog =TwoStepMultiCheckBoxDialog.showMultiSelect(project, "ToMave", "请选择需要上传到Maven仓库的模块",allModules,-2);
        }else {
            ArrayList<String> temp = new ArrayList<>();
            ModuleRootManager instance = ModuleRootManager.getInstance(module);
            Module[] dependencies = instance.getDependencies();
        }

//        if(boxDialog == null || boxDialog.getExitCode() !=0){
//            HintManager.getInstance().showInformationHint(editor,"ToMaven任务取消");
//            return;
//        }
//
//
//
//        List<String> s = new ArrayList<>();
//        for (int i = 0; i < 60; i++) {
//            s.add("Module -> "+i);
//        }
//
//        boxDialog = TwoStepMultiCheckBoxDialog.showTwoStepSelect(project, "测试标题", "信息", s,-2,"同时打包依赖",true);
//
//        int exitCode = boxDialog.getExitCode();
//        System.out.println("exitCode : "+exitCode);
//        TwoStepMultiCheckBoxDialog.showMultiSelect(project, "测试标题", "信息", s,-2);
//        new MultiChecBox2().show();
//        for (int i = 0; i < modules.length*10; i++) {
//            for(String s :ModuleRootManager.getInstance(modules[i]).getDependencyModuleNames()){
//                System.out.println("s = "+s);
//            }
//        }
        GradleUtils.runTask(project, Arrays.asList("clean","build"), new TaskCallback() {
            @Override
            public void onSuccess() {
                System.out.println("onSuccess-------------");
                new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID,"哈哈哈","嘻嘻嘻",NotificationType.INFORMATION).notify(project);

            }

            @Override
            public void onFailure() {
                System.out.println("onFailure-------------");
            }
        });

//        Messages.showCheckboxMessageDialog()
//        Messages.showMultilineInputDialog(project,"111\nddd\n33","ddd","111\nddd\n33",null,null);
//        Messages.showCheckboxOkCancelDialog("提示","hh","text",true,1,1,null);
//        Messages.showChooseDialog("哈哈", "ddd", strs,"ddd", null);
//        Messages.showCheckboxMessageDialog("多选", " ti ", strs, "请选择", true, 2, 2, null, (integer, jCheckBox) -> {
//            Messages.showCheckboxMessageDialog("多选", " ti ", strs, "请选择", true, 2, 2, null, null);
//            return -1;
//        });

    }




}
