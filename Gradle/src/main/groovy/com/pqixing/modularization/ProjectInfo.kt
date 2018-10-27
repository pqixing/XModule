package com.pqixing.modularization


class ProjectInfo {
    //需要导入的工程，使用 ,或这+号分割
    var include = null

    //快速导入该工程所有依赖的内部工程
    var dpsInclude = null

    //git 的用户名称
    var gitUserName = null

    //git 密码
    var gitPassWord = null


    //#note:git操作目标范畴
//#note:include:只操作当前导入的代码
//#note:all:影响本地所有存在的代码(仅包含配置在default.xml的工程)
    var target = "all"

    //当前分支名称
    var curBranch = "master"


    //#note:是否拦截依赖缺少的异常,默认true
//#note:true:当有依赖模块缺少时，抛出异常，方便查找问题
//#note:false:不拦截错误，方便代码导入AS，但是缺少依赖构建过程出现的类缺失异常问题很难定位
    var abortDependentLose = true


    //#note:需要不被控制的git名称
    var excludeGit = ""


    //#note:是否使用日志
//#note:true:打印日志
    var log = true


    //#note:模块依赖方式，默认: mavenOnly
//#note:mavenOnly:只使用maven仓库进行依赖，不关联本地代码
//#note:mavenFirst:优先使用maven仓库进行依赖，当仓库不存在依赖时，关联本地代码依赖
//#note:localFirst:优先使用本地导入模块代码，没有导入则使用maven仓库
//#note:localOnly:只使用本地导入的模块，禁用ｍａｖｅｎ仓库
    var dependentModel = "localFirst"

    //指定一个版本号的map文件，则会优先使用该文件中的信息进行版本号管理
    var versionFile = ""


}

var Document1 = ""
var Document2 = ""
var Document3 = ""