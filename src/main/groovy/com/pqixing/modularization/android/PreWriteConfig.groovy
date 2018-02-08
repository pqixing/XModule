package com.pqixing.modularization.android

import com.alibaba.fastjson.JSON
import com.pqixing.modularization.base.BaseExtension
import com.pqixing.modularization.common.BuildConfig
import com.pqixing.modularization.dependent.Dependencies
import com.pqixing.modularization.git.GitConfig
import com.pqixing.modularization.utils.FileUtils
import com.pqixing.modularization.utils.TextUtils
import org.gradle.api.Project

/**
 * Created by pqixing on 17-12-7.
 */

class PreWriteConfig extends BaseExtension {
    HashMap<String, String> configs

    PreWriteConfig(Project project) {
        super(project)
        configs = new HashMap<>()
    }

    void addConfig(Map<String, String> configs) {
        this.configs += configs
    }

    @Override
    LinkedList<String> getOutFiles() {

        def buildConfig = wrapper.getExtends(BuildConfig)
        def gitConfig = wrapper.getExtends(GitConfig)
        def dependent = wrapper.getExtends(Dependencies)

        String appLikeValue = "/applike/$buildConfig.projectName"
        appLikeValue = "/${appLikeValue.hashCode()}$appLikeValue"
        addConfig(["NAME": buildConfig.projectName, "PATH_APPLIE": appLikeValue, "BUILD_TIME": System.currentTimeMillis().toString(), "BUILD_TIME_STR": new Date().toLocaleString()])
        addConfig(["GIT_COMMIT_LOG": gitConfig.lastLog, "GIT_COMMIT_NUM": gitConfig.revisionNum, "DEPENDENCIES": JSON.toJSONString(dependent.modules).replace("\"", "")])

        def confStr = new StringBuilder()
        configs.each { confStr.append("public static final String $it.key = \"$it.value\"; \n") }

        def writes = new auto.Prewrite(buildConfig.properties)
        String className = TextUtils.firstUp("${buildConfig.projectName}Config")
        writes.params += ["preConfigs": confStr.toString(), "className": className]

        FileUtils.write(FileUtils.getFileForClass(buildConfig.javaDir, "${buildConfig.javaPackage}.$className"), writes.configClass)
        return [FileUtils.write(new File(buildConfig.cacheDir, "java.gradle"), writes.sourceSet)]
    }
}
