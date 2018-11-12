package com.pqixing.modularization.iterface;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.invocation.Gradle;

public interface IExtHelper {
    public Object getExtValue(Project project, String key);
    public Object setExtValue(Project project, String key,String value);
    public Object getExtValue(Gradle gradle, String key);
    public Object setExtValue(Gradle gradle, String key,String value);

    void setExtMethod(Project project, String method, Action action);
}
