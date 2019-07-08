package com.dachen.creator.actions;

import com.dachen.creator.core.gennerator.ModelCreatorGenerator;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.pqixing.intellij.actions.QToolGroup;

import java.util.Objects;

public class ModelCreator extends AnAction {

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        e.getPresentation().setVisible(QToolGroup.Companion.isDachenProject(e.getProject()));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        PsiFile file = e.getData(PlatformDataKeys.PSI_FILE);
        for (PsiElement psiElement : Objects.requireNonNull(file).getChildren()) {
            if (psiElement instanceof PsiClass) {
                PsiClass clazz = (PsiClass) psiElement;
                WriteCommandAction.runWriteCommandAction(project, () -> ModelCreatorGenerator.genCode(project, clazz));
            }
        }
    }

}
