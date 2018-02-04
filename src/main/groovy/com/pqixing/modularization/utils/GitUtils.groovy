package com.pqixing.modularization.utils

class GitUtils {
    /**
     * clone或者拷贝信息
     * @param path
     * @return
     */
    static boolean  cloneOrUpdate(String path){

    }
    /**
     * 获取git名称
     * @param url
     * @return
     */
    static String getNameFromUrl(String url){
        return url.substring(url.lastIndexOf("/") + 1).replace(".git", "")
    }
}