package com.pqixing.modularization.impl

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle
import org.jetbrains.annotations.NotNull

/**
 * @see com.pqixing.modularization.helper.IExtHelper
 */
interface IGExtHelper {
    public Object getExtValue(Project project, String key);
    public Object setExtValue(Project project, String key,Object value);
    public Object getExtValue(Gradle gradle, String key);
    public Object setExtValue(Gradle gradle, String key,Object value);

    void setExtMethod(Project project, String method, Action action);

    void addRepositories(Project project,@NotNull List<String> dependMaven);
    void addSourceDir(Project project,String dir);
    void setApiSourceDir(Project project, String dir,String manifestPath);
    void setMavenInfo(Project project
                      ,String groupId
                      ,String artifactId
                      ,String version
                      ,String name);

    Map<String,File> getAndroidOut(Project project, String type);

    void setApplicationId(@NotNull Project project, @NotNull String s);
}