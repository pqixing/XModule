package com.dachen.creator.utils;

import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.openapi.externalSystem.model.ProjectSystemId;
import com.intellij.openapi.externalSystem.model.execution.ExternalSystemTaskExecutionSettings;
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode;
import com.intellij.openapi.externalSystem.task.TaskCallback;
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
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
        settings.setExecutionName("请勿更新构建代码,正在执行:" + tasks);
        settings.setTaskNames(tasks);
        settings.setExternalSystemIdString(GRADLE.getId());
        settings.setExternalProjectPath(project.getBasePath());
        ExternalSystemUtil.runTask(settings, DefaultRunExecutor.EXECUTOR_ID, project, GRADLE, callback, progressExecutionMode, activateToolWindowBeforeRun);
    }

    public static void addProperties(Project project){
        addProperties(project,getDefaultProperties());
    }
    /**
     * 添加配置文件
     * @param project
     * @param pair
     */
    public static void addProperties(Project project,List<Pair<String,Object>> pair){
        File file = new File(project.getBasePath(), ".modularization/hide.properties");
        FileUtils.delete(file);
        if(pair.size()==0)return;
        StringBuilder sb = new StringBuilder("#AUTO ADD BY MODULARIZATION \n");

        for (Pair<String,Object> p: pair) {
            Object temp = p.getSecond();
            String fix = temp instanceof CharSequence?"'":"";
            sb.append(p.getFirst()+" = "+fix+temp+fix+"\n");
        }
        FileUtils.write(sb.toString(),file);
    }

    public static List<Pair<String,Object>> getDefaultProperties(){
        ArrayList<Pair<String,Object>> parmas = new ArrayList<>();
        parmas.add(new Pair<>("dependentModel", "mavenOnly"));
        parmas.add(new Pair<>("updateBeforeSync", true));
        parmas.add(new Pair<>("silentLog", false));

        return parmas;
    }

    public static void clear(Project project){
        FileUtils.delete(new File(project.getBasePath(), ".modularization/hideInclude.kt"));
        FileUtils.delete(new File(project.getBasePath(), ".modularization/hide.properties"));
        FileUtils.delete(new File(project.getBasePath(), ".modularization/buildDir.lock"));
    }

    public static void addFocusInclude(Project project,String ... modules){
        File file = new File(project.getBasePath(), ".modularization/hideInclude.kt");
        StringBuilder sb = new StringBuilder("focusInclude = ");

        for (String module : modules) {
            sb.append(module).append("+");
        }
        FileUtils.write(sb.toString(),file);
        FileUtils.write("",new File(project.getBasePath(), ".modularization/buildDir.lock"));
    }

}
