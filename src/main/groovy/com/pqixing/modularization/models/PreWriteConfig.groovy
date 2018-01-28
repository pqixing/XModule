package com.pqixing.modularization.models

import com.alibaba.fastjson.JSON
import com.pqixing.modularization.utils.FileUtils
import com.pqixing.modularization.utils.NormalUtils
import org.gradle.api.Project
/**
 * Created by pqixing on 17-12-7.
 */

class PreWriteConfig extends BaseExtension {
    Project project
    HashMap<String, String> writeConfigs

    PreWriteConfig(Project project) {
        this.project = project
        writeConfigs = new HashMap<>()
    }

    void addConfig(Map<String, String> configs) {
        writeConfigs += configs
    }
    @Override
    LinkedList<String> generatorFiles() {

        BuildConfig buildConfig = project.buildConfig
        ModuleConfig moduleConfig = project.moduleConfig

        String appLikeValue = "/applike/$buildConfig.projectName"
        appLikeValue = "/${appLikeValue.hashCode()}$appLikeValue".replace("-","_")
        addConfig(["NAME": buildConfig.projectName, "PATH_APPLIE": appLikeValue,"BUILD_TIME":System.currentTimeMillis().toString(),"BUILD_TIME_STR":new Date().toLocaleString()])
        addConfig(["GIT_COMMIT": project.lastCommit,"DEPENDENCIES": JSON.toJSONString(moduleConfig.dependModules.dependModules).replace("\"","")])


        String className = "${buildConfig.projectName}Config".replace("-","_")
        className = className.substring(0,1).toUpperCase()+className.substring(1)
        def clsString = new StringBuilder("package auto.#{packageName};\n")
        clsString.append("public final class $className { \n")
        writeConfigs.each { map ->
            clsString.append("public static final String $map.key = \"$map.value\"; \n")
        }
        clsString.append("} \n")
        String fileName = FileUtils.appendUrls(buildConfig.javaDir, "auto",buildConfig.packageName.replace('.', File.separator), "${className}.java")
        FileUtils.write(new File(fileName), NormalUtils.parseString(clsString.toString(), ["packageName": buildConfig.packageName]))
        return [FileUtils.write(new File(buildConfig.cacheDir, "java.gradle"), NormalUtils.parseString(sourceSetTxt, ["javaDir": buildConfig.javaDir]))]
    }

    String getSourceSetTxt() {
        return '''
android{
 sourceSets {
        //在main目录中
        main {
            java.srcDirs += "#{javaDir}"
        }
    }
}
'''
    }
}
