package com.dachen.creator.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class ModuleConfig extends OpenFileAction {

    @Override
    VirtualFile[] getFiles(Project project) {
        return new VirtualFile[]{project.getBaseDir().getParent().findChild("Document").findChild("default.xml")};
    }
}
