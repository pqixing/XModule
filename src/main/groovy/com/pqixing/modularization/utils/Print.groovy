package com.pqixing.modularization.utils

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
            checkFile(outputFile)
            if (NormalUtils.isEmpty(outputFile)) outputData?.append(newStr)
            else outputFile.append(newStr)
        }
    }
    static void checkFile(File f){
        if (f!=null&&!f.exists()) {
            f.parentFile.mkdirs()
            f.createNewFile()
        }
    }

    static void setOutputFile(File out) {
        if (NormalUtils.isEmpty(out)) return
        outputFile = out


        outputFile.append(outputData.toString())
        outputData = null
    }


}