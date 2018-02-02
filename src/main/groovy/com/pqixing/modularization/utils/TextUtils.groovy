package com.pqixing.modularization.utils

import com.pqixing.modularization.Keys
import com.pqixing.modularization.git.GitConfig
import com.pqixing.modularization.wrapper.ProjectWrapper

class TextUtils {
    static int count = 0
    /**
     * 只保留数字和字母
     * @param str
     * @return
     */
    static String numOrLetter(String str) {
        return str?.trim().replaceAll("[^0-9a-zA-Z]", "")
    }

    static String getOnlyName() {
        return "${System.currentTimeMillis()}${count++}"
    }

    static String getBranchArtifactId(String name, ProjectWrapper wrapper) {
        return wrapper.master ? name : "$name-b-${wrapper.getExtends(GitConfig.class).branchName}"
    }

    /**
     * 集合转字符串
     * @param collection
     * @return
     */
    static String collection2Str(Collection collection) {
        if (isEmpty(collection)) return ""
        StringBuilder sb = new StringBuilder()
        collection.each { sb.append(it).append(Keys.SEPERATOR) }
        return sb.substring(0, sb.length() - Keys.SEPERATOR.length())
    }
}