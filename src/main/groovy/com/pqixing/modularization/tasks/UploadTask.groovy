package com.pqixing.modularization.tasks

import com.pqixing.modularization.Default
import com.pqixing.modularization.models.MavenType
import com.pqixing.modularization.utils.FileUtils
import com.pqixing.modularization.utils.NormalUtils
import com.pqixing.modularization.utils.Print
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class UploadTask extends DefaultTask {

    MavenType mavenInfo

    UploadTask() {
        group = Default.taskGroup
    }


    @TaskAction
    void generatorMavenFile() {
        def pros = mavenInfo.properties
        if ("test" == mavenInfo.name) {//测试环境，读取
            def versionKey = "${mavenInfo.groupName}android:${mavenInfo.artifactId}".hashCode().toString()
            def configFile = new File(project.gradle.gradleUserHomeDir, "modularization.config")
            if (!configFile.exists()) {
                configFile.parentFile.mkdirs()
                configFile.createNewFile()
            }


            def versionPros = new Properties()
            versionPros.load(configFile.newInputStream())

            String version = versionPros.getProperty(versionKey)

            String newVersion = NormalUtils.isEmpty(versionKey) ? "1" : (version.toInteger() + 1).toString()

            versionPros.setProperty(versionKey, newVersion)
            versionPros.store(configFile.newOutputStream(), "")

            pros["pom_version"] = "${mavenInfo.pom_version}.${newVersion}"

        } else if ("debug" == mavenInfo.name) {//测试环境，直接在版本后添加时间戳
            pros["pom_version"] = "${mavenInfo.pom_version}.${System.currentTimeMillis()}"
        }
        def file = new File(project.buildConfig.cacheDir, "${name}maven.gradle")

        project.apply from: FileUtils.write(file, NormalUtils.parseString(mavenTxt, pros))
    }

    @TaskAction
    void uploadFile() {
        Print.l("uploadFile  name = $mavenInfo.name ---------------------")
        project.uploadArchives.execute()
    }

    @TaskAction
    void deleteMavenFile() {

    }

/**
 * 获取maven的文本类型
 * @return
 */
    static String getMavenTxt() {
        return '''
apply plugin: "maven"
// 上传到本地代码库
uploadArchives{
    repositories{
        mavenDeployer{
            repository(url:#{maven_url}){
                authentication(userName: "#{userName}", password: "#{password}")
            }
            pom.groupId = '#{groupName}.android' // 组名
            pom.artifactId = '#{artifactId}' // 插件名
            pom.version = '#{pom_version}' // 版本号
        }
    }
}
'''
    }
}