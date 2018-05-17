package com.pqixing.modularization.base

import com.pqixing.modularization.Keys
import com.pqixing.modularization.utils.Print
import com.pqixing.modularization.wrapper.ProjectWrapper
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

/**
 * Created by pqixing on 17-12-20.
 * 同步文档的任务
 */

public abstract class BaseTask extends DefaultTask {
    ProjectWrapper wrapper

    BaseTask() {
        group = Keys.GROUP_TASK
        wrapper = ProjectWrapper.with(project)
    }

    @TaskAction
    public void run() {
        long startTime = System.currentTimeMillis()
        Print.ln("start task $project.name:$name -> ${new Date(startTime).toLocaleString()}")
        start()
        runTask()
        end()
        long endTime = System.currentTimeMillis()
        Print.ln("end task $project.name:$name count :  ${endTime - startTime}  :-> ${new Date(endTime).toLocaleString()}")
    }

    static <T extends DefaultTask> T task(Project project, Class<T> tClass) {
        return project.task(getTaskName(tClass), type: tClass)
    }

    static String getTaskName(Class tClass){
        return tClass.simpleName.replace("Task","")
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
