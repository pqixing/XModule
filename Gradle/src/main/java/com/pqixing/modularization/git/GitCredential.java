package com.pqixing.modularization.git;

import com.pqixing.interfaces.ICredential;

import org.jetbrains.annotations.NotNull;

public class GitCredential implements ICredential {
    @NotNull
    @Override
    public String getUserName() {
        return null;
    }

    @NotNull
    @Override
    public String getPassWord() {
        return null;
    }
}
