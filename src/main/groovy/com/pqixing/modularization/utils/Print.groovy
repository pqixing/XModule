package com.pqixing.modularization.utils

import com.pqixing.modularization.Default
import org.gradle.api.Project

class Print {
    static File outputFile;
    static StringBuilder outputData = new StringBuilder()

    static String ln(String str, Closure closure = null) {
        l(str + "\n", closure)
    }

    static String l(String str, Closure closure = null) {
        closure?.call(str)
        if (!NormalUtils.isEmpty(str)) {
            print(str)
            def newStr = "${new Date().toLocaleString()} --> $str"
            if (NormalUtils.isEmpty(outputFile)) outputData?.append(newStr)
            else outputFile.append(newStr)
        }
    }

    static void setOutputFile(File out) {
        if (NormalUtils.isEmpty(out)) return
        outputFile = out
        if (!outputFile.exists()) {
            outputFile.parentFile.mkdirs()
            outputFile.createNewFile()
        }

        outputFile.append(outputData.toString())
        outputData = null
    }


}