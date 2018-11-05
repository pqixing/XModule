package com.dachen.creator.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class Include extends OpenFileAction {
    @Override
    VirtualFile[] getFiles(Project project) {
        return new VirtualFile[]{project.getBaseDir().findChild("setting.kt")};
    }

    @Override
    protected String getPath(int pos) {
        return "setting.kt";
    }
}
