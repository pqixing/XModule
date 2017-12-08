package com.pqixing.modularization.utils
/**
 * Created by pqixing on 17-11-30.
 */

class FileUtils {
    /**
     * 拼接url
     * @param urls
     * @return
     */
    static String appendUrls(String[] urls) {
        StringBuilder newUrl = new StringBuilder()
        for (String url : urls) {
            newUrl.append(url).append(File.separator)
        }

        return newUrl.substring(0, newUrl.size() - 1)
    }

    /**
     * 输出文件
     * @param file
     * @param data
     */
    static String write(File file, String data) {
        if (file.exists()) file.delete()
        file.parentFile.mkdirs()
        BufferedOutputStream out = file.newOutputStream()
        out.write(data.getBytes())
        out.flush()
        out.close()
        return file.path
    }
}
