package com.pqixing.modularization.common

import com.pqixing.modularization.Keys
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
    public static final long netCacheTime = 1000 * 20
    public static final long timOut = 5
    public static
    final String rootOutDir = FileUtils.urls(BasePlugin.rootProject.rootDir.absolutePath, dirName)
    public static final String versionDir = FileUtils.urls(rootOutDir, "versions")
    public static final String netCacheDir = FileUtils.urls(rootOutDir, "netCache")
    public static final String mavenRecordFile = FileUtils.urls(rootOutDir, Keys.MAVEN_RECORD)

    final String projectName
    //根目录的输出路径
    //当前工程的输出路径
    final String outDir
    final String cacheDir
    final String cacheJavaDir
    final String javaDir


    final String packageName
    //java包名
    final String javaPackage
    final String groupName
    /**
     * 输出文档路径
     */
    String docDir

    BuildConfig(Project project) {
        super(project)
        projectName = TextUtils.numOrLetter(project.name).toLowerCase()
        outDir = FileUtils.urls(project.projectDir.path, dirName).replace("\\","/")
        docDir = FileUtils.urls(project.projectDir.path, GlobalConfig.docDirName).replace("\\","/")

        cacheDir = FileUtils.urls(outDir, ".cache")
        javaDir = FileUtils.urls(outDir, "java")
        cacheJavaDir = FileUtils.urls(cacheDir, "java")
        groupName = GlobalConfig.groupName
        packageName = "${groupName}.$projectName"
        javaPackage = "${Keys.PREFIX_PKG}.$packageName"
    }

    @Override
    LinkedList<String> getOutFiles() {
        return null
    }
}