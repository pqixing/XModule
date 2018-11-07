package com.pqixing.modularization.manager;

import com.pqixing.interfaces.ICredential;
import com.pqixing.modularization.base.BasePlugin;

import org.jetbrains.annotations.NotNull;

public class GitCredential implements ICredential {

    @NotNull
    @Override
    public String getUserName() {
        return BasePlugin.getPlugin(ManagerPlugin.class).getProjectInfo().getGitUserName();
    }

    @NotNull
    @Override
    public String getPassWord() {
        return BasePlugin.getPlugin(ManagerPlugin.class).getProjectInfo().getGitPassWord();
    }
}
