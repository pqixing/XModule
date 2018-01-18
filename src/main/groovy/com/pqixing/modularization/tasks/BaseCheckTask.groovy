package com.pqixing.modularization.tasks

import com.alibaba.fastjson.JSON
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
    boolean listExclude
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
        ModuleConfig m = project.moduleConfig
        FileUtils.write(new File(m.buildConfig.outDir,"innerDependency.txt"),JSON.toJSONString(dpItems,true))
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
        loadItemInfo(pomStr, item)
        //解析pom 文件
        NormalUtils.parseListXmlByKey(pomStr, "dependency").each { dep ->
            if (groupName != NormalUtils.parseXmlByKey(dep, "groupId")) return
            Dependencies.DpItem inner = new Dependencies.DpItem()

            loadDependencyInfo(inner, dep)
            item.dpItems += inner
            //继续解析子item
            loadDependencyItems(inner)
        }
    }

    void loadDependencyInfo(Dependencies.DpItem inner, String dep) {
        inner.group = NormalUtils.parseXmlByKey(dep, "groupId")
        inner.version = NormalUtils.parseXmlByKey(dep, "version")
        inner.compileMode = NormalUtils.parseXmlByKey(dep, "scope")
        inner.moduleName = NormalUtils.parseXmlByKey(dep, "artifactId")
        if (listExclude) NormalUtils.parseListXmlByKey(dep, "exclusion").each { exc ->
            inner.exclude(["group" : NormalUtils.parseListXmlByKey(exc, "groupId"),
                           "module": NormalUtils.parseListXmlByKey(exc, "artifactId")])
        }
    }

    String loadItemInfo(String pomStr, Dependencies.DpItem item) {
        String name = NormalUtils.parseXmlByKey(pomStr, "name").trim()
        int index = name.indexOf("##")
        item.lastUpdate = index < 0 ? "0" : name.substring(0, index)
        item.updateDesc = index < 0 ? name : name.substring(index + 2, name.length())
    }
    /**
     * 依赖保存
     */
    static class LevelItem{
        String name
        Dependencies.DpItem item
        int level =0
    }

    /**
     * 按照层级关系对依赖进行排序
     * @param dpItems
     * @param curLevel
     * @param container
     */
    void sortByLevel(Collection<Dependencies.DpItem> dpItems, int curLevel, Map<String, LevelItem> container) {
        dpItems.each { item ->
            String moduleName = item.moduleName.split("-b-")[0]
            LevelItem level = container.get(moduleName)
            if (level == null) {
                level = new LevelItem()
                level.name = moduleName
                level.item = item
                container.put(moduleName,level)
            }
            if(item.moduleName.contains("-b-")) {
                level.name = item.moduleName
                level.item = item
            }
            level.level = Math.max(curLevel, level.level)
            sortByLevel(item.dpItems,curLevel+1,container)
        }
    }
    /**
     * 模块 xxx 依赖更新关系图
     * 需要更新   依赖层级    Master Moduriaztion        依赖库(-b-*为分支名后缀)
     *                    1.0/2018-12-11      1.0/2018-12-11      dcnet
     * 对比当前item和指定仓库的区别
     * @param item
     * @param compareMavenUrl
     * @param isCheck
     * @return
     */
    String compareLastDiff(Dependencies.DpItem item, MavenType compareMaven, int dpLevel, Closure<Boolean> isCheck) {
        Dependencies.DpItem compareItem = new Dependencies.DpItem()
        compareItem.moduleName = item.moduleName.split("-b-")[0]
        compareItem.version = NormalUtils.parseLastVersion(NormalUtils.getMetaUrl(compareMaven.maven_url, Default.groupName, compareItem.moduleName))
        loadItemInfo(FileUtils.readCachePom(compareMaven, compareItem.moduleName, compareItem.version), compareItem)
        String checkStr = isCheck.call(item, compareItem) ? "√" : " "
        return "$checkStr     $dpLevel      $compareItem.version/${new Date(compareItem.lastUpdate).toString()}    " +
                " $item.version/${new Date(item.lastUpdate).toString()}         $compareItem.moduleName   \n"
    }
}
