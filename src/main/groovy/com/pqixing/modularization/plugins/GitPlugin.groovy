package com.pqixing.modularization.plugins

import auto.Moulds
import com.pqixing.modularization.Keys
import com.pqixing.modularization.base.BasePlugin
import com.pqixing.modularization.configs.BuildConfig
import com.pqixing.modularization.configs.GlobalConfig
import com.pqixing.modularization.git.GitConfig
import com.pqixing.modularization.git.GitProject
import com.pqixing.modularization.utils.FileUtils
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle

/**
 * Created by pqixing on 17-12-20.
 */

class GitPlugin extends BasePlugin {
    public static final String mouldVersion = "//1.0"
    /**
     * 设置页面文件
     */
    public static final String SETTING_FILE = "moulds.gradle"

    @Override
    Set<String> getIgnoreFields() {
        return ["config.gradle", "include.txt", "local.gradle", "git.properties", "config2.gradle"]
    }

    @Override
    void apply(Project project) {
        super.apply(project)
        addMouldGradle()
        if (writeMouldGradle()) {
            throw new RuntimeException("init setting file, please sync again -- 初始化设置，请重新同步")
        }
//        readGitProject(project.gradle)
    }

    /**
     * 从Gradle中获取git工程的信息
     * @param gradle
     */
    void readGitProject(Gradle gradle) {
        def gitConfig = wrapper.getExtends(GitConfig)
        gitConfig.email = gradle.gitEmail
        gitConfig.baseGitUrl = gradle.ext.baseGitUrl
        gitConfig.userName = gradle.ext.gitUserName
        gitConfig.password = gradle.ext.gitPassword
        gitConfig.localProject = gradle.ext.localProject

        HashMap<String, List<String>> submodules = gradle.ext.submodules
        gradle.projectUrls.each { map ->
            def project = new GitProject()
            project.name = map.key
            project.gitUrl = map.value.split(Keys.SEPERATOR)[0]
            project.introduce = map.value.replace("$project.gitUrl$Keys.SEPERATOR", "")
            gitConfig.allGitProjects += project
            def childs = submodules[map.key]
            if (childs != null) project.submodules += childs
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
        //如果模板已经存在，并且版本号不小于当前，则不需要重写
        if (mouldFile.exists() && mouldFile.readLines()[0].trim() >= mouldVersion) return false
        Moulds moulds = Moulds.with()
        moulds.params += ["defaultXmlGitUrl":GlobalConfig.docGitUrl]
        moulds.params += ["AutoInclude":moulds.autoInclude]
        FileUtils.write(mouldFile, "$mouldVersion\n$moulds.settingGradle")
        return true
    }

    @Override
    String getPluginName() {
        return Keys.NAME_GIT
    }
}
