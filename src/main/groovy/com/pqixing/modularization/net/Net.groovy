package com.pqixing.modularization.net

import com.pqixing.modularization.configs.BuildConfig
import com.pqixing.modularization.configs.GlobalConfig
import com.pqixing.modularization.maven.MavenType
import com.pqixing.modularization.utils.CheckUtils
import com.pqixing.modularization.utils.FileUtils
import com.pqixing.modularization.utils.Print

class Net {

    static String get(String url) {
        return get(url,false)
    }
    /**
     * 封装网络请求
     * @param url
     * @param useCache
     * @return
     */
    static String get(String url,boolean useCache) {
        String netResult = ""
        if(GlobalConfig.offlineMode) {
            netResult = FileUtils.readCache(url)
            if (CheckUtils.isEmpty(netResult)) throw new RuntimeException("offlineMode invail there is no cache for url :$url")
            return netResult
        }
        if(useCache) netResult = FileUtils.readCache(url)
        if(!CheckUtils.isEmpty(netResult)) return netResult
        netResult = requestNet(url)
        FileUtils.saveCache(url,netResult)
        return netResult
    }
    /**
     * 读取网络
     * @param url
     * @return
     */
    private static String requestNet(String url){
        try {
            def conn = new URL(url).openConnection()
            conn.connectTimeout = BuildConfig.timOut//4秒超时
           return conn.inputStream.text
        } catch (Exception e) {
            Print.ln("requestNet -> $url e : ${e.toString()}")
          return   ""
        }
    }

    /**
     *获取指定版本的pom信息
     * @param envUrl
     * @param group
     * @param moduleName
     * @return
     * http://192.168.3.7:9527/nexus/content/repositories/androidtest/com/dachen/android/router/0.1.7/router-0.1.7.pom
     */
    static String getPomStr(MavenType maven, String moduleName, String version) {
        BuildConfig buildConfig = maven.wrapper.getExtends(BuildConfig)
        StringBuilder pomUrl = new StringBuilder(maven.maven_url)
        if (!maven.maven_url.endsWith("/")) pomUrl.append("/")
        pomUrl.append(buildConfig.groupName.replace(".", "/"))
                .append("/$moduleName/$version/$moduleName-${version}.pom")
        return get(pomUrl.toString(),true)
    }
}