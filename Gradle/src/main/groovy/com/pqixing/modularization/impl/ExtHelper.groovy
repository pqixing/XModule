package com.pqixing.modularization.impl

import com.pqixing.modularization.iterface.IExtHelper
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle;

public class ExtHelper implements IExtHelper {

    public Object getExtValue(Project project, String key) {
        try {
            return project.ext."$key"
        } catch (Exception e) {
        }
        return null
    }

    public Object getExtValue(Gradle gradle, String key) {
        try {
            return gradle.ext."$key"
        } catch (Exception e) {
        }
        return null
    }

    @Override
    String getTestHelper() {
        return "TestHelper"
    }
}
