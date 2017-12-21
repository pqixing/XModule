package com.pqixing.modularization.utils

class Print {
    static File outputFile;
    static StringBuilder outputData = new StringBuilder()

    static String ln(String str, Closure closure = null) {
        return l(str + "\n", closure)
    }

    static String l(String str, Closure closure = null) {
        closure?.call(str)
        if (!NormalUtils.isEmpty(str)) {
            print(str)
            def newStr = "${new Date().toLocaleString()} --> $str"
            checkFile(outputFile)
            if (NormalUtils.isEmpty(outputFile)) outputData?.append(newStr)
            else outputFile.append(newStr)
        }
        return str
    }

    static void checkFile(File f) {
        if (f != null && !f.exists()) {
            f.parentFile.mkdirs()
            f.createNewFile()
        }
    }

    static void setOutputFile(File out) {
        if (NormalUtils.isEmpty(out)) return
        outputFile = out
        checkFile(outputFile)

        outputFile.append(outputData.toString())
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