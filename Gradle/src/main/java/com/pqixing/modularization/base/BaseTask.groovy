package com.pqixing.modularization.base

import com.pqixing.modularization.Keys
import com.pqixing.modularization.utils.Print
import com.pqixing.modularization.wrapper.ProjectWrapper
import com.pqixing.tools.TextUtils
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

/**
 * Created by pqixing on 17-12-20.
 * 同步文档的任务
 */

public abstract class BaseTask extends DefaultTask {
    public static final String minVersion = "ide:2.1"
    ProjectWrapper wrapper
    String ideVersion

    BaseTask() {
        TextUtils.getSystemEnv(Keys.ENV_RUN_TYPE)
        group = Keys.GROUP_TASK
        wrapper = ProjectWrapper.with(project)
    }

    void checkIdeVersion() {
        String ideVersion = TextUtils.getSystemEnv(Keys.ENV_RUN_TYPE)
        if (ideVersion == null || !ideVersion.contains("ide") || ideVersion.length() > minVersion.length() || ideVersion >= minVersion) return
        String errorMsg = "Ide Plugin Update=current plugin version is too low,please update $ideVersion"
        Print.lnIde(errorMsg)
        throw new GradleException(errorMsg)

    }

    @TaskAction
    public void run() {
        checkIdeVersion()
        long startTime = System.currentTimeMillis()
        Print.ln("start task $project.name:$name -> ${new Date(startTime).toLocaleString()}")
        start()
        runTask()
        end()
        long endTime = System.currentTimeMillis()
        Print.ln("end task $project.name:$name count :  ${endTime - startTime}  :-> ${new Date(endTime).toLocaleString()}")
    }

    static <T extends DefaultTask> T task(Project project, Class<T> tClass) {
        return taskByName(project, getTaskName(tClass), tClass)
    }

    static <T extends DefaultTask> T taskByName(Project project, String taskName, Class<T> tClass) {
        return project.task(taskName, type: tClass)
    }

    static String getTaskName(Class tClass) {
        return tClass.simpleName.replace("Task", "")
    }

    boolean excuteTask(Project project, String taskName) {
        try {
            project."$taskName".execute()
            return true
        } catch (Exception e) {
            Print.lne("excuteTask", e)
        }
        return false
    }

    abstract void start()

    abstract void runTask()

    abstract void end()
}
