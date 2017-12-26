package com.pqixing.modularization.models

import com.pqixing.modularization.utils.FileUtils
import com.pqixing.modularization.utils.NormalUtils
import com.pqixing.modularization.utils.Print
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
    boolean focusLocal

    static final String DEFAULT_GROUP = "default_group"

    Dependencies(Project project) {
        this.dependModules = new LinkedList<>()
        this.project = project
        focusLocal = project.hasProperty("focusLocal") && "Y" == project.ext.get("focusLocal")
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
        inner.compileMode = "implementation"
        return inner
    }

    Inner addExImpl(String moduleName, Closure closure = null) {
        Inner inner = add(moduleName, closure)
        inner.compileMode = "implementation"
        inner.excludeGroup(DEFAULT_GROUP)
        return inner
    }

    @Override
    LinkedList<String> generatorFiles() {
        StringBuilder sb = new StringBuilder("dependencies { \n")
        dependModules.each { model ->
            if (model.local) sb.append("    $model.compileMode  project(':$model.moduleName') ")
            else {
                String newGroup = NormalUtils.isEmpty(model.group) ? baseGroup : model.group
                String version = model.version
                if (NormalUtils.isEmpty(version)) {
                    version = versions?.containsKey(model.moduleName) ? versions[model.moduleName] : "+"
                }
                sb.append("    $model.compileMode  ('$newGroup:$model.moduleName:$version') ")
            }
            sb.append(" { \n")
            model.excludes.each {
                it.each { map ->
                    String value = DEFAULT_GROUP == map.value ? baseGroup : map.value
                    sb.append("         exclude $map.key : '$value'  \n")
                }
            }
            sb.append("     } \n")
            Print.ln(" model.excludes :$model.excludes")
        }
        return [FileUtils.write(new File(project.buildConfig.cacheDir, "dependencies.gradle"), sb.append("}").toString())];
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
        boolean local = false
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
            Print.ln(" exludeGroup. groups :$groups")
            groups.each {
                excludes += ["group": it]
            }
        }

        void excludeModule(String[] modules) {
            modules.each {
                excludes += ["module": it]
            }
        }

    }
}
