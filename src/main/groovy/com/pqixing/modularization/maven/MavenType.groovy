package com.pqixing.modularization.maven

import com.pqixing.modularization.Keys
import com.pqixing.modularization.ModuleConfig
import com.pqixing.modularization.base.BaseContainer
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.configs.GlobalConfig
import com.pqixing.modularization.utils.CheckUtils
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

    private void mergerData() {
        MavenType defType = wrapper.getExtends(ModuleConfig).mavenTypes.getByName(Keys.DEFAULT)
        if (CheckUtils.isEmpty(pom_version)) pom_version = defType.pom_version
        if (CheckUtils.isEmpty(uploadEnable)) uploadEnable = defType.uploadEnable
        if (CheckUtils.isEmpty(updateDesc)) updateDesc = defType.updateDesc
        if (CheckUtils.isEmpty(uploadKey)) uploadKey = defType.uploadKey
        focusUpload |= defType.focusUpload
    }

    @Override
    LinkedList<String> getOutFiles() {
        mergerData()
        ToMavenTask task =  BaseTask.task(project, ToMavenTask.class)
        task.dependsOn "assembleRelease"
        task.mavenInfo = this
        return []
    }
}
