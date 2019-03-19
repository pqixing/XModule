package com.pqixing.modularization.manager

import com.pqixing.Tools
import com.pqixing.modularization.utils.ICredential


class GitCredential : ICredential {

    override fun getUserName(): String {
        return ManagerPlugin.getExtends().docRepoUser
    }

    override fun getPassWord(): String {
        return ManagerPlugin.getExtends().docRepoPsw
    }
}
