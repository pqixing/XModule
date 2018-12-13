package com.pqixing.modularization.manager

import com.pqixing.interfaces.ICredential
import com.pqixing.modularization.base.BasePlugin

class GitCredential : ICredential {

    override fun getUserName(): String {
        return ManagerPlugin.getManagerPlugin().projectInfo.gitUserName
    }

    override fun getPassWord(): String {
        return ManagerPlugin.getManagerPlugin().projectInfo.gitPassWord
    }
}
