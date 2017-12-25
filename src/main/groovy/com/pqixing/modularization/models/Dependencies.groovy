package com.pqixing.modularization.models

import org.gradle.api.Project
import com.pqixing.modularization.utils.NormalUtils
import com.pqixing.modularization.utils.FileUtils
/**
 * Created by pqixing on 17-12-25.
 */

class Dependencies extends BaseExtension {
    String baseGroup
    LinkedList<Inner> dependModules
    Project project
    Map<String, String> versions


    Dependencies(Project project) {
        this.dependModules = new LinkedList<>()
        this.project = project
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
       Inner inner =add(moduleName,closure)
        inner.compileMode = "implementation"
        return inner
    }

    @Override
    public LinkedList<String> generatorFiles() {
        StringBuilder sb = new StringBuilder("dependencies { \n")
        dependModules.each { model ->
            if (model.local) sb.append("    $model.compileMode  project(':$model.moduleName') \n")
            else {
                String newGroup = NormalUtils.isEmpty(model.group) ? baseGroup : model.group
                String version = versions?.containsKey(model.moduleName) ? versions[model.moduleName] : "+"
                sb.append("    $model.compileMode  '$newGroup:$model.moduleName:$version' \n")
            }
        }
        return [FileUtils.write(new File(project.buildConfig.cacheDir, "dependencies.gradle"), sb.append("}").toString())];
    }

    boolean hasLocalCompile() {
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
    }
}
