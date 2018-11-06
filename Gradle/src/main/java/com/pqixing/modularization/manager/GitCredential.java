package com.pqixing.modularization.manager;

import com.pqixing.interfaces.ICredential;

import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

public class GitCredential implements ICredential {
    Project project;

    public GitCredential(Project project) {
        this.project = project;
    }

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
