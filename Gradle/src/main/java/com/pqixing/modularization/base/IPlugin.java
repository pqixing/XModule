package com.pqixing.modularization.base;

import com.pqixing.modularization.forOut.ProjectInfo;

import org.gradle.api.Task;

import java.io.File;
import java.util.List;
import java.util.Set;

public interface IPlugin {
    Set<String> getIgnoreFields();
    File getCacheDir();
    File getBuildDir();
    File getProjectDir();
    File getRootDir();
    Set<? extends Task> getTask(Class<? extends Task> taskClass);
    List<Class<? extends Task>> linkTask();

    ProjectInfo getProjectInfo();

    <T> T getExtends(Class<T> tClass);

}
