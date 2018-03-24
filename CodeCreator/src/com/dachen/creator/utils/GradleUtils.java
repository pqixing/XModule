package com.dachen.creator.utils;

import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.openapi.externalSystem.model.ProjectSystemId;
import com.intellij.openapi.externalSystem.model.execution.ExternalSystemTaskExecutionSettings;
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode;
import com.intellij.openapi.externalSystem.task.TaskCallback;
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GradleUtils {

    public static ProjectSystemId GRADLE = new ProjectSystemId("GRADLE");

    public static void runTask(@NotNull Project project, List<String> tasks, @Nullable TaskCallback callback) {
        runTask(project, tasks, callback, ProgressExecutionMode.IN_BACKGROUND_ASYNC);
    }

    public static void runTask(@NotNull Project project, List<String> tasks, @Nullable TaskCallback callback, @NotNull ProgressExecutionMode progressExecutionMode) {
        runTask(project, tasks, callback, progressExecutionMode, true);
    }

    public static void runTask(@NotNull Project project, List<String> tasks, @Nullable TaskCallback callback, @NotNull ProgressExecutionMode progressExecutionMode, boolean activateToolWindowBeforeRun) {
        ExternalSystemTaskExecutionSettings settings = new ExternalSystemTaskExecutionSettings();
        settings.setExecutionName("请勿更新或者构建代码,正在执行:" + tasks);
        settings.setTaskNames(tasks);
        settings.setExternalSystemIdString(GRADLE.getId());
        settings.setExternalProjectPath(project.getBasePath());
        ExternalSystemUtil.runTask(settings, DefaultRunExecutor.EXECUTOR_ID, project, GRADLE, callback, progressExecutionMode, activateToolWindowBeforeRun);
    }
}
