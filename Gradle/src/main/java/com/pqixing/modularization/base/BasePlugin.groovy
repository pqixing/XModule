package com.pqixing.modularization.base

import com.alibaba.fastjson.JSON
import com.pqixing.Tools
import com.pqixing.interfaces.ILog
import com.pqixing.modularization.FileNames
import com.pqixing.modularization.Keys
import com.pqixing.modularization.forOut.ProjectInfo
import com.pqixing.modularization.manager.GitCredential
import com.pqixing.modularization.utils.FileUtils
import com.pqixing.tools.CheckUtils
import com.pqixing.tools.TextUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

/**
 * Created by pqixing on 17-12-20.
 */

abstract class BasePlugin implements Plugin<Project>, IPlugin {
    Project project
    private static final HashMap<String, Plugin> pluginCache = new HashMap<>()

    private final HashMap<String, Task> tasks = new HashMap<>();

    private static ProjectInfo info;

    @Override
    void apply(Project project) {
        initProject(project)
    }

    public static final <T> T getPlugin(Class<T> pluginClass) {
        return pluginCache.get(pluginClass.name)
    }

    public final void setPlugin() {
        pluginCache.put(this.class.name, this)
    }

    private static void initTools(Project project) {
        if (!Tools.init) Tools.init(new ILog() {
            @Override
            void println(String l) {
                System.out.println(l)
            }
        }, project.rootDir.absolutePath, new GitCredential(project))
    }

    protected void initProject(Project project) {
        this.project = project
        setPlugin()
        initTools(project)
        createIgnoreFile()
        linkTask()?.each { onTaskCreate(it, BaseTask.task(project, it)) }
    }

    protected void onTaskCreate(Class taskClass, Task task) {

    }

    @Override
    <T> T getExtends(Class<T> tClass) {
        return project.extensions.getByType(tClass)
    }

    @Override
    ProjectInfo getProjectInfo() {
        if (info == null) {
            String infoJsonStr = getJsonFromEnv()
            if (CheckUtils.isEmpty(infoStr)) {
                try {
                    Class parseClass = new GroovyClassLoader().parseClass(new File(getRootDir(), FileNames.PROJECT_INFO))
                    infoStr = JSON.toJSONString(parseClass.newInstance())
                } catch (Exception e) {

                }
            }
            if (!CheckUtils.isEmpty(infoStr)) {
                try {
                    info = JSON.parseObject(infoJsonStr, ProjectInfo.class)
                } catch (Exception e) {
                }
            }

            if (info == null) info = new ProjectInfo()
        }
        return info
    }

    private String getJsonFromEnv() {
        String str = TextUtils.getSystemEnv("ProjectInfo")
        return str ?: new String(Base64.decoder.decode(infoStr.getBytes(Keys.CHARSET)), Keys.CHARSET)
    }

    @Override
    File getRootDir() {
        return project.rootDir
    }

    @Override
    File getProjectDir() {
        return project.projectDir
    }

    @Override
    File getBuildDir() {
        return project.buildDir
    }

    @Override
    File getCacheDir() {
        return new File(buildDir, FileNames.MODULARIZATION)
    }

    @Override
    Set<? extends Task> getTask(Class<? extends Task> taskClass) {
        return project.getTasksByName(BaseTask.getTaskName(taskClass))
    }

    void createIgnoreFile() {
        File ignoreFile = project.file(Keys.GIT_IGNORE)
        Set<String> defSets = ["build", Keys.FOCUS_GRADLE, FileNames.MODULARIZATION, "*.iml"] + ignoreFields

        String old = FileUtils.read(ignoreFile)
        old.eachLine { line ->
            defSets.remove(line.trim())
        }
        if (defSets.isEmpty())
            return

        StringBuilder txt = new StringBuilder(old)
        defSets.each {
            txt.append("\n$it")
        }
        FileUtils.write(ignoreFile, sb.toString())
    }
}
