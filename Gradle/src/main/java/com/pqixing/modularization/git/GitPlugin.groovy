package com.pqixing.modularization.git

import auto.Moulds
import com.pqixing.modularization.Keys
import com.pqixing.modularization.base.BasePlugin
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.common.BuildConfig
import com.pqixing.modularization.common.GlobalConfig
import com.pqixing.modularization.gradle.base.BasePlugin
import com.pqixing.modularization.gradle.base.BaseTask
import com.pqixing.modularization.gradle.common.BuildConfig
import com.pqixing.modularization.gradle.common.GlobalConfig
import com.pqixing.modularization.maven.IndexMavenTask
import com.pqixing.modularization.utils.FileUtils
import com.pqixing.modularization.utils.GitUtils
import com.pqixing.modularization.utils.MavenUtils
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle

/**
 * Created by pqixing on 17-12-20.
 */

class GitPlugin extends com.pqixing.modularization.base.BasePlugin {
    public static final String mouldVersion = "//2.4"
    /**
     * 设置页面文件
     */
    public static final String SETTING_FILE = "moulds.gradle"

    @Override
    Set<String> getIgnoreFields() {
        return ["config.gradle", "setting.kt", "local.gradle", "git.properties", "config2.gradle"]
    }

    @Override
    void apply(Project project) {
        initProject(project)
        GlobalConfig.init()
        super.apply(project)
        addMouldGradle()
        addGradleFile()
        if (writeMouldGradle()) {
            throw new RuntimeException("设置文件更新，请重新同步 --init setting file, please sync again ")
        }

        com.pqixing.modularization.base.BaseTask.task(project, UpdateCodeTask.class)
        com.pqixing.modularization.base.BaseTask.task(project, CheckOutTask.class)
        com.pqixing.modularization.base.BaseTask.task(project, CloneAllTask.class)
        com.pqixing.modularization.base.BaseTask.task(project, VersionTagTask.class)
        com.pqixing.modularization.base.BaseTask.task(project, CreateBranchTask.class)
        com.pqixing.modularization.base.BaseTask.task(project, DeleteBranchTask.class)
        com.pqixing.modularization.base.BaseTask.task(project, IndexMavenTask.class)
        com.pqixing.modularization.base.BaseTask.task(project, LogAllGitTask.class)
        com.pqixing.modularization.base.BaseTask.task(project, FastMergeTask.class)
        project.task("PushDocument").doFirst {
            com.pqixing.modularization.gradle.utils.MavenUtils.pushMaven()
        }

        readGitProject(project.gradle)
        applyDefaultGradle()
        applyLocalGradle()
    }

    void addGradleFile() {
        def gradleProperties = new File(project.rootDir, Keys.NAME_PRO_GRADLE)
        if (!gradleProperties.exists()) {
            com.pqixing.modularization.gradle.utils.FileUtils.write(gradleProperties, new Moulds().gradleProperteis)
        }
    }
    /**
     * 如果文档库中有default.gradle文件，则应用
     */
    void applyDefaultGradle() {
        String docDir = com.pqixing.modularization.gradle.utils.GitUtils.getNameFromUrl(GlobalConfig.docGitUrl)
        def defaultGradle = new File(rootProject.rootDir.parentFile, "$docDir/$Keys.NAME_GRADLE_DEFAULT")
        if (defaultGradle.exists()) wrapper.apply from: defaultGradle.path
    }
    /**
     * 如果有本地gradle文件，则使用
     */
    void applyLocalGradle() {
        def localGradle = new File(project.rootDir, Keys.LOCAL_GRADLE)
        if (localGradle.exists()) wrapper.apply from: localGradle.path
    }

    /**
     * 从Gradle中获取git工程的信息
     * @param gradle
     */
    void readGitProject(Gradle gradle) {
        GitConfig.allGitProjects.clear()
//        GitConfig.email = gradle.ext.gitEmail
        GitConfig.baseGitUrl = gradle.ext.baseGitUrl
        GitConfig.userName = gradle.ext.gitUserName
        GitConfig.password = gradle.ext.gitPassword
        GitConfig.localProject = gradle.ext.localProject

        HashMap<String, List<String>> submodules = gradle.ext.submodules
        gradle.projectUrls.each { map ->
            def project = new GitProject()
            project.name = map.key
            project.gitUrl = map.value.split(Keys.SEPERATOR)[0]
            project.introduce = map.value.replace("$project.gitUrl$Keys.SEPERATOR", "")
            GitConfig.allGitProjects.add(project)
            def childs = submodules[map.key]
            if (childs != null) project.submodules.add(childs)
        }
    }
    /**
     * 修改原来的setting文件
     */
    void addMouldGradle() {
        File setting = new File(project.rootDir, "settings.gradle")
        if (!setting.exists()) setting.createNewFile()
        if (!setting.text.contains(Keys.TAG_AUTO_ADD))
            setting.append("\nfile('$BuildConfig.dirName/$SETTING_FILE').with {  if(exists()) apply from: path} //$Keys.TAG_AUTO_ADD")
    }
    /**
     * 输出模板设置文件
     */
    boolean writeMouldGradle() {
        File mouldFile = new File(BuildConfig.rootOutDir, SETTING_FILE)
        String oldVersion = ""
        //如果模板已经存在，并且版本号不小于当前，则不需要重写
        if (mouldFile.exists() && (oldVersion = mouldFile.readLines()[0].trim()) >= mouldVersion) return false
        Moulds moulds = new Moulds()
        moulds.params += ["defaultXmlGitUrl": GlobalConfig.docGitUrl]
        moulds.params += ["com.pqixing.modularization.gradle.forOut.AutoInclude": moulds.autoInclude]
        com.pqixing.modularization.gradle.utils.FileUtils.write(mouldFile, "$mouldVersion\n$moulds.settingGradle")

        return true
    }

    @Override
    String getPluginName() {
        return Keys.NAME_GIT
    }
}
