package com.pqixing.modularization.utils

class Print {

    static String silentLog = "Y"

    static String ln(String str, Closure closure = null) {
        return l(str + "\n",false, closure)
    }
    static String lnf(String str, Closure closure = null) {
        return l(str + "\n",true, closure)
    }

    static String l(String str,boolean focusLog = false, Closure closure = null) {
        closure?.call(str)
        if ("Y" == silentLog&&!focusLog) return
        print(str)
        def newStr = "${new Date().toLocaleString()} --> $str"
        write(newStr)
        return str
    }
    static File outputFile

    static void write(String str) {
        if (outputFile == null) outputFile = new File(".modularization/print.log")
        //如果日志文件大于100M 删除
        if (outputFile.exists() && outputFile.length() >= 1024 * 1024 * 100) outputFile.delete()
        if (!outputFile.exists()) {
            outputFile.parentFile.mkdirs()
            outputFile.createNewFile()
        }
        outputFile.append(str)
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