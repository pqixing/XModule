package com.pqixing.modularization.models

import com.pqixing.modularization.utils.FileUtils
import com.pqixing.modularization.utils.NormalUtils
import org.gradle.api.Project

/**
 * Created by pqixing on 17-12-25.
 */

class Dependencies extends BaseExtension {
    String baseGroup
    LinkedList<Inner> dependModules
    Project project
    Map<String, String> versions
    //强制使用本地库进行依赖处理，兼容旧版本进行本地化开发使用
    Boolean focusLocal

    static final String DEFAULT_GROUP = "default_group"
    Inner allInner


    Dependencies(Project project) {
        this.dependModules = new LinkedList<>()
        this.project = project
        if (project.hasProperty("focusLocal"))
            focusLocal = "Y" == project.ext.get("focusLocal")
    }
    /**
     * 配置通用的配置
     * @param closure
     */
    void defaultConfig(Closure closure) {
        if (NormalUtils.isEmpty(closure)) return
        if (NormalUtils.isEmpty(allInner)) allInner = new Inner()
        closure.delegate = allInner
        closure.setResolveStrategy(Closure.DELEGATE_ONLY)
        closure.call()
    }

    Inner add(String moduleName, Closure closure = null) {
        def inner = new Inner()
        inner.moduleName = moduleName
        if (closure != null) {
            closure.delegate = inner
            closure.setResolveStrategy(Closure.DELEGATE_ONLY)
            closure.call()
        }
        dependModules.add(inner)
        return inner
    }

    Inner addImpl(String moduleName, Closure closure = null) {
        Inner inner = add(moduleName, closure)
        inner.compileMode = "compile"
        return inner
    }

    Inner addExImpl(String moduleName, Closure closure = null) {
        Inner inner = add(moduleName, closure)
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

    String excludeString(Map<String,String> maps){
        StringBuilder sb = new StringBuilder()
        maps.each { map ->
            sb.append("$map.key : '${DEFAULT_GROUP == map.value ? baseGroup : map.value}',")
        }
        return sb.substring(0,sb.length()-1)
    }
    @Override
    LinkedList<String> generatorFiles() {
        StringBuilder sb = new StringBuilder("dependencies { \n")
        dependModules.each { model ->
            if(model.moduleName == project.name) return
            if (isLocal(model.local)) sb.append("    $model.compileMode ( project(':$model.moduleName')) ")
            else {
                String newGroup = NormalUtils.isEmpty(model.group) ? baseGroup : model.group
                String version = model.version
                if (NormalUtils.isEmpty(version)) {
                    version = versions?.containsKey(model.moduleName) ? versions[model.moduleName] : "+"
                }
                sb.append("    $model.compileMode  ('$newGroup:$model.moduleName:$version')  ")
            }
            sb.append("{ \n")
            model.excludes.each { sb.append("         exclude(${excludeString(it)})  \n") }
            sb.append("     } \n ")
        }
        sb.append("} \n")

        sb.append("\nconfigurations { \n")
        allInner?.excludes?.each { sb.append("    all*.exclude(${excludeString(it)})  \n") }
        sb.append(" }")

        return [FileUtils.write(new File(project.buildConfig.cacheDir, "dependencies.gradle"), sb.toString())];
    }

    boolean hasLocalCompile() {
        if (focusLocal) return true
        for (Inner i : dependModules) {
            if (i.local) return true
        }
        return false
    }

    void endConfig(Map<String, String> outConfig) {
        versions = outConfig
    }

    static class Inner {
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

        void excludeGroup(String[] groups) {
//            Print.ln(" exludeGroup. groups :$groups")
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

    }

}
