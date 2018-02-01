package com.pqixing.modularization.dependent

import com.pqixing.modularization.Default
import com.pqixing.modularization.Keys
import com.pqixing.modularization.base.BaseExtension
import com.pqixing.modularization.configs.BuildConfig
import com.pqixing.modularization.configs.GlobalConfig
import com.pqixing.modularization.maven.MavenType
import com.pqixing.modularization.models.ModuleConfig
import com.pqixing.modularization.utils.CheckUtils
import com.pqixing.modularization.utils.FileUtils
import com.pqixing.modularization.utils.XmlUtils
import com.pqixing.modularization.wrapper.MetadataWrapper
import org.gradle.api.Project

/**
 * Created by pqixing on 17-12-25.
 */

class Dependencies extends BaseExtension {
    boolean hasLocalModule = false
    LinkedList<Map<String, String>> allExcludes
    LinkedList<Module> modules

    File versionFile
    Properties versionMaps
    MavenType mavenType
    /**
     * 给全部依赖库添加
     * @param exclude
     */
    void allExclude(Map<String, String> exclude) {
        allExcludes += exclude
    }

    Dependencies(Project project) {
        super(project)
        modules = new LinkedList<>()
        allExcludes = new LinkedList<>()
    }


    Module add(String moduleName, Closure closure = null) {
        def inner = new Module()
        inner.moduleName = moduleName
        if (closure != null) {
            closure.delegate = inner
            closure.setResolveStrategy(Closure.DELEGATE_ONLY)
            closure.call()
        }
        modules += inner
        return inner
    }

    Module addImpl(String moduleName, Closure closure = null) {
        Module inner = add(moduleName, closure)
        inner.compileMode = "implementation"
        return inner
    }

    List<String> getModuleNames() {
        List<String> names = new LinkedList<>()
        modules.each { names += it.moduleName }
        return names
    }

    String excludeString(Map<String, String> maps) {
        StringBuilder sb = new StringBuilder()
        maps.each { map ->
            sb.append("$map.key : '$map.value',")
        }
        return sb.substring(0, sb.length() - 1)
    }

    /**
     * 获取该aar在仓库的最后一个一个版本
     * @param artifactId
     * @return
     */
    String getLastVersion(String group, String artifactId) {
        String timeStamp = "$artifactId-stamp"
        String version = versionMaps.getProperty(artifactId)
        //一分钟秒内,不更新相同的组件版本,避免不停的爬取相同的接口
        if (System.currentTimeMillis() - (versionMaps.getProperty(timeStamp)?.toLong() ?: 0L) >= 1000 * 60) {
            String release = MetadataWrapper.create(mavenType.maven_url, group, artifactId).release
            if (!CheckUtils.isEmpty(release)) {
                version = release
                versionMaps.put(timeStamp, System.currentTimeMillis().toString())
            }
        }
        return CheckUtils.isEmpty(version) ? "+" : version


    }

    void initVersionMap() {
        mavenType = wrapper.getExtends(ModuleConfig.class).mavenType
        versionFile = new File(BuildConfig.versionDir, "$mavenType.name/$Keys.FILE_VERSION")
        versionMaps = FileUtils.readMaps(versionFile)
    }

    void saveVersionMap() {
        versionMaps.store(versionFile.newOutputStream(), Keys.CHARSET)
        versionMaps.clear()
    }

    /**
     * 进行本地依赖
     * @param module
     * @return
     */
    boolean onLocal(StringBuilder sb, Module module) {

    }
    /**
     * 进行本地依赖
     * @param module
     * @return
     */
    boolean onMaven(StringBuilder sb, Module module) {

    }

    @Override
    LinkedList<String> getOutFiles() {
        initVersionMap()

        StringBuilder sb = new StringBuilder("dependencies { \n")
        modules.each { model ->
            if (model.moduleName == project.name) return
            switch (GlobalConfig.dependenModel) {

            //只依赖本地工程
                case "localOnly":
                    onLocal(model)
                    break
            //优先依赖本地工程
                case "localFirst":
                    if (onLocal(model)) break
                    break

            //优先仓库版本
                case "mavenFirst":
                    break
            //只依赖仓库版本
                case "mavenOnly":
                default:
                    break
            }

            if (isLocal(model.local)) sb.append("    $model.compileMode ( project(':$model.moduleName')) ")
            else {
                String newGroup = XmlUtils.isEmpty(model.group) ? baseGroup : model.group
                //获取当前模块在该分支的名称
                String moduleName = XmlUtils.getNameForBranch(project, model.moduleName)
                //如果该分支上,没有对应的版本号,则使用主线上的依赖包
                if (!versions.containsKey(moduleName)) moduleName = model.moduleName

                String version = model.version
                if (XmlUtils.isEmpty(version)) {
                    version = versions.containsKey(moduleName) ? versions[moduleName] : "+"
                }
                sb.append("    $model.compileMode  ('$newGroup:$moduleName:$version')  ")
                //如果当前依赖是分支,则,解析该依赖中是否还包含了别的分支依赖,并且把当前模块的主线依赖添加exclude
                if (moduleName.contains("-b-")) {
                    masterExclude.add(model.moduleName)
                    addMasterExclude(masterExclude, moduleName, version)
                }
                //更新bean里面的信息
                model.moduleName = moduleName
                model.group = newGroup
                model.version = version
                String lastUpdateKey = "${moduleName}-stamp"
                model.lastUpdate = versions[lastUpdateKey]
            }
            sb.append("{ \n")
            model.excludes.each { sb.append("         exclude(${excludeString(it)})  \n") }
            sb.append("     } \n ")
        }
        sb.append("} \n")
        masterExclude.each { allInner.excludeModule(it) }
        sb.append("\nconfigurations { \n")
        allInner.allExcludes.each { sb.append("    all*.exclude(${excludeString(it)})  \n") }
        sb.append("    all*.exclude(group : 'com.dachen.master',module : '${XmlUtils.collection2Str(masterExclude)},test') \n")
        sb.append(" }")

        saveVersionMap()
        return [FileUtils.write(new File(project.buildConfig.cacheDir, "dependencies.gradle"), sb.toString())];
    }
    /**
     * 添加对应的主线依赖到exclue中
     * @param masterExclude
     * @param model
     */
    void addMasterExclude(Set<String> masterExclude, String name, String version) {
        MavenType maven = project.moduleConfig.mavenType
        String targetGroup = "${Default.groupName}.master"
        def pomStr = FileUtils.readCachePom(maven, name, version)
        XmlUtils.parseListXmlByKey(pomStr, "dependency").each { dep ->
            if (targetGroup != XmlUtils.parseXmlByKey(dep, "groupId")) return
            masterExclude += XmlUtils.parseXmlByKey(dep, "artifactId").split(",")
        }
    }

}
