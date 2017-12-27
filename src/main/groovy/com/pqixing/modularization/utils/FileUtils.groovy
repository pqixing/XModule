package com.pqixing.modularization.utils

import com.pqixing.modularization.Default
import org.gradle.api.Project

/**
 * Created by pqixing on 17-11-30.
 */

class FileUtils {
    /**
     * 拼接url
     * @param urls
     * @return
     */
    static String appendUrls(String[] urls) {
        StringBuilder newUrl = new StringBuilder()
        for (String url : urls) {
            newUrl.append(url).append("/")
        }

        return newUrl.substring(0, newUrl.size() - 1)
    }

    /**
     * 输出文件
     * @param file
     * @param data
     */
    static String write(File file, String data) {
        if (file.exists()) file.delete()
        file.parentFile.mkdirs()
        BufferedOutputStream out = file.newOutputStream()
        out.write(data.getBytes())
        out.flush()
        out.close()
        return file.path
    }

    /**
     * 输出依赖关系
     * @param project
     * @return
     */
    static void writeDependency(Project project, File outFile) {
        project.task("dependency", type: org.gradle.api.tasks.diagnostics.DependencyReportTask) {
            group = Default.taskGroup
            outputFile = outFile
            doLast {
                def strList = new LinkedList<String>()
                outputFile.eachLine {
                    if (it.startsWith("No dependencies")) {
                        strList.removeLast()
                        strList.removeLast()
                    } else {
                        strList.add(it + "\n")
                    }
                }
                FileUtils.write(outputFile, strList.toString())
            }
        }.execute()
    }
}
