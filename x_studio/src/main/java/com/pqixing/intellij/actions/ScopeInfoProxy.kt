//package com.pqixing.intellij.actions
//
//import com.intellij.openapi.vcs.FilePath
//import com.intellij.openapi.vcs.actions.VcsContext
//import com.intellij.openapi.vcs.update.ActionInfo
//import com.intellij.openapi.vcs.update.ScopeInfo
//import com.pqixing.help.XmlHelper
//import com.pqixing.intellij.utils.UiUtils
//
//class ScopeInfoProxy(val info :ScopeInfo) :ScopeInfo {
//   override fun getRoots(context: VcsContext?, actionInfo: ActionInfo?): Array<FilePath> {
//       println("invoke -> ScopeInfoProxy getRoots")
//       UiUtils.addTask(0, Runnable {  XmlHelper.loadVersionFromNet(context?.project?.basePath) })
//       return info.getRoots(context, actionInfo)
//    }
//
//    override fun getScopeName(dataContext: VcsContext?, actionInfo: ActionInfo?): String {
//        return info.getScopeName(dataContext, actionInfo)
//    }
//
//    override fun filterExistsInVcs(): Boolean {
//        return info.filterExistsInVcs()
//    }
////