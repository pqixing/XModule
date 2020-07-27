package com.pqixing.creator.core.gennerator;

import com.pqixing.creator.core.RouterCodeFactory;
import com.pqixing.creator.utils.AndroidUtils;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;

import java.io.IOException;

public class RouterCreatorGenerator {
    public static void genCode(Project project, PsiClass clazz) {
        genRouterPathsFile(project, clazz);
    }

    private static void genRouterPathsFile(Project project, PsiClass clazz) {
        String className = clazz.getName() + ".java";
        VirtualFile pkgDir = AndroidUtils.getPackageByName(clazz, className, "proxy");
        VirtualFile virtualFile = pkgDir.findChild(className);
        if(virtualFile != null){
            try {
                // 如果存在就删除
                virtualFile.delete(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        PsiFile initFile = PsiFileFactory.getInstance(project).createFileFromText(
                className, JavaFileType.INSTANCE, RouterCodeFactory.generatRouterProxy(clazz));
        // 加到包目录下
        PsiManager.getInstance(project).findDirectory(pkgDir).add(initFile);
    }




}
