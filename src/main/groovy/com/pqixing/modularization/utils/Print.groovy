package com.pqixing.modularization.utils

import org.gradle.api.Project

class Print {
    static File outputFile;
    static StringBuilder outputData = new StringBuilder()
    static String silentLog = "N"

    static String ln(String str, Closure closure = null) {
        return l(str + "\n", closure)
    }

    static String l(String str, Closure closure = null) {
        closure?.call(str)
        if ("Y" != silentLog) print(str)

        def newStr = "${new Date().toLocaleString()} --> $str"
        outputData?.append(newStr)
        outputFile?.parentFile?.mkdirs()
        outputFile?.append(newStr)
        return str
    }

    static void init(Project project) {
        if (project.hasProperty("silentLog")) silentLog = project.ext.get("silentLog")

        File f = new File(project.rootProject.buildDir, "${project.name}.log")
        if (f.length() > 1024 * 1024 * 1024) f.delete()
        if (!f.exists()) {
            f.parentFile.mkdirs()
            f.createNewFile()
        }

        outputFile = f
        if (outputData != null && outputData.size() > 0) outputFile.append(outputData.toString())
        outputData = null
    }

    static String lnPro(Object p) {
        return ln(getProStr(p))
    }

    static String getProStr(Object p, int deep = 2) {
        if (deep <= 0 || p instanceof String || NormalUtils.isEmpty(p?.properties)) return p?.toString()
        StringBuilder sb = new StringBuilder().append("{ ")
        p.properties.toSpreadMap().each { map ->
            String proStr = NormalUtils.isEmpty(map?.value?.properties) ? map.value : getProStr(map.value, deep - 1)
            sb.append("$map.key :$proStr  ,")
        }
        return sb.append(" }").toString()
    }

}