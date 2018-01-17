package com.pqixing.modularization.models

import com.pqixing.modularization.utils.FileUtils
import com.pqixing.modularization.utils.NormalUtils
import org.gradle.api.Project

/**
 * Created by pqixing on 17-12-25.
 */

class Dependencies extends BaseExtension {
    String baseGroup
    LinkedList<DpItem> dependModules
    Project project
    Map<String, String> versions
    //强制使用本地库进行依赖处理，兼容旧版本进行本地化开发使用
    Boolean focusLocal

    static final String DEFAULT_GROUP = "default_group"
    DpItem allInner


    Dependencies(Project project) {
        this.dependModules = new LinkedList<>()
        this.project = project
        versions = new HashMap<>()
        allInner = new DpItem()
        if (project.hasProperty("focusLocal"))
            focusLocal = "Y" == project.ext.get("focusLocal")
    }
    /**
     * 配置通用的配置
     * @param closure
     */
    void defaultConfig(Closure closure) {
        if (NormalUtils.isEmpty(closure)) return
        if (NormalUtils.isEmpty(allInner)) allInner = new DpItem()
        closure.delegate = allInner
        closure.setResolveStrategy(Closure.DELEGATE_ONLY)
        closure.call()
    }

    DpItem add(String moduleName, Closure closure = null) {
        def inner = new DpItem()
        inner.moduleName = moduleName
        if (closure != null) {
            closure.delegate = inner
            closure.setResolveStrategy(Closure.DELEGATE_ONLY)
            closure.call()
        }
        dependModules.add(inner)
        return inner
    }

    DpItem addImpl(String moduleName, Closure closure = null) {
        DpItem inner = add(moduleName, closure)
        inner.compileMode = "compile"
        return inner
    }

    DpItem addExImpl(String moduleName, Closure closure = null) {
        DpItem inner = add(moduleName, closure)
        inner.compileMode = "compile"
        inner.excludeGroup(DEFAULT_GROUP)
        return inner
    }

    List<String> getModuleNames() {
        List<String> names = new LinkedList<>()
        dependModules.each { names += it.moduleName }
        return names
    }

    private boolean isLocal(Boolean local) {
        if (!NormalUtils.isEmpty(local)) return local
        else if (!NormalUtils.isEmpty(allInner?.local)) return allInner?.local
        else if (!NormalUtils.isEmpty(focusLocal)) return focusLocal
        else return false
    }

    String excludeString(Map<String, String> maps) {
        StringBuilder sb = new StringBuilder()
        maps.each { map ->
            sb.append("$map.key : '${DEFAULT_GROUP == map.value ? baseGroup : map.value}',")
        }
        return sb.substring(0, sb.length() - 1)
    }

    @Override
    LinkedList<String> generatorFiles() {
        StringBuilder sb = new StringBuilder("dependencies { \n")
        //需要exclude的主线的包
        Set<String> masterExclude = new HashSet<>()
        dependModules.each { model ->
            if (model.moduleName == project.name) return
            if (isLocal(model.local)) sb.append("    $model.compileMode ( project(':$model.moduleName')) ")
            else {
                String newGroup = NormalUtils.isEmpty(model.group) ? baseGroup : model.group
                //获取当前模块在该分支的名称
                String moduleName = NormalUtils.getNameForBranch(project, model.moduleName)
                //如果该分支上,没有对应的版本号,则使用主线上的依赖包
                if (!versions.containsKey(moduleName)) moduleName = model.moduleName

                String version = model.version
                if (NormalUtils.isEmpty(version)) {
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
                model.version = version
                model.version = version
            }
            sb.append("{ \n")
            model.excludes.each { sb.append("         exclude(${excludeString(it)})  \n") }
            sb.append("     } \n ")
        }
        sb.append("} \n")
        masterExclude.each {allInner.excludeModule(it)}
        sb.append("\nconfigurations { \n")
        allInner.excludes.each { sb.append("    all*.exclude(${excludeString(it)})  \n") }
        sb.append("    all*.exclude(group : 'com.dachen.master',module : '${NormalUtils.collection2Str(masterExclude)},test') \n")
        sb.append(" }")

        return [FileUtils.write(new File(project.buildConfig.cacheDir, "dependencies.gradle"), sb.toString())];
    }
    /**
     * 添加对应的主线依赖到exclue中
     * @param masterExclude
     * @param model
     */
    void addMasterExclude(Set<String> masterExclude, String name,String version) {
        MavenType maven = project.moduleConfig.mavenType
        String lastLine
        FileUtils.readCachePom(maven, name,version).eachLine { line ->
            if (line.contains("com.dachen.master")) {
                masterExclude += lastLine.substring(lastLine.indexOf(">") + 1, lastLine.lastIndexOf("<")).split(",")
            }
            lastLine = line
        }
    }

    boolean hasLocalCompile() {
        if (focusLocal) return true
        for (DpItem i : dependModules) {
            if (i.local) return true
        }
        return false
    }

    void endConfig(Map<String, String> outConfig) {
        versions = outConfig
    }

    static class DpItem {
        String moduleName
        /**
         * 是否依赖本地工程，more依赖仓库工程
         */
        Boolean local
        /**
         * 依赖模式
         * runtimeOnly
         * compileOnly
         * implementation
         */
        String compileMode = "runtimeOnly"

        String group
        String version
        LinkedList<Map<String, String>> excludes = new LinkedList<>()
        /**
         * 依赖中的依赖树
         */
        Set<DpItem> dpItems = new HashSet<>()

        void excludeGroup(String[] groups) {
            groups.each {
                excludes += ["group": it]
            }
        }

        void excludeModule(String[] modules) {
            modules.each {
                excludes += ["module": it]
            }
        }

        void exclude(Map<String, String> exclude) {
            excludes += exclude
        }


        @Override
        String toString() {
            return "DpItem{" +
                    "moduleName='" + moduleName + '\'' +
                    ", local=" + local +
                    ", compileMode='" + compileMode + '\'' +
                    ", group='" + group + '\'' +
                    ", version='" + version + '\'' +
                    ", excludes=" + excludes +
                    ", dpItems=" + dpItems +
                    '}';
        }
    }

}
