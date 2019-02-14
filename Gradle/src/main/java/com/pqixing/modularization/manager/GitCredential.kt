package com.pqixing.modularization.manager

import com.pqixing.modularization.utils.ICredential


class GitCredential : ICredential {

    override fun getUserName(): String {
        return ManagerPlugin.getManagerPlugin().projectInfo.gitUserName
    }

    override fun getPassWord(): String {
        return ManagerPlugin.getManagerPlugin().projectInfo.gitPassWord
    }
}
