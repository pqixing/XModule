package com.pqixing.modularization.models

import com.pqixing.modularization.Default
import com.pqixing.modularization.utils.FileUtils
import org.gradle.api.Project
import com.pqixing.modularization.utils.NormalUtils

/**
 * Created by pqixing on 17-12-7.
 */

class MavenType extends BaseContainerExtension {
    boolean uploadEnable = false

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
        StringBuilder sb = new StringBuilder()
        sb.append(repoTxt)
        if (uploadEnable && ("release" != name || Default.uploadKey == uploadKey)) {
            sb.append(mavenTxt)
            project.task("upload$project.name") {
                group = Default.taskGroup
                doLast {
                    project.uploadArchives.execute()
                }
            }
        }
        sb.append(this.toString())
        sb.append("\n maven_url = $maven_url")
        def file = new File(project.buildConfig.cacheDir, "maven.gradle")
        return [FileUtils.write(file, NormalUtils.parseString(sb.toString(), properties))]
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
            repository(url:"#{maven_url}"){
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

/**
 * 获取依赖仓库的信息
 * @return
 */
    static String getRepoTxt() {
        return '''
repositories {
     maven {
         url "#{maven_url}"
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
