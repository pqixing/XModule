package com.pqixing.modularization.git

import com.pqixing.modularization.Default
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.configs.BuildConfig
import com.pqixing.modularization.configs.GlobalConfig
import com.pqixing.modularization.utils.CheckUtils
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec

/**
 * Created by pqixing on 17-12-20.
 * 同步文档的任务
 */

public class DocSyncTask extends BaseTask {

    /**
     * 文档路径
     */
    String docDir
    /**
     * readme文件名称
     */
    String readme

    @Override
    void start() {
        GlobalConfig.docGits.each { map ->

        }
        if (!CheckUtils.isGit(GlobalConfig.docGits)) throw new RuntimeException("$docDir ")
        docDir = wrapper.getExtends(BuildConfig.class).docDir
    }

    @Override
    void runTask() {

    }

    @Override
    void end() {

    }

    void updateDocGitPath() {
        if (CheckUtils.isEmpty(docGitPath)) docGitPath = getModulePaths()
        if (CheckUtils.isEmpty(updateDesc)) updateDesc = "update doc"
    }

    void updateDocGit() {
        new File(project.buildDir, "updateGit").write("cd $docGitPath \ngit pull \necho update git,path :$docGitPath")
        project.task("${tempTaskName}${tempTaskCount++}", type: Exec) {
            if (winOs) {
                commandLine 'cmd', '/c', 'build\\updateGit'
            } else {
                commandLine 'sh', './build/updateGit'
            }
        }.execute()
    }

    void copyFile() {
        project.task("${tempTaskName}${tempTaskCount++}", type: Copy) {
            from CheckUtils.isEmpty(docFileDirs) ? "doc-${project.name}.md" : docFileDirs
            into "$docGitPath/readme/$project.name"
        }.execute()
    }

    void pushFile() {
        def txt = new StringBuilder().append("cd $docGitPath \n").append("git add ").append("$docGitPath/readme/$project.name/* \n")
                .append("git commit -m '$updateDesc' \n").append("git push \n")
        new File(project.buildDir, "pushGit").write(txt.toString())
        project.task("${tempTaskName}${tempTaskCount++}", type: Exec) {
            if (winOs) {
                commandLine 'cmd', '/c', 'build\\pushGit'
            } else {
                commandLine 'sh', './build/pushGit'
            }
        }.execute()
    }

    /**
     * @param dir
     * @param deep 层级，如果小于0 停止获取
     * @return
     */
    String getModulePaths() {
        File dir = project.rootDir.parentFile
        for (int i = 0; i < 4; i++) {
            File f = new File(dir, Default.docGitName)
            if (f.exists() && f.isDirectory()) return f.absolutePath
            dir = dir.parentFile
        }
        throw new RuntimeException("sync document fail ,can't find document git project")
    }
}
