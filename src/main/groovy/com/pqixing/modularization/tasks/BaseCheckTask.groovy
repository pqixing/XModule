package com.pqixing.modularization.tasks

import com.pqixing.modularization.Default
import com.pqixing.modularization.models.Dependencies
import com.pqixing.modularization.models.MavenType
import com.pqixing.modularization.models.ModuleConfig
import com.pqixing.modularization.utils.FileUtils
import com.pqixing.modularization.utils.NormalUtils
import com.pqixing.modularization.utils.Print
import org.gradle.api.DefaultTask
/**
 * Created by pqixing on 17-12-20.
 * 基础的检测任务
 */

class BaseCheckTask extends DefaultTask {
    MavenType maven
    int runtime = 0
    BaseCheckTask() {
        group = Default.taskGroup
    }
    /**
     * 获取当前工程所有的依赖的最后版本
     */
    Collection<Dependencies.DpItem> getLastDependencies() {
        runtime = 0
        ModuleConfig config = project.moduleConfig
        maven = config.mavenType
        HashSet<Dependencies.DpItem> dpItems = new HashSet<Dependencies.DpItem>(config.dependModules.dependModules)
        dpItems.each { item ->
            loadDependencyItems(item)
        }
        Print.ln("loadDependencyItems runtime  = $runtime")
        return dpItems
    }
    /**
     * 加载该item的依赖
     * @param item
     */
    void loadDependencyItems(Dependencies.DpItem item) {
        runtime++
        if(item ==null ) return
        if (item.version == "+" || item.version.contains("last"))
            item.version = NormalUtils.parseLastVersion(NormalUtils.getMetaUrl(maven.maven_url, Default.groupName, item.moduleName))
        String pomStr = FileUtils.readCachePom(maven, item.moduleName, item.version)
        String groupName = "${Default.groupName}.android"
        String name =NormalUtils.parseXmlByKey(pomStr,"name").trim()
        int index = name.indexOf("##")
        item.lastUpdate = index<0? "0":name.substring(0,index)
        //解析pom 文件
        NormalUtils.parseListXmlByKey(pomStr, "dependency").each { dep ->
            if (groupName != NormalUtils.parseXmlByKey(dep, "groupId")) return
            Dependencies.DpItem inner = new Dependencies.DpItem()
            inner.group = groupName
            inner.version = NormalUtils.parseXmlByKey(dep, "version")
            inner.compileMode = NormalUtils.parseXmlByKey(dep, "scope")
            inner.moduleName = NormalUtils.parseXmlByKey(dep, "artifactId")
            NormalUtils.parseListXmlByKey(dep, "exclusion").each { exc ->
                inner.exclude(["group" : NormalUtils.parseListXmlByKey(exc, "groupId"),
                               "module": NormalUtils.parseListXmlByKey(exc, "artifactId")])
            }
            item.dpItems += inner

            //继续解析子item
            loadDependencyItems(inner)
        }
    }

}
