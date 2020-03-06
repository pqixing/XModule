package com.dachen.creator.actions;

import com.dachen.creator.core.gennerator.RouterCreatorGenerator;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.pqixing.intellij.actions.QToolGroup;

import java.util.Objects;

public class PathCreator extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        PsiFile file = e.getData(PlatformDataKeys.PSI_FILE);
        for (PsiElement psiElement : Objects.requireNonNull(file).getChildren()) {
            if(psiElement instanceof PsiClass) {
                PsiClass clazz = (PsiClass) psiElement;
                if (classFilter(clazz)) {
                    return;
                }
                WriteCommandAction.runWriteCommandAction(project, () -> RouterCreatorGenerator.genCode(project, clazz));
            }
        }
    }

    private boolean classFilter(PsiClass clazz) {
        if(clazz.hasModifierProperty("public")){
            Messages.showWarningDialog("Class can not be public", "Warning");
            return true;
        }

        if(!clazz.hasModifierProperty("final")){
            Messages.showWarningDialog("Class must be final", "Warning");
            return true;
        }
        return false;
    }

    @Override
    public void update(AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor != null) {
            e.getPresentation().setEnabled(true);
        } else {
            e.getPresentation().setEnabled(false);
        }
        e.getPresentation().setVisible(QToolGroup.Companion.isModulariztionProject(e.getProject()));
    }
}
