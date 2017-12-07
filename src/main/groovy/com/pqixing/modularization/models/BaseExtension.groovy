package com.pqixing.modularization.models

import org.gradle.api.Project

/**
 * Created by pqixing on 17-12-7.
 */

abstract class BaseExtension {

/**
 * 配置解析
 * @param closure
 */
    void configure(Closure closure) {
        closure.delegate = this
        closure.setResolveStrategy(Closure.DELEGATE_ONLY)
        closure(this)
    }

    void updateMeta(Project project) {
        metaPropertyValues.each { meta ->
            String key = meta.name.toString()
            if ("class" != key && project.hasProperty(key)) try {
                meta.value = project.ext.get(key)
            } catch (Exception e) {
                println("updateMeta error key = $key value = ${project.ext.get(key)} exception = ${e.toString()}")
            }
        }
    }
}
