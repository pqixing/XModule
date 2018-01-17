package com.pqixing.modularization.tasks

import com.pqixing.modularization.Default
import com.pqixing.modularization.models.Dependencies
import com.pqixing.modularization.models.MavenType
import com.pqixing.modularization.models.ModuleConfig
import com.pqixing.modularization.utils.NormalUtils
import org.gradle.api.DefaultTask
/**
 * Created by pqixing on 17-12-20.
 * 基础的检测任务
 */

class BaseCheckTask extends DefaultTask {
    private MavenType maven

    BaseCheckTask() {
        group = Default.taskGroup
    }
    /**
     * 获取当前工程所有的依赖的最后版本
     */
    Collection<Dependencies.DpItem> getLastDependencies() {
        ModuleConfig config = project.moduleConfig
        maven = config.mavenType
        HashSet<Dependencies.DpItem> dpItems = new HashSet<Dependencies.DpItem>(config.dependModules.dependModules)
        dpItems.each { item ->
            loadDependenItems(item)
        }
        return dpItems
    }
    /**
     * 加载该item的依赖
     * @param item
     */
    void loadDependenItems(Dependencies.DpItem item) {
        if(item.version == "+"|| item.version.contains("last"))
            item.version = NormalUtils.parseLastVersion(NormalUtils.getMetaUrl(maven.maven_url,Default.groupName,item.moduleName))
//        String pomStr = FileUtils.readCachePom(maven,item.moduleName,item.version)
    }

}
