package com.dachen.creator.core.gennerator;

import com.dachen.creator.utils.AndroidUtils;
import com.dachen.creator.utils.PluginUtils;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.dachen.creator.core.MVPCodeFactory;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class MVPCreatorGenerator {

    public static void genCode(Project project, String classPrefix, String modulePath, boolean isActivity) {
        genConstractFile(project, classPrefix, modulePath);
        genModelImplFile(project, classPrefix, modulePath);
        genPresenerImplFile(project, classPrefix, modulePath);
        if(isActivity){
            genActivityFile(project, classPrefix, modulePath);
        }else {
            genFragmentFile(project, classPrefix, modulePath);
        }
    }

    private static void genConstractFile( Project project, String classPrefix, String modulePath) {
        VirtualFile dbDir = getPackageByName(project, "contract", modulePath);
        String name = classPrefix + "Contract.java";
        VirtualFile virtualFile = dbDir.findChild(name);
        if(virtualFile == null) {
            // 没有就创建一个，第一次使用代码字符串创建个类
            PsiFile initFile = PsiFileFactory.getInstance(project).createFileFromText(
                    name, JavaFileType.INSTANCE, MVPCodeFactory.generatContract(dbDir,classPrefix));

            // 加到db目录下
            PsiManager.getInstance(project).findDirectory(dbDir).add(initFile);
            virtualFile = dbDir.findChild(name);
        }

        PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
        // 用拼接的代码生成MvpModel interface
        String mvpModelString = MVPCodeFactory.getMvpModelString();
        PsiClass mvpModelClass = factory.createClassFromText(mvpModelString, psiFile);
        // 用拼接的代码生成MvpModel interface
        String mvpPresenterlString = MVPCodeFactory.getMvpPrecenterString();
        PsiClass mvpPresenterClass = factory.createClassFromText(mvpPresenterlString, psiFile);
        // 用拼接的代码生成MvpModel interface
        String mvpViewString = MVPCodeFactory.getMvpViewString();
        PsiClass mvpViewClass = factory.createClassFromText(mvpViewString, psiFile);

        // 将创建的class添加到DataContract Class中
        PsiClass fileClass = PluginUtils.getFileClass(psiFile);
        fileClass.add(mvpModelClass.getInnerClasses()[0]);
        fileClass.add(mvpPresenterClass.getInnerClasses()[0]);
        fileClass.add(mvpViewClass.getInnerClasses()[0]);
    }

    private static void genModelImplFile( Project project, String classPrefix, String modulePath) {
        VirtualFile dbDir = getPackageByName(project, "model", modulePath);
        String name = classPrefix + "Model.java";
        VirtualFile virtualFile = dbDir.findChild(name);
        if(virtualFile == null) {
            // 没有就创建一个，第一次使用代码字符串创建个类
            PsiFile initFile = PsiFileFactory.getInstance(project).createFileFromText(
                    name, JavaFileType.INSTANCE, MVPCodeFactory.generatModelImpl(dbDir,classPrefix));

            // 加到db目录下
            PsiManager.getInstance(project).findDirectory(dbDir).add(initFile);
        }
    }

    private static void genPresenerImplFile( Project project, String classPrefix, String modulePath) {
        VirtualFile dbDir = getPackageByName(project, "presenter", modulePath);
        String name = classPrefix + "Presenter.java";
        VirtualFile virtualFile = dbDir.findChild(name);
        if(virtualFile == null) {
            // 没有就创建一个，第一次使用代码字符串创建个类
            PsiFile initFile = PsiFileFactory.getInstance(project).createFileFromText(
                    name, JavaFileType.INSTANCE, MVPCodeFactory.generatPrensterImpl(dbDir,classPrefix));

            // 加到db目录下
            PsiManager.getInstance(project).findDirectory(dbDir).add(initFile);
        }
    }

    private static void genActivityFile( Project project, String classPrefix, String modulePath) {
        VirtualFile actDir = getPackageByName(project, "activity", modulePath);
        String name = classPrefix + "Activity.java";
        VirtualFile virtualFile = actDir.findChild(name);
        if(virtualFile == null) {
            // 没有就创建一个，第一次使用代码字符串创建个类
            PsiFile initFile = PsiFileFactory.getInstance(project).createFileFromText(
                    name, JavaFileType.INSTANCE, MVPCodeFactory.generatActivity(actDir,classPrefix));

            // 加到db目录下
            PsiManager.getInstance(project).findDirectory(actDir).add(initFile);
        }
    }

    private static void genFragmentFile( Project project, String classPrefix, String modulePath) {
        VirtualFile dbDir = getPackageByName(project, "fragment", modulePath);
        String name = classPrefix + "Fragment.java";
        VirtualFile virtualFile = dbDir.findChild(name);
        if(virtualFile == null) {
            // 没有就创建一个，第一次使用代码字符串创建个类
            PsiFile initFile = PsiFileFactory.getInstance(project).createFileFromText(
                    name, JavaFileType.INSTANCE, MVPCodeFactory.generatFragment(dbDir,classPrefix));

            // 加到db目录下
            PsiManager.getInstance(project).findDirectory(dbDir).add(initFile);
        }
    }

    @Nullable
    private static VirtualFile getPackageByName(Project project, String pkgName, String modulePath) {
        // app包名根目录 ...\app\src\main\java\PACKAGE_NAME\
        VirtualFile baseDir = AndroidUtils.getAppPackageBaseDir(project, modulePath);

        // 判断根目录下是否有对应包名文件夹
        VirtualFile dbDir = baseDir.findChild(pkgName);
        if(dbDir == null) {
            // 没有就创建一个
            try {
                dbDir = baseDir.createChildDirectory(null, pkgName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return dbDir;
    }

}
