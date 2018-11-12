package com.pqixing.modularization.impl

import com.pqixing.modularization.iterface.IExtHelper
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle;

public class GExtHelper implements IExtHelper {

    public Object getExtValue(Project project, String key) {
        try {
            return project.ext."$key"
        } catch (Exception e) {
        }
        return null
    }

    @Override
    Object setExtValue(Project project, String key, String value) {
        try {
             project.ext."$key" = value
        } catch (Exception e) {
        }
    }

    public Object getExtValue(Gradle gradle, String key) {
        try {
            return gradle.ext."$key"
        } catch (Exception e) {
        }
        return null
    }

    @Override
    Object setExtValue(Gradle gradle, String key, String value) {
        try {
            gradle.ext."$key" = value
        } catch (Exception e) {
        }
    }

    @Override
    void setExtMethod(Project project, String method, Action action) {
        project.ext."$method" = { action?.execute(it) }
    }
}
