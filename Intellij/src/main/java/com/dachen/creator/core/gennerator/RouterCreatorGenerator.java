package com.dachen.creator.core.gennerator;

import com.dachen.creator.core.RouterCodeFactory;
import com.dachen.creator.utils.AndroidUtils;
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
        String fullClassName = clazz.getQualifiedName();
        String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1, fullClassName.length()) + ".java";
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
        //virtualFile = pkgDir.findChild(className);

        //PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
       /* PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
        PsiClass fileClass = PluginUtils.getFileClass(initFile);
        PsiField[] fields = clazz.getAllFields();
        for (PsiField field : fields) {
            if (fieldFilter(field)) {
                continue;
            }

            // 用拼接的代码生成innerClass
            String innerCode = RouterCodeFactory.generatInnerClass(field);
            PsiClass innerClass = factory.createClassFromText(innerCode, initFile);
            fileClass.add(innerClass.getInnerClasses()[0]);
        }*/
    }




}
