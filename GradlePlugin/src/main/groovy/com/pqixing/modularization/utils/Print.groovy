package com.pqixing.modularization.utils

import com.pqixing.modularization.Keys
import com.pqixing.modularization.common.BuildConfig
import com.pqixing.modularization.common.GlobalConfig

class Print {

    static String ln(String str, Closure closure = null) {
        return l(str + "\n", false, closure)
    }

    static String lnf(String str, Closure closure = null) {
        return l(str + "\n", true, closure)
    }

    static String lne(String str = "", Exception e) {
        lnf("$str Exception -> ${e.toString()}")
    }

    static String l(String str, boolean focusLog = false, Closure closure = null) {
        closure?.call(str)
        if (GlobalConfig.silentLog &&  !focusLog) return
        print(str)
        def newStr = "${new Date().toLocaleString()} --> $str"
        write(newStr)
        return str
    }
    static File outputFile

    static void write(String str) {
        if (outputFile == null) outputFile = new File("$BuildConfig.dirName/print.log")
        //如果日志文件大于10M 删除
        if (outputFile.exists() && outputFile.length() >= 1024 * 1024 * 10) outputFile.delete()
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
        if (deep <= 0 || p instanceof String || CheckUtils.isEmpty(p?.properties)) return p?.toString()
        StringBuilder sb = new StringBuilder().append("{ ")
        p.properties.toSpreadMap().each { map ->
            String proStr = CheckUtils.isEmpty(map?.value?.properties) ? map.value : getProStr(map.value, deep - 1)
            sb.append("$map.key :$proStr  ,")
        }
        return sb.append(" }").toString()
    }
    /**
     * 打印Maven上传日志
     * @param msg
     */
    static void lnm(String msg) {
        File recordFile = new File(BuildConfig.mavenRecordFile)
        if (!recordFile.exists()) recordFile.parentFile.mkdirs()
        recordFile.append("${new Date().toLocaleString()} -> $msg\n")
        lnf(msg)
    }
    /**
     * 输出给Ide使用，每次只输出一条记录
     * @param str
     */
    static void lnIde(String str){
        String id = TextUtils.getSystemEnv(Keys.ENV_RUN_ID)?:"task"
        FileUtils.write(new File(BuildConfig.dirName,"ide.record"),System.currentTimeMillis()+Keys.SEPERATOR+id+Keys.SEPERATOR+str)
        ln(str)
    }
}