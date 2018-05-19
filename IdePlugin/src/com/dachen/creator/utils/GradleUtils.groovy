package com.dachen.creator.utils

import com.dachen.creator.Conts
import com.dachen.creator.GradleCallBack
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.model.execution.ExternalSystemTaskExecutionSettings
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode
import com.intellij.openapi.externalSystem.task.TaskCallback
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.NotNull

public class GradleUtils {

    public static ProjectSystemId GRADLE = new ProjectSystemId("GRADLE")


    public static void runTask(@NotNull Project project,
                               List<String> tasks,
                               GradleCallBack callback = null,
                               Map<String, Object> scriptParameters = [:],
                               ProgressExecutionMode progressExecutionMode = ProgressExecutionMode.IN_BACKGROUND_ASYNC,
                               boolean activateToolWindowBeforeRun = true) {
        ExternalSystemTaskExecutionSettings settings = new ExternalSystemTaskExecutionSettings()
        settings.setExecutionName("请勿更新代码,正在执行:" + tasks)
        settings.setTaskNames(tasks)
        settings.setExternalSystemIdString(GRADLE.getId())
        settings.setExternalProjectPath(project.getBasePath())
        def pro = ["$Conts.ENV_DEPENDENT_MODEL": "mavenOnly", "$Conts.ENV_FOCUS_INCLUDES": "empty", "$Conts.ENV_RUN_TYPE": "ide", "$Conts.ENV_UPDATE_BEFORE_SYNC": true, "$Conts.ENV_SILENT_LOG": false, "$Conts.ENV_BUILD_DIR": "ide"] + scriptParameters

        settings.setScriptParameters(getPro(pro))

        ExternalSystemUtil.runTask(settings, DefaultRunExecutor.EXECUTOR_ID, project, GRADLE, new TaskCallback() {
            @Override
            void onSuccess() {
                String[] records = FileUtils.readForOne(new File(project.getBasePath(), ".modularization/ide.record")).split("##")
                int l = records.length
                long logTime = l > 0 ? Long.parseLong(records[0]) : -1
                if (logTime - System.currentTimeMillis() > 1000 * 60) logTime = -1
                String id = l > 1 ? records[1].replace("\"", "") : ""
                String msg = l > 2 ? records[2] : ""

                callback?.onFinish(logTime, id, msg)
            }

            @Override
            void onFailure() {
                onSuccess()
            }
        }, progressExecutionMode, activateToolWindowBeforeRun);
    }

    public static String getPro(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder()
        map.each { m ->
//            String tag1 = (m.value instanceof String) ? "\"'" : ""
//            String tag2 = (m.value instanceof String) ? "'\"" : ""
//            sb.append(" -D${m.key}=$tag1$m.value$tag2 ")
            sb.append(" -D${m.key}=$m.value ")
        }
        return sb.toString()
    }
}
