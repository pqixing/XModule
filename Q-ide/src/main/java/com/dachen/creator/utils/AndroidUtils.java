package com.dachen.creator.utils;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.tools.apk.analyzer.AaptInvoker;
import com.android.tools.apk.analyzer.AndroidApplicationInfo;
import com.android.tools.idea.apk.viewer.ApkParser;
import com.android.tools.idea.sdk.AndroidSdks;
import com.android.tools.log.LogWrapper;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlDocument;

import com.pqixing.intellij.adapter.JListInfo;
import com.pqixing.intellij.utils.UiUtils;
import kotlin.Pair;
import org.jetbrains.android.sdk.AndroidSdkUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class AndroidUtils {
    public static String lastDevices = "";

    public static IDevice getSelectDevice(final Project project, final JComboBox comboBox) {
        String id = comboBox.getSelectedItem().toString();
        for (Pair<String, IDevice> p : getDevices(project)) {
            if (id.equals(p.getFirst())) {
                lastDevices = p.getSecond().getSerialNumber();
                return p.getSecond();
            }
        }
        return null;
    }

    public static void initDevicesComboBox(final Project project, final JButton refreshButton, final JComboBox comboBox) {
        ActionListener a = actionEvent -> {
            List<Pair<String, IDevice>> pairs = getDevices(project);
            comboBox.removeAllItems();
            for (Pair p : pairs) {
                comboBox.addItem(p.getFirst());
            }
        };
        a.actionPerformed(null);
        if (refreshButton != null) refreshButton.addActionListener(a);
    }

    public static List<Pair<String, IDevice>> getDevices(Project project) {
        AndroidDebugBridge bridge = AndroidSdkUtils.getDebugBridge(project);
        ArrayList<Pair<String, IDevice>> infos = new ArrayList<>();
        if (bridge != null) for (IDevice d : bridge.getDevices()) {
            String avdName = d.getAvdName();
            if (avdName == null)
                avdName = UiUtils.adbShellCommon(d, "getprop ro.product.brand", true) + "-" + UiUtils.adbShellCommon(d, "getprop ro.product.model", true);
            Pair<String, IDevice> pair = new Pair<>(avdName, d);
            if (lastDevices.equals(d.getSerialNumber())) infos.add(0, pair);
            else infos.add(pair);
        }
        return infos;
    }


    public static AndroidApplicationInfo getAppInfoFromApk(File fileApk) {
        try {
            AaptInvoker invoker = new AaptInvoker(AndroidSdks.getInstance().tryToChooseSdkHandler(), new LogWrap());
            List<String> xmlTree = invoker.getXmlTree(fileApk, "AndroidManifest.xml");
            return AndroidApplicationInfo.parse(xmlTree);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

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
        // app包名根目录 ...\app\src\main\iterface\PACKAGE_NAME\
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
}