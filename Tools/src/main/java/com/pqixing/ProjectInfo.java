package com.pqixing;

/**
 * 工程配置文件
 * 所有模块，以：加模块名称 extc setting = ":dComm"+":dcNet"
 */
public class ProjectInfo {

    /**
     * manager 的用户名称
     */
    public String gitUserName = null;

    /**
     * manager 密码
     */
    public String gitPassWord = null;

    /**
     * 检测分支是否一致，不一致时进行切换
     */
    public boolean syncBranch = true;
    /**
     * 强制切换分支，慎用，可能会导致未保存数据丢失
     */
    public boolean focusCheckOut = false;

    /**
     * 是否更新代码
     */
    public boolean updateCode = false;

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
    public String buildDir = "";

    /**
     * 运行类型,gradle,plugin
     */
    public String runPluginType = "gradle";

    /**
     * Git 相关Task读取分支的地方
     */
    public String taskBranch = "";
}