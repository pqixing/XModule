package com.pqixing.modularization.helper;

import com.pqixing.modularization.impl.GExtHelper;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.invocation.Gradle;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Map;

public class JGroovyHelper implements IExtHelper {

    private static JGroovyHelper impls = new JGroovyHelper();

    public static JGroovyHelper getImpl(Class<?> tClass) {
        return getImpl();
    }

    public static JGroovyHelper getImpl() {
        return impls;
    }

    private GExtHelper helper = new GExtHelper();

    @Override
    public Object getExtValue(Project project, String key) {
        return helper.getExtValue(project, key);
    }

    @Override
    public Object setExtValue(Project project, String key, Object value) {
        return helper.setExtValue(project, key, value);
    }

    @Override
    public Object getExtValue(Gradle gradle, String key) {
        return helper.getExtValue(gradle, key);
    }

    @Override
    public Object setExtValue(Gradle gradle, String key, Object value) {
        return helper.setExtValue(gradle, key, value);
    }

    @Override
    public void setExtMethod(Project project, String method, Action action) {
        helper.setExtMethod(project, method, action);
    }

    @Override
    public void addRepositories(Project project, @NotNull List<String> dependMaven) {
        helper.addRepositories(project, dependMaven);
    }

    @Override
    public void addSourceDir(Project project, String dir) {
        helper.addSourceDir(project, dir);
    }

    @Override
    public void setApiSourceDir(Project project, String dir, String manifestPath) {
        helper.setApiSourceDir(project, dir, manifestPath);
    }

    @Override
    public void setMavenInfo(Project project, String groupId, String artifactId, String version, String name) {
        helper.setMavenInfo(project, groupId, artifactId, version, name);
    }

    @Override
    public Map<String, File> getAndroidOut(Project project, String type) {
        return helper.getAndroidOut(project, type);
    }

    @Override
    public void setApplicationId(@NotNull Project project, @NotNull String s) {
        helper.setApplicationId(project, s);
    }
}
