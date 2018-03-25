package com.dachen.creator.actions;

import com.dachen.creator.utils.FileUtils;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.JDOMException;
import org.jetbrains.annotations.SystemIndependent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NewProject extends AnAction {

    private List<String> copyFiles = Arrays.asList("build.gradle","include.kt","modularization.properties");
    FileChooserDescriptor dirs = null;
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        Pair<String, Boolean> pair = Messages.showInputDialogWithCheckBox("请输入目录名称，例如master等", "创建新Project工程", "使用当前工程路径作为参考坐标？", true, true, null, "master", null);
        if(pair == null||pair.getFirst()==null) return;
        String rootName = pair.getFirst();
        String rootPath = "";
        Boolean useCurDir = pair.getSecond();
        if("".equals(rootName)) {
            new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "创建Project失败", "目录名称不能为空", NotificationType.WARNING).notify(project);
            return;
        }
        if(useCurDir) rootPath = project.getBaseDir().getParent().getParent().getPath();
        else {
            dirs = new FileChooserDescriptor(false,true,false,false,false,false);
            VirtualFile[] chooseFiles = FileChooser.chooseFiles(dirs, project, null);
            rootPath = chooseFiles.length>0?chooseFiles[0].getPath():"";
        }
        System.out.print("rootPath : "+rootPath);
        if(rootPath.equals("")) {
            new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "创建Project失败", "请选择代码存放目录!!!", NotificationType.WARNING).notify(project);
            return;
        }
        File newCodeDir = new File(rootPath, rootName+"/"+project.getName());
        if(newCodeDir.exists()) {
            new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "创建Project失败", "目录已经存在，请重新选择: "+newCodeDir.getPath(), NotificationType.WARNING).notify(project);
            return;
        }else newCodeDir.mkdirs();

        String basePath = project.getBasePath();
        List<String> successFile = new ArrayList<>();
        for (String fileName :copyFiles) {
            if(FileUtils.copyFile(new File(basePath, fileName), new File(newCodeDir, fileName))){
                successFile.add(fileName);
            }
        }
        if(successFile.isEmpty()){
            new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "创建Project失败", "拷贝工程文件失败: "+copyFiles.toString(), NotificationType.WARNING).notify(project);
            return;
        }
        if(Messages.showOkCancelDialog("拷贝文件成功,是否打开新工程:\n"+successFile,"Open Project",null)!=0) return;

        try {
            ProjectManager.getInstance().loadAndOpenProject(newCodeDir.getPath());
        } catch (Exception e1) {
            new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "打开Project失败", e.toString(), NotificationType.ERROR).notify(project);
            return;
        }

    }
}
