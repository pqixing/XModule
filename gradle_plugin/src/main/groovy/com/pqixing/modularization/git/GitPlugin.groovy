package com.pqixing.modularization.git

import auto.Moulds
import com.pqixing.modularization.Keys
import com.pqixing.modularization.base.BasePlugin
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.common.BuildConfig
import com.pqixing.modularization.common.GlobalConfig
import com.pqixing.modularization.maven.UpdateMavenTask
import com.pqixing.modularization.utils.FileUtils
import com.pqixing.modularization.utils.GitUtils
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle

/**
 * Created by pqixing on 17-12-20.
 */

class GitPlugin extends BasePlugin {
    public static final String mouldVersion = "//1.5"
    /**
     * 设置页面文件
     */
    public static final String SETTING_FILE = "moulds.gradle"

    @Override
    Set<String> getIgnoreFields() {
        return ["config.gradle", "include.kt", "local.gradle", "git.properties", "config2.gradle"]
    }

    @Override
    void apply(Project project) {
        initProject(project)
        GlobalConfig.init()
        super.apply(project)
        addMouldGradle()
        addGradleFile()
        if (writeMouldGradle()) {
            throw new RuntimeException("init setting file, please sync again -- 初始化设置，请重新同步")
        }

        BaseTask.task(project, GitUpdateTask.class)
        BaseTask.task(project, CheckBranchTask.class)
        BaseTask.task(project, GitCloneAllTask.class)
        BaseTask.task(project, CheckMasterTask.class)
        BaseTask.task(project, ModuReleaseTask.class)
        BaseTask.task(project, DelModuReleaseTask.class)
        BaseTask.task(project, UpdateMavenTask.class)

        readGitProject(project.gradle)
        applyDefaultGradle()
        applyLocalGradle()
    }

    void addGradleFile() {
        def gradleProperties = new File(project.rootDir, Keys.NAME_PRO_GRADLE)
        if (!gradleProperties.exists()) {
            FileUtils.write(gradleProperties, new Moulds().gradleProperteis)
        }
    }
    /**
     * 如果文档库中有default.gradle文件，则应用
     */
    void applyDefaultGradle() {
        String docDir = GitUtils.getNameFromUrl(GlobalConfig.docGitUrl)
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
        GitConfig.email = gradle.gitEmail
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
        moulds.params += ["AutoInclude": moulds.autoInclude]
        FileUtils.write(mouldFile, "$mouldVersion\n$moulds.settingGradle")
        new File(project.rootDir, "include.txt").delete()
        new File(project.rootDir, "hideInclude.txt").delete()
        new File(project.rootDir, "hideInclude.kt").delete()

        return true
    }

    @Override
    String getPluginName() {
        return Keys.NAME_GIT
    }
}
