package com.pqixing.modularization.models
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

//    void updateMeta(Project project) {
//        properties.each { map ->
//            String key = map.key
//            if ("class" != key && project.hasProperty(key)) try {
//                setProperty(key,project.ext.get(key))
//            } catch (Exception e) {
//                println("updateMeta error key = $key  exception = ${e.toString()}")
//            }
//        }
//    }

    abstract LinkedList<String> generatorFiles()

}
