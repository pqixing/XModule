package com.pqixing;

/**
 * 工程配置文件
 * 所有模块，以：加模块名称 extc setting = ":dComm"+":dcNet"
 */
public class Config {

    /**
     * 导入工程,多个工程之间,用 , 号分隔
     * 前缀含义 E#  exclude 当前工程
     * D#   dpsInclude  导入该工程的所有依赖工程,如需生效,需要对该工程执行一次DPSAnalis
     * ED#  dpsExclude
     * eg: include="demo1,D#demo2,ED#demo3"
     * 如果 include字段为空,或等于Auto,则,获取当前需要执行的任务,来自动确定需要导入的工程
     */
    public String include = "";

    /**
     * 本地目录的根目录，用来查找本地目录，以及下载Clone
     */
    public String codeRoot = "../CodeSrc";

    /**
     * 用户名称,可用于git和maven上传时使用
     */
    public String userName = "";

    /**
     * 用户密码,可用于git和maven上传时使用
     */
    public String passWord = "";

    /**
     * 跨分支工程依赖，默认不允许
     */
    public boolean allowDpDiff = false;

    /**
     * 是否拦截依赖缺少的异常 , 默认true
     * true: 当有依赖模块缺少时 ， 抛出异常 ， 方便查找问题
     * false: 不拦截错误 ， 方便代码导入AS ， 但是缺少依赖构建过程出现的类缺失异常问题很难定位
     **/
    public boolean allowLose = false;

    /**
     * 是否使用日志
     * true:打印日志
     **/
    public boolean log = true;

    /**
     * 模块依赖方式，默认: mavenOnly
     * mavenOnly:只使用maven仓库进行依赖，不关联本地代码
     * mavenFirst:优先使用maven仓库进行依赖，当仓库不存在依赖时，关联本地代码依赖
     * localFirst:优先使用本地导入模块代码，没有导入则使用maven仓库
     * localOnly:只使用本地导入的模块，禁用ｍａｖｅｎ仓库
     */
    public String dependentModel = "localFirst";

    /**
     * 指定一个版本号的map文件 ， 则会优先使用该文件中的信息进行版本号管理
     **/
    public String versionFile = "";

    /**
     * 工程编译目录，使用不同的编译目录便于同时执行多个编译事件，例如ToMaven和构建
     */
    public String buildDir = "default";

    /**
     * 部分重要操作需要的密码
     */
    public String screctKey = "";
    /**
     * 运行类型,gradle,plugin,ide
     */
    public String syncType = "gradle";

    /**
     * Task任务运行产物拷贝至指定目录,请注意dir 或者 file类型
     */
    public String taskResultFile = "";

    /**
     * 同步根目录的build.gradle文件(使用doc目录下的文件进行覆盖)
     */
    public boolean syncBuildFile = true;

    /**
     * 运行task任务时,目标分支
     */
    public String taskBranch = "";

    /**
     * 打标签时,需要tag的分支
     */
    public String tagBranchs = "";
    /**
     * toMaven时,忽略检查项目
     * 0:UnCheck null
     * 1:UnCheck branch 不校验新分支第一次提交是否需要升级版本号
     * 2:UnCheck version  不校验是否和上次代码是否相同,允许提交重复
     * 3:UnCheck change  不检验本地是否存在未提交修改
     */
    public String toMavenUnCheck = "0";
}