package com.pqixing.tools


object TextUtils {
    var count = 0

    val onlyName: String
        get() = "${System.currentTimeMillis()}${count++}"

    /**
     * 只保留数字和字母
     * @param str
     * @return
     */
    fun numOrLetter(str: String): String {
        return str.trim().replace("[^0-9a-zA-Z]".toRegex(), "")
    }


    fun firstUp(source: String): String {
        return if (CheckUtils.isEmpty(source)) "" else "${source.substring(0, 1).toUpperCase()}${source.substring(1)}"
    }

    fun className(source: String): String {
        return firstUp(numOrLetter(source))
    }

    /**
     * 比较版本号的大小,前者大则返回一个正数,后者大返回一个负数,相等则返回0
     * @param version1
     * @param version2
     * @return
     */
    fun compareVersion(version1: String?, version2: String?): Int {
        if (version1 == null || version2 == null) return 0

        val versionArray1 = version1.trim().split("\\.".toRegex())//注意此处为正则匹配，不能用"."；
        val versionArray2 = version2.trim().split("\\.".toRegex())
        var idx = 0
        val minLength = Math.min(versionArray1.size, versionArray2.size)//取最小长度值
        var diff: Int
        while (idx < minLength) {
            //先比较长度,长度不一致时，较长的数字为大，不考虑0开头的数字
            diff = versionArray1[idx].length - versionArray2[idx].length
            if (diff != 0) return diff

            diff = versionArray1[idx].compareTo(versionArray2[idx])
            if (diff != 0) return diff

            ++idx
        }
        //如果已经分出大小，则直接返回，如果未分出大小，则再比较位数，有子版本的为大；
        return versionArray1.size - versionArray2.size
    }

    /**
     * 集合转字符串
     * @param collection
     * @return
     */
    fun collection2Str(collection: Collection<*>): String {
        if (CheckUtils.isEmpty(collection)) return ""
        val sb = StringBuilder()
        collection.forEach { sb.append(it).append("##") }
        return sb.substring(0, sb.length - 2)
    }
    fun isVersionCode(str: String?) = str?.matches(Regex("\\d+(\\.\\d+){2,}")) ?: false
    /**
     * 是不是基础版本，etc 1.0
     */
    fun isBaseVersion(version: String?): Boolean = version?.matches(Regex("\\d*\\.\\d+")) ?: false
    /**
     * 根据深度获取tab空格
     * @param deep
     * @return
     */
    fun getTab(deep: Int): String {
        val sb = StringBuilder()
        for (i in 0 until deep) {
            sb.append(" ")
        }
        return sb.toString()
    }

    fun getUrl(pkg: String): String {
        return pkg.replace(".", "/")
    }

    /**
     * 根据模块名称获取对应的api模块名称
     */
    fun getApiModuleName(module: String): String {
        if (checkIfApiModule(module)) return module
        return "${module}__api"
    }

    fun checkIfApiModule(module: String) = module.endsWith("__api")

    /**
     * 从Api模块名称中，解析出实际模块名称
     */
    fun getModuleFromApi(module: String): String {
        if (!checkIfApiModule(module)) return module
        return module.substring(0, module.lastIndexOf("__"))
    }

    fun getModuleName(module: String, api: Boolean): String = if (api) getApiModuleName(module) else getModuleFromApi(module)

    /**
     * 检查是否是对应的Api模块
     */
    fun isTargetApiModule(module: String, apiModule: String) = checkIfApiModule(apiModule) && module == getModuleFromApi(apiModule)

    /**
     * 获取系统变量
     * @param key
     * @return
     */
    fun getSystemEnv(key: String): String? {
        var v: String? = null
        try {
            v = System.getProperty(key)
        } catch (e: Exception) {
        }

        return v
    }

    /**
     * 拼接url
     * @param urls
     * @return
     */
    fun append(s: String, urls: Array<String>): String {
        val newUrl = StringBuilder()
        for (url in urls) {
            if (CheckUtils.isEmpty(url))
                continue
            newUrl.append(url).append(s)
        }
        if (newUrl.isEmpty()) return ""
        return newUrl.substring(0, newUrl.length - s.length)
    }

    fun removeMark(s: String?): String {
        return s?.replace("\"|'".toRegex(), "") ?: ""
    }

    fun removeLineAndMark(s: String): String {
        return removeMark(s.replace("\r|\n".toRegex(), ""))
    }
}