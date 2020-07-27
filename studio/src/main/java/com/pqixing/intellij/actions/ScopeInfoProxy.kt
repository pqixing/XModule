package com.pqixing.intellij.actions

import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.actions.VcsContext
import com.intellij.openapi.vcs.update.ActionInfo
import com.intellij.openapi.vcs.update.ScopeInfo

class ScopeInfoProxy(val info :ScopeInfo) :ScopeInfo {
   override fun getRoots(context: VcsContext?, actionInfo: ActionInfo?): Array<FilePath> {
       println("invoke -> ScopeInfoProxy getRoots")
        return info.getRoots(context, actionInfo)
    }

    override fun getScopeName(dataContext: VcsContext?, actionInfo: ActionInfo?): String {
        return info.getScopeName(dataContext, actionInfo)
    }

    override fun filterExistsInVcs(): Boolean {
        return info.filterExistsInVcs()
    }
}