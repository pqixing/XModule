package com.pqixing.aspect.reflect

class MainDexListFilter {
    var enable = true
    var mainDexTask = "transformClassesWithMultidexlist"
    //忽略的文件,支持正则
    var excludes = mutableListOf<String>()
}