package com.dachen.creator.actions;

import com.dachen.creator.core.gennerator.MVPCreatorGenerator;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.pqixing.intellij.actions.QToolGroup;

public class MvpCreator extends AnAction   {

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        e.getPresentation().setVisible(QToolGroup.Companion.isDachenProject(e.getProject()));
    }

    private String classPrefix;
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        Module module = e.getData(LangDataKeys.MODULE);
        if(module == null){
            Messages.showWarningDialog("请选中要生成mvp代码的组件!", "CreateMvpTemplateCode");
            return;
        }
        String path = module.getModuleFilePath();
        String realPath = path.substring(0, path.lastIndexOf("/"));
        InputValidator validator = new InputValidator() {
            @Override
            public boolean checkInput(String s) {
                return s != null && s.trim().length() > 0;
            }

            @Override
            public boolean canClose(String s) {
                classPrefix = s;
                return s != null && s.trim().length() > 0;
            }
        };
        com.intellij.openapi.util.Pair<String, Boolean> p = Messages.showInputDialogWithCheckBox(
                "please input class prefix",
                module.getName(),
                "create Activity ?",
                true,
                true,
                Messages.getQuestionIcon(), "", validator);

        if(classPrefix ==null || classPrefix.trim().length()<=0 || "null".equals(classPrefix)){
            return;
        }
        WriteCommandAction.runWriteCommandAction(project, new Runnable() {
            @Override
            public void run() {
                MVPCreatorGenerator.genCode(project, classPrefix, realPath, p.second);
                classPrefix = null;
            }
        });

    }
}
