package com.pqixing.modularization.manager

import com.pqixing.Tools
import com.pqixing.modularization.utils.ICredential


class GitCredential : ICredential {

    override fun getUserName(): String {
        Tools.println("GitCredential -> getUserName ${ManagerPlugin.getExtends().docRepoUser}")
        return ManagerPlugin.getExtends().docRepoUser
    }

    override fun getPassWord(): String {
        Tools.println("GitCredential -> getPassWord ${ManagerPlugin.getExtends().docRepoPsw}")
        return ManagerPlugin.getExtends().docRepoPsw
    }
}
