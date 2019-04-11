package com.pqixing.regester.reflect

import com.android.build.api.transform.Transform
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.pipeline.TransformTask
import com.android.build.gradle.internal.transforms.D8MainDexListTransform
import com.android.build.gradle.internal.transforms.MultiDexTransform
import com.pqixing.regester.utils.FieldUtls
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.nio.file.Path

/**
 * 注册注解生成器
 */
class MainDexReflectPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val isApp = project.plugins.hasPlugin(AppPlugin::class.java)
        //only application module needs this plugin to generate register code
        val anExtends = MainDexListFilter()
        project.extensions.add("MainDexFilter", anExtends)
        if (!isApp) return
        project.afterEvaluate { p ->
            if (anExtends.enable) p.tasks.forEach { task ->
                if (task.name.startsWith(anExtends.mainDexTask) && task is TransformTask) {
                    System.out.println("filterMainDex -> " + anExtends.excludes)
                    task.doLast { findMainDexList(task.transform, anExtends) }
                }
            }
        }
    }

    private fun findMainDexList(t: Transform?, anExtends: MainDexListFilter) {
        t ?: return
        if (t is MultiDexTransform) {
            val value = FieldUtls.getValue(t, MultiDexTransform::class.java, "mainDexListFile")
            if (value is File) filterMainDex(value, anExtends.excludes)
        } else if (t is D8MainDexListTransform) {
            val value = FieldUtls.getValue(t, D8MainDexListTransform::class.java, "outputMainDexList")
            if (value is Path) filterMainDex(value.toFile(), anExtends.excludes)
        } else {
            val value = FieldUtls.getValue(t, t.javaClass, "mainDexListFile")
            if (value is File) filterMainDex(value, anExtends.excludes)
        }
    }

    /**
     * 根据配置,过滤文件
     */
    private fun filterMainDex(value: File, excludes: MutableList<String>) {
        System.out.println("filterMainDex -> " + value.absolutePath)
        if (excludes.isEmpty() || !value.exists()) return//如果为空,不过滤
        val mathch = excludes.map { Regex(it) }
        //没有在过滤范围之内的值,保留
        val resultValues = value.readLines().filter { l -> mathch.find { f -> l.matches(f) } == null }
        value.writeText(resultValues.joinToString { it + "\n" })
    }
}
