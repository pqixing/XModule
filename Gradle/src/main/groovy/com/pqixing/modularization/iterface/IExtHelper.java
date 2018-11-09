package com.pqixing.modularization.iterface;

import org.gradle.api.Project;
import org.gradle.api.invocation.Gradle;

public interface IExtHelper {
    public Object getExtValue(Project project, String key);
    public Object getExtValue(Gradle gradle, String key);

    String getTestHelper();
}
