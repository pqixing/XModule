package com.pqixing.modularization.models

import com.pqixing.modularization.utils.FileUtils
import com.pqixing.modularization.utils.NormalUtils
import org.gradle.api.Project

/**
 * Created by pqixing on 17-12-25.
 */

class Dependencies extends BaseExtension {
    String baseGroup
    LinkedList<Inner> dependencies
    Project project
    Map<String, String> versions


    Dependencies(Project project) {
        this.dependencies = new LinkedList<>()
        this.project = project
    }

    void include(String moduleName, Closure closure) {
        def inner = new Inner()
        inner.moduleName = moduleName
        if (closure != null) {
            closure.delegate = inner
            closure.setResolveStrategy(Closure.DELEGATE_ONLY)
            closure.call()
        }
        dependencies.add(inner)
    }

    void includeImpl(String moduleName, Closure closure) {
        def inner = new Inner()
        inner.moduleName = moduleName
        inner.compileMode = implementation
        if (closure != null) {
            closure.delegate = inner
            closure.setResolveStrategy(Closure.DELEGATE_ONLY)
            closure.call()
        }
        dependencies.add(inner)
    }

    @Override
    public LinkedList<String> generatorFiles() {
        StringBuilder sb = new StringBuilder("dependencies { \n")
        dependencies.each { model ->
            if (model.local) sb.append("    $model.compileMode  project(':$model.moduleName') \n")
            else {
                String newGroup = NormalUtils.isEmpty(model.group) ? baseGroup : model.group
                String version = versions?.containsKey(model.moduleName) ? versions[model.moduleName] : "+"
                sb.append("    $model.compileMode  $model.group:$model.moduleName:$version \n")
            }
        }
        return [FileUtils.write(new File(project.buildConfig.cacheDir, "dependencies.gradle"), sb.append("}").toString())];
    }

    boolean hasLocalCompile() {
        for (Inner i : dependencies) {
            if (i.local) return true
        }
        return false
    }

    void endConfig(Map<String, String> outConfig) {
        versions = outConfig
    }

    private class Inner {
        String moduleName;
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
