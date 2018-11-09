package com.pqixing.modularization.manager

import com.pqixing.interfaces.ICredential
import com.pqixing.modularization.base.BasePlugin

class GitCredential : ICredential {

    override fun getUserName(): String {
        return BasePlugin.getPlugin(ManagerPlugin::class.java)?.projectInfo?.gitUserName?:""
    }

    override fun getPassWord(): String {
        return BasePlugin.getPlugin(ManagerPlugin::class.java)?.projectInfo?.gitPassWord?:""
    }
}
