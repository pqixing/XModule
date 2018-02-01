package com.pqixing.modularization.git

import com.pqixing.modularization.base.BaseExtension
import org.gradle.api.Project

/**
 * Created by pqixing on 17-12-7.
 * 基础信息编译生成的
 */
class GitConfig extends BaseExtension {
    final String branchName
    final String revisionNum
    final String lastLog


    GitConfig(Project project) {
        super(project)
        branchName = "git rev-parse --abbrev-ref HEAD".execute(null, project.projectDir).text.trim()
        revisionNum = "git rev-parse HEAD".execute(null, project.projectDir).text.trim()
        lastLog = "git branch -vv".execute(null, project.projectDir).text.find {
            it.startsWith("*")
        }
    }

    List<String> log(int num = 5) {
        List<String> logs = new LinkedList<>()
        StringBuilder item = null
        "git log -$num".execute(null, project.projectDir).text.eachLine { line ->
            if (line.startsWith("commit ")) {
                if (item != null) logs += item.toString()
                item = new StringBuilder()
            }
            item.append("$line\n")
        }
        logs += item.toString()
        return logs
    }

    @Override
    LinkedList<String> getOutFiles() {
        return null
    }
}
