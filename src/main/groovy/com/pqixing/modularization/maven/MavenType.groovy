package com.pqixing.modularization.maven

import com.pqixing.modularization.Keys
import com.pqixing.modularization.ModuleConfig
import com.pqixing.modularization.base.BaseContainer
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.configs.GlobalConfig
import com.pqixing.modularization.utils.TextUtils
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
        if (TextUtils.isEmpty(pom_version)) m.pom_version = defType.pom_version
        if (TextUtils.isEmpty(uploadEnable)) m.uploadEnable = defType.uploadEnable
        if (TextUtils.isEmpty(updateDesc)) m.updateDesc = defType.updateDesc
        if (TextUtils.isEmpty(uploadKey)) m.uploadKey = defType.uploadKey
        focusUpload |= defType.focusUpload
    }

    @Override
    LinkedList<String> getOutFiles() {
        mergerData()
        BaseTask.task(project, ToMavenTask.class).mavenInfo = this
        return []
    }
}
