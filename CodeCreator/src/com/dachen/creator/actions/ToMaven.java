package com.dachen.creator.actions;

import com.dachen.creator.utils.GradleUtils;
import com.intellij.codeInsight.navigation.BackgroundUpdaterTask;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.externalSystem.task.TaskCallback;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.PairFunction;

import javax.swing.*;
import java.util.Arrays;

public class ToMaven extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
//        GradleUtils.runTask(project, Arrays.asList("clean","build"), new TaskCallback() {
//            @Override
//            public void onSuccess() {
//                System.out.println("onSuccess-------------");
//                        Messages.showInfoMessage("Haa","sdfsd");
//
//            }
//
//            @Override
//            public void onFailure() {
//                System.out.println("onFailure-------------");
//            }
//        });
        JTextField jTextField = new JTextField();
//        Messages.showMultilineInputDialog(project, "提示信息", "多行", "哈哈哈", null, new InputValidator() {
//            @Override
//            public boolean checkInput(String s) {
//                System.out.println(s);
//                return true;
//            }
//
//            @Override
//            public boolean canClose(String s) {
//                System.out.println(s);
//                return false;
//            }
//        });

        Messages.showCheckboxMessageDialog("多选", " ti ", new String[]{"1", "2", "3"}, "请选择", true, 2, 2, null, new PairFunction<Integer, JCheckBox, Integer>() {
            @Override
            public Integer fun(Integer integer, JCheckBox jCheckBox) {
                System.out.println(integer+"-------"+jCheckBox.getText());
                return 2;
            }
        });


    }
}
