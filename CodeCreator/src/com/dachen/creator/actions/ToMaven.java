package com.dachen.creator.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.PairFunction;

import javax.swing.*;

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
//        JTextField jTextField = new JTextField();
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
        Messages.showCheckboxMessageDialog("多选", " ti ", new String[]{"1", "2", "3"}, "请选择", true, 2, 2, null, (integer, jCheckBox) -> {
            System.out.println(integer+"-------"+jCheckBox.getText());
            return -1;
        });


    }
}
