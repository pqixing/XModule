package com.pqixing.modularization.models

import com.pqixing.modularization.Default
import com.pqixing.modularization.utils.FileUtils
import org.gradle.api.Project
import com.pqixing.modularization.utils.NormalUtils

/**
 * Created by pqixing on 17-12-7.
 */

class MavenType extends BaseContainerExtension {
    Boolean uploadEnable

    String pom_version
    String maven_url
    String artifactId
    String userName
    String password
    String groupName
    String uploadKey

    Map<String, String> repoVersions

    MavenType(String name) {
        super(name)
        repoVersions = new HashMap<>()
    }

    String getMaven_url() {
        if (maven_url.startsWith("uri")) return maven_url
        return "'$maven_url'"
    }

    @Override
    void onCreate(Project project) {
        super.onCreate(project)
        artifactId = project.name
        userName = Default.maven_user
        password = Default.maven_password
        groupName = project.buildConfig.groupName
        if ("release" == name) maven_url = Default.maven_url_release
        if ("test" == name) maven_url = Default.maven_url_test
        if ("debug" == name) maven_url = Default.maven_url_debug
    }

    @Override
    LinkedList<String> generatorFiles() {
        def file = new File(project.buildConfig.cacheDir, "${name}maven.gradle")
        return [FileUtils.write(file, NormalUtils.parseString(mavenTxt, properties))]
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


    @Override
    public String toString() {
        return "MavenType{" +
                "name=" + name +
                ",uploadEnable=" + uploadEnable +
                ", pom_version='" + pom_version + '\'' +
                ", maven_url='" + maven_url + '\'' +
                ", artifactId='" + artifactId + '\'' +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", groupName='" + groupName + '\'' +
                ", uploadKey='" + uploadKey + '\'' +
                ", repoVersions=" + repoVersions +
                '}';
    }


}
