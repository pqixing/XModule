package com.pqixing.modularization.utils

class TextUtils {
    /**
     * 只保留数字和字母
     * @param str
     * @return
     */
    static String numOrLetter(String str){
        return str?.trim().replaceAll("[^0-9a-zA-Z]","")
    }
}