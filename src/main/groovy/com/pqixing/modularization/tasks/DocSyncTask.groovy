package com.pqixing.modularization.tasks

import com.pqixing.modularization.Default
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
/**
 * Created by pqixing on 17-12-20.
 * 同步文档的任务
 */

public class DocSyncTask extends DefaultTask {
    String docGitPath;
    String docFileDirs
    String defaultDoc
    DocSyncTask() {
        group = Default.taskGroup
        docGitPath = Default.docGitName

        defaultDoc = "doc-${project.name}.md"
        docFileDirs = "document"
    }

    @TaskAction
    void updateDocGitPath() {
        if (!new File(docGitPath).exists()) return
        docGitPath = getModulePaths()
    }

    @TaskAction
    void updateDocGit() {

    }

    @TaskAction
    void copyFile() {

    }

    @TaskAction
    void pushFile() {
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
            if (f.exists() && f.isDirectory()) return f.path
            dir = dir.parentFile
        }
        throw new RuntimeException("sync document fail ,can't find document git project")
    }
}
