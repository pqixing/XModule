package com.pqixing.modularization.utils

import com.pqixing.modularization.Keys
import com.pqixing.modularization.common.BuildConfig
import com.pqixing.modularization.common.GlobalConfig

/**
 * Created by pqixing on 17-11-30.
 */

class FileUtils {
    private static HashMap<String, Long> cacheTime = new HashMap<>()


    /**
     * 根据包名获取路径
     * @param dir
     * @param pkgName
     * @return
     */
    static File getFileForClass(String dir, String fullName) {
        return new File(dir, "${fullName.replace(".", "/")}.java")
    }
    /**
     * 如果文件不存在,先创建文件
     * @param file
     * @return
     */
    static boolean createIfNotExist(File file) {
        if (file.exists()) return false
        file.parentFile.mkdirs()
        file.createNewFile()
        return true
    }

    static String readCache(String url) {
        File fileName = new File(BuildConfig.netCacheDir, TextUtils.numOrLetter(url))
        if (fileName.exists()) return fileName.text
    }
    /**
     * 缓存是否有效
     * @param url
     * @return
     */
    static boolean cacheVail(String url) {
        File fileName = new File(BuildConfig.netCacheDir, TextUtils.numOrLetter(url))
        long lastTime = Math.max(fileName.lastModified(), cacheTime.get(url)?.toLong() ?: 0L)
        boolean vail = fileName.exists() && (System.currentTimeMillis() - lastTime) <= GlobalConfig.netCacheTime
        return vail
    }

    static File saveCache(String url, String cache) {
        File fileName = new File(BuildConfig.netCacheDir, TextUtils.numOrLetter(url))
        cacheTime.put(url, System.currentTimeMillis())
        write(fileName, cache)
        return fileName
    }

    /**
     * 拼接url
     * @param urls
     * @return
     */
    static String urls(String[] urls) {
        StringBuilder newUrl = new StringBuilder()
        for (String url : urls) {
            newUrl.append(url).append("/")
        }

        return newUrl.substring(0, newUrl.size() - 1)
    }

    static Properties readMaps(File file) {
        Properties p = new Properties()
        if (file.exists()) p.load(file.newInputStream())
        return p
    }

    static void saveMaps(Properties maps, File file) {
        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()
        }
        maps.store(file.newPrintWriter(), Keys.CHARSET)
    }

    static String read(String file) {
        return read(new File(file))
    }
    /**
     * 读取文件
     * @param file
     * @return
     */
    static String read(File file) {
        if (file.exists() && file.isFile()) return file.text
        return ""
    }
    /**
     * 输出文件
     * @param file
     * @param data
     */
    static String write(File file, String data) {
        if (file.exists() && file.text == data) return file.path

        if (file.exists()) file.delete()
        file.parentFile.mkdirs()
        Writer out = file.newWriter(Keys.CHARSET)
        out.write(data)
        out.flush()
        out.close()
        return file.path
    }
    /**
     * 字符串转为输入流
     * @param coverStr
     * @return
     */
    static InputStream coverStream(String coverStr) {
        return new File(write(new File(BuildConfig.rootOutDir, "cover/${TextUtils.onlyName}"), coverStr)).newInputStream()
    }
}
