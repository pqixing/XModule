package com.dachen.creator.actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public abstract class OpenFileAction extends AnAction {;
    Project project;
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        project = anActionEvent.getProject();
        int i = 0;
        for (VirtualFile child:getFiles(project)) {
            if(child!=null&&child.exists()){
                FileEditorManager.getInstance(project).openFile(child,true);
            }else {
                new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "打开文件失败","文件不存在："+(child==null?getPath(i):child.getPath()), NotificationType.WARNING).notify(project);
            }
            i++;
        }
    }
    protected String getPath(int pos){
        VirtualFile file = getFiles(project)[pos];
        return file==null?"":file.getPath();
    }
    abstract VirtualFile[] getFiles(Project project);
}
