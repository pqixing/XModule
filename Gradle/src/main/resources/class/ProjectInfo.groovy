package com.pqixing.modularization.gradle

/**
 * 工程配置文件
 * 所有模块，以：加模块名称 extc include = ":dComm"+":dcNet"
 */
class ProjectInfo{
    /** 需要导入的工程，使用 ,或这+号分割 **/
    String include = null

    /**  填入不需要导入的工程名称 */
    String exclude = ""

    /** 快速导入该工程所有依赖的内部工程*/
    String dpsInclude = null
    /** 快速导入该工程所有依赖的内部工程*/
    String dpsExclude = null

    /** 代码存放的根目录**/
    String codeRootDir = "../"

    /**  git 的用户名称*/
    def gitUserName = null

    /**  git 密码 */
    def gitPassWord = null

    /**
     * git操作目标范畴
     include : 只操作当前导入的代码
     all :
     影响本地所有存在的代码(仅包含配置在default.xml的工程)
     **/
    def target = "all"

    /**  当前分支名称*/
    def curBranch = "master"

    /**   是否拦截依赖缺少的异常 , 默认true
     true: 当有依赖模块缺少时 ， 抛出异常 ， 方便查找问题
     false: 不拦截错误 ， 方便代码导入AS ， 但是缺少依赖构建过程出现的类缺失异常问题很难定位
     **/
    def allowLose = false

    /**  是否使用日志
     true:打印日志
     **/
    def log = true

    /**
     *  模块依赖方式，默认: mavenOnly
     * mavenOnly:只使用maven仓库进行依赖，不关联本地代码
     * mavenFirst:优先使用maven仓库进行依赖，当仓库不存在依赖时，关联本地代码依赖
     * localFirst:优先使用本地导入模块代码，没有导入则使用maven仓库
     * localOnly:只使用本地导入的模块，禁用ｍａｖｅｎ仓库
     */
    def dependentModel = "localFirst"

    /** 指定一个版本号的map文件 ， 则会优先使用该文件中的信息进行版本号管理 **/
    def versionFile = ""
}