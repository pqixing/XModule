package com.pqixing.modularization.maven

import com.pqixing.modularization.base.BaseContainer
import com.pqixing.modularization.configs.GlobalConfig
import org.gradle.api.Project
/**
 * Created by pqixing on 17-12-7.
 */

class MavenType extends BaseContainer {
    boolean uploadEnable

    String pom_version
    String maven_url
    String artifactId
    String userName
    String password
    String groupName
    String uploadKey
    String updateDesc
    boolean focusUpload

    MavenType(String name) {
        super(name)
    }

    @Override
    void onCreate(Project project) {
        super.onCreate(project)
        artifactId = wrapper.artifactId
        userName = GlobalConfig.mavenUser
        password = GlobalConfig.mavenPassword
        groupName = GlobalConfig.groupName
        maven_url = GlobalConfig.preMavenUrl[name]
        uploadEnable = true
        focusUpload = false
    }

    @Override
    LinkedList<String> getOutFiles() {
        return null
    }
}
