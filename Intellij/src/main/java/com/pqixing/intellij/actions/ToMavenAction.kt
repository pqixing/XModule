package com.pqixing.intellij.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ModuleRootManager
import com.pqixing.intellij.ui.ToMavenDialog
import java.util.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.List
import kotlin.collections.set

class ToMavenAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val dialog = ToMavenDialog()
        dialog.pack()
        dialog.isVisible = true
        val module = e.getData(DataKey.create<Module>("module"))
//        val dpsSort = loadAndSortDps(module)
    }
//
//    private fun loadAndSortDps(module: Module?): List<String> {
//
//    }
//
//    /**
//     * 获取依赖的模块名称
//     *
//     * @param module
//     * @return
//     */
//    private fun getDps(module: Module): List<String> {
//        var m:Module? = module
//        while (m!=null){
//
//        }
//        val levels = HashMap<String,Int>()
//        loadModuleByLevel(0, module, levels)
//        val newMaps = HashMap()
//
//        var lastDept = 0
//        for (entry in levels.entrySet()) {
//            val value = entry.value
//            lastDept = Math.max(lastDept, value)
//            var list = newMaps.get(value)
//            if (list == null) {
//                list = ArrayList<String>()
//                newMaps.put(value, list)
//            }
//            list!!.add(entry.key)
//        }
//        val sortModles = ArrayList<String>()
//        for (i in lastDept downTo 0) {
//            val strings = newMaps.get(i)
//            if (strings != null) sortModles.addAll(strings!!)
//        }
//        return sortModles
//    }
//
//    private fun loadModuleByLevel(dept: Int, module: Module, levels: HashMap<String, Int>) {
//        val modules = ModuleRootManager.getInstance(module).dependencies
//        if (modules == null || modules.size == 0) return
//        for (m in modules) {
//            if (m == null) continue
//            val name = m.name
//            val oldDept = if (levels.containsKey(name)) levels[name] else 0
//            levels[name] = Math.max(oldDept, dept)
//        }
//
//        for (m in modules) {
//            loadModuleByLevel(dept + 1, m, levels)
//        }
//    }
}
