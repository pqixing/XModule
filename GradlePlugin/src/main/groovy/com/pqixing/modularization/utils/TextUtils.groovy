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
        return str?.trim()?.replaceAll("[^0-9a-zA-Z]", "")
    }

    static String getOnlyName() {
        return "${System.currentTimeMillis()}${count++}"
    }

    static String getBranchArtifactId(String name, ProjectWrapper wrapper) {
        return wrapper.master ? name : "$name$Keys.BRANCH_TAG${wrapper.getExtends(GitConfig.class).branchName}"
    }

    static String firstUp(String source) {
        if (source == null) return ""
        return "${source.substring(0, 1).toUpperCase()}${source.substring(1)}"
    }
    static String className(String source){
        return firstUp(numOrLetter(source))
    }
    /**
     * 比较版本号的大小,前者大则返回一个正数,后者大返回一个负数,相等则返回0
     * @param version1
     * @param version2
     * @return
     */
    public static int compareVersion(String version1, String version2) {
        if (version1 == null || version2 == null)  return 0

        String[] versionArray1 = version1.trim().split("\\.")//注意此处为正则匹配，不能用"."；
        String[] versionArray2 = version2.trim().split("\\.")
        int idx = 0;
        int minLength = Math.min(versionArray1.length, versionArray2.length);//取最小长度值
        int diff;
        while (idx < minLength
                && (diff = versionArray1[idx].length() - versionArray2[idx].length()) == 0//先比较长度
                && (diff = versionArray1[idx] <=> versionArray2[idx]) == 0) {//再比较字符
            ++idx;
        }
        //如果已经分出大小，则直接返回，如果未分出大小，则再比较位数，有子版本的为大；
        diff = (diff != 0) ? diff : versionArray1.length - versionArray2.length;
        return diff
    }
    /**
     * 集合转字符串
     * @param collection
     * @return
     */
    static String collection2Str(Collection collection) {
        if (CheckUtils.isEmpty(collection)) return ""
        StringBuilder sb = new StringBuilder()
        collection.each { sb.append(it).append(Keys.SEPERATOR) }
        return sb.substring(0, sb.length() - Keys.SEPERATOR.length())
    }
    /**
     * 根据深度获取tab空格
     * @param deep
     * @return
     */
    static String getTab(int deep) {
        StringBuilder sb = new StringBuilder()
        for (int i = 0; i < deep; i++) {
            sb.append(Keys.TAB)
        }
        return sb.toString()
    }

    static String getUrl(String pkg){
        return pkg.replace(".","/")
    }
}