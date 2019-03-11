package com.dachen.creator.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlDocument;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

;

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