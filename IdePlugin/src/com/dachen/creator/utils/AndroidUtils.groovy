package com.dachen.creator.utils

import com.dachen.creator.Conts
import com.dachen.creator.ui.MultiBoxDialog
import com.intellij.codeInsight.navigation.BackgroundUpdaterTask
import com.intellij.codeInsight.navigation.ListBackgroundUpdaterTask
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.JavaCommandLineState
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.OSProcessHandler
import com.intellij.ide.ui.EditorOptionsTopHitProvider.Ex
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.Application
import com.intellij.openapi.externalSystem.util.ExternalSystemSettingsControl
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.progress.PerformInBackgroundOption
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlDocument
import com.intellij.util.EnvironmentUtil
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

public class AndroidUtils {

    /**
     * 获取App对应的包名根目录
     */
    public static VirtualFile getAppPackageBaseDir(Project project, String modulePath) {
        String path = modulePath + File.separator +
                "src" + File.separator +
                "main" + File.separator +
                "java" + File.separator +
                getAppPackageName(project, modulePath).replace(".", File.separator);
        return LocalFileSystem.getInstance().findFileByPath(path);
    }

    /**
     * 根据类名获取包名路径
     */
    public static VirtualFile getAppPackageByClass(Project project, String fullClass, String moduleName) {
        String pkg = fullClass.substring(0, fullClass.lastIndexOf(".")).replace(".", File.separator);
        String path = project.getBasePath() + File.separator +
                moduleName + File.separator +
                "src" + File.separator +
                "main" + File.separator +
                "java" + File.separator +
                pkg;
        return LocalFileSystem.getInstance().findFileByPath(path);
    }

    /**
     * 根据类名获取包名路径
     */
    public static VirtualFile getAppPackageBySimpleClass(PsiClass clazz, String simpleClassName) {
        PsiFile file = clazz.getContainingFile();
        String path = file.getVirtualFile().getPath();
        String pkgPath = path.substring(0, path.indexOf(simpleClassName));
        return LocalFileSystem.getInstance().findFileByPath(pkgPath);
    }

    @Nullable
    public static VirtualFile getPackageByName(PsiClass clazz, String className, String pkgName) {
        // app包名根目录 ...\app\src\main\java\PACKAGE_NAME\
        VirtualFile pkgDir = AndroidUtils.getAppPackageBySimpleClass(clazz, className);
        // 判断根目录下是否有对应包名文件夹
        VirtualFile realDir = pkgDir.findChild(pkgName);
        if (realDir == null) {
            // 没有就创建一个
            try {
                realDir = pkgDir.createChildDirectory(null, pkgName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return realDir;
    }


    public static PsiFile getManifestFile(Project project, String modulePath) {
        String path = modulePath + File.separator +
                "src" + File.separator +
                "main" + File.separator +
                "AndroidManifest.xml";
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(path);
        if (virtualFile == null) return null;
        return PsiManager.getInstance(project).findFile(virtualFile);
    }

    public static String getAppPackageName(Project project, String modulePath) {
        PsiFile manifestFile = getManifestFile(project, modulePath);
        XmlDocument xml = (XmlDocument) manifestFile.getFirstChild();
        return xml.getRootTag().getAttribute("package").getValue();
    }

    public static String getFilePackageName(VirtualFile dir) {
        if (!dir.isDirectory()) {
            // 非目录的取所在文件夹路径
            dir = dir.getParent();
        }
        String path = dir.getPath().replace("/", ".");
        String preText = "src.main.java";
        int preIndex = path.indexOf(preText) + preText.length() + 1;
        path = path.substring(preIndex);
        return path;
    }
    /**
     * 获取所有的设备信息
     * @return
     */
    public static List<String> getDevices(Project project) {
        try {
            Set<String> devices = new HashSet<>()
            GitUtils.run("${FileUtils.readConfig("adb", "adb")} devices -l", new File(project.getBasePath()))?.eachLine { l ->
                if (l.contains("*") || l.startsWith("List") || l.trim().isEmpty()) return

                def split = l.trim().split(" ")
                String num = split[0]
                String d = l.split(" ").find { it.trim().startsWith("model:") }.trim().replace("model:", "")

                String r = "$num    $d"
                devices.add(r)
            }
            return devices.toList()
        } catch (Exception e) {
            return null
        }
    }
    /**
     * 安装设备
     * @param devices
     * @param apk
     */
    public static void adbInstall(Project project, List<String> devices, File apk) {
        def install = new Task.Backgroundable(project, "Start Install", true) {
            @Override
            void run(@NotNull ProgressIndicator progressIndicator) {
                progressIndicator.start()
                progressIndicator.setText("安装中")
                StringBuilder resutStr = new StringBuilder()
                for (String s : devices) {
                    progressIndicator.setText("Install to $s")
                    def run = GitUtils.run("/Tools/linux-sdk/platform-tools/adb install -r $apk.absolutePath", new File(project.getBasePath()))
                    resutStr.append("$s : $run   ->  ")
                }
                progressIndicator.setText("Install Finish")
                progressIndicator.cancel()
                new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "Install Finish", resutStr.toString(), NotificationType.INFORMATION).notify(project)
            }
        }
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(install, new BackgroundableProcessIndicator(install))
    }

    /**
     * 安装应用
     * @param apk
     * @return
     */
    public static void installApk(Project project, File apk) {
        if (!apk.exists()) {
            new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "Install Fail", "apk not exits $apk.absolutePath", NotificationType.WARNING).notify(project)
            return
        }
        def devices = getDevices(project)
        if (devices == null) {
            int exitCode = Messages.showOkCancelDialog("adb 命令执行失败,请先选择adb目录", "选择adb工具", null)
            if (exitCode != 0) return

            FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
            VirtualFile[] chooseFiles = FileChooser.chooseFiles(descriptor, project, null)
            if (chooseFiles.length <= 0) return
            File adb = new File(chooseFiles[0].getPath(), "adb")
            if (!adb.exists()) {
                new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "Choose Fail", "该目录没有不包含adb工具,请重新选择", NotificationType.WARNING).notify(project)
                return
            }
            FileUtils.saveConfig("adb", adb.absolutePath)
            devices = getDevices(project)
        }
        if (devices == null) {
            Messages.showOkCancelDialog("请检查adb工具是否正常", "adb命令异常", null)
            return
        }

        MultiBoxDialog.builder(project)
                .setMode(true, true, false)
                .setMsg("选择设备", "请选择需要安装的设备")
                .setInput(devices[0])
                .setItems(devices)
                .setHint("请勾选需要安装的设备")
                .setListener(new MultiBoxDialog.Listener() {
            @Override
            void onOk(String input, List<String> items, boolean check) {
                input = input.trim()
                if (!input.isEmpty() && !items.contains(input)) {
                    items.add(input, 0)
                }
                if (items.isEmpty()) {
                    new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "Install Fail", "没有选中的设备", NotificationType.WARNING).notify(project)
                    return
                }
                new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "Start Install", "正在安装应用到${items.size()}台设备", NotificationType.INFORMATION).notify(project)
                adbInstall(project, items, apk)
            }

            @Override
            void onCancel() {

            }
        }).show()
    }
}