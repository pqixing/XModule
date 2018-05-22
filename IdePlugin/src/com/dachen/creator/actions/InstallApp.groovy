package com.dachen.creator.actions

import com.dachen.creator.utils.AndroidUtils
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

public class InstallApp extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        Module module = e.getData(LangDataKeys.MODULE);


        VirtualFile dir = module == null ? project.baseDir : module.moduleFile.parent;
        VirtualFile target = dir;
        dir = dir.findChild("build");
        if (dir != null) {
            target = dir;
            dir = dir.findChild("outputs");
        }
        if (dir != null) {
            target = dir;
            dir = dir.findChild("apk");
        }
        if (dir != null) target = dir;
        FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false);
        VirtualFile[] chooseFiles = FileChooser.chooseFiles(descriptor, project, target);
        if (chooseFiles.length > 0) {
            VirtualFile apk = chooseFiles[0]
            if (!apk.name.endsWith(".apk")) {
                new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "文件错误", "不是apk文件类型", NotificationType.WARNING).notify(project)
            } else {
                AndroidUtils.installApk(project, new File(apk.path))
            }
        }
    }
}
