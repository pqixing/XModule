package com.pqixing.modularization.configs

import com.pqixing.modularization.base.BaseExtension
import com.pqixing.modularization.base.BasePlugin
import com.pqixing.modularization.utils.FileUtils
import com.pqixing.modularization.utils.TextUtils
import org.gradle.api.Project
/**
 * Created by pqixing on 17-12-7.
 * 基础信息编译生成的
 */
class BuildConfig extends BaseExtension {
    //默认路径
    public static final String dirName = ".modularization"
    //网络进行10秒钟缓存
    public static final long netCacheTime = 1000*20
    public static final long timOut = 1000*4
    public static final String rootOutDir = FileUtils.urls(BasePlugin.rootProject.rootDir.absolutePath,dirName)
    public static final String versionDir = FileUtils.urls(rootOutDir,"versions")
    public static final String netCacheDir = FileUtils.urls(rootOutDir,"netCache")

    final String projectName
    //根目录的输出路径
    //当前工程的输出路径
    final String outDir
    final String cacheDir
    final String javaDir

    /**
     * 默认的内网镜像依赖组
     */
    String MavenGroupUrl = "http://192.168.3.7:9527/nexus/content/groups/androidgroup/"

    String packageName
    String groupName

    BuildConfig(Project project) {
        super(project)
        projectName = TextUtils.numOrLetter(project.name).toLowerCase()
        outDir =  FileUtils.urls(project.projectDir.absolutePath, dirName)
        cacheDir = FileUtils.urls(outDir, ".cache")
        javaDir = FileUtils.urls(outDir, "java")
        packageName = "${GlobalConfig.groupName}.$projectName"
    }

    @Override
    LinkedList<String> generatorFiles() {
        return null
    }
}
