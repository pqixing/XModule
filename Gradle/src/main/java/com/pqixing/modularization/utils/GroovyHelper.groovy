package com.pqixing.modularization.utils

import org.gradle.api.Project
import org.gradle.api.invocation.Gradle;

public class GroovyHelper {

    public static Object getExtValue(Project project,String key) {
        try {
            return project.ext."$key"
        } catch (Exception e) {
        }
        return null
    }

    public static Object getExtValue(Gradle gradle, String key) {
        try {
            return gradle.ext."$key"
        } catch (Exception e) {
        }
        return null
    }
}
