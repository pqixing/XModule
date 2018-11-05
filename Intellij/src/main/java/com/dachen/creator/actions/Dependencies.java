package com.dachen.creator.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class Dependencies extends OpenFileAction {

    @Override
    VirtualFile[] getFiles(Project project) {
        if(module == null) {
            VirtualFile child = project.getBaseDir().findChild(".modularization");
            if(child!=null) child = child.findChild("setting.gradle");
            return new VirtualFile[]{child};
        }

        VirtualFile child = module.getModuleFile().getParent().findChild(".modularization");
        if(child!=null) child = child.findChild(".cache");
        if(child!=null) child = child.findChild("dependencies.gradle");
        return new VirtualFile[]{child};
    }

    @Override
    protected String getPath(int pos) {
        return module==null?"请选择模块！！":"dependencies.gradle";
    }
}
