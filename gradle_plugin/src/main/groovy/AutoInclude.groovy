import org.gradle.api.invocation.Gradle

/**
 * Created by pqixing on 18-2-3.
 * this class will run on setting.gradle,so can not import any other class
 */
public class AutoInclude {

    Gradle gradle
    File rootDir
    File outIncludeFile
    /**
     * 所有project以及对应的submodule
     */
    HashMap<String, List<String>> submodules

    /**
     * 所有project的Url地址
     */
    HashMap<String, String> projectUrls
    HashMap<String, String> localProject = [:]
    Set<String> includes = []
    private String baseGitUrl
    private String username
    private String password
    private String email


    AutoInclude(Gradle gradle, File rootDir, File outIncludeFile) {
        this.gradle = gradle
        this.rootDir = rootDir
        this.outIncludeFile = outIncludeFile
    }
    /**
     * 解析本地需要导入的工程
     */
    void formatInclude(StringBuilder icTxt) {
        File includeFile = new File(rootDir, AutoConfig.TXT_INCLUDE)
        if (!includeFile.exists()) includeFile.write(AutoConfig.mould_include)
        readInclude(icTxt, includeFile)
        //读取隐藏配置
        File hideIncludeFile = new File(rootDir, AutoConfig.TXT_HIDEINCLUDE)
        if (hideIncludeFile.exists()) readInclude(icTxt, hideIncludeFile)

        gradle.ext.gitUserName = username
        gradle.ext.gitPassword = password
        gradle.ext.gitEmail = email
    }

    void readInclude(StringBuilder autoTxt, File f) {
        if (!f.exists() || !f.isFile()) return
        boolean end = false
        StringBuilder icTxt = new StringBuilder()
        f.eachLine { line ->
            end |= line.startsWith(AutoConfig.TAG_AUTO_ADD)
            if (end) return

            def map = line.replaceAll("//.*", "").split("=")
            if (map.length < 2) return
            String key = map[0].trim()
            String value = map[1].trim()
            switch (key) {
                case "username": username = value
                    break
                case "password": password = value
                    break
                case "email": email = value
                    break
                case "targetInclude": readInclude(autoTxt, new File(rootDir, value))
                    break
                case "include":
                    value?.split(",")?.each { includes += it.trim() }
                    break
            }
            icTxt.append(line).append("\\n")
        }
        f.write("${icTxt.toString()}\\n$AutoConfig.TAG_AUTO_ADD ------------- the config below this line is not work!!!!! \\n${autoTxt.toString()}")
    }
    /**
     * 解析default xml 加载所有的git信息
     */
    void formatXml(StringBuilder icTxt) {
        File defaultXml = null
        String error = ""

        //优先使用文档仓库中存在的文件
        if (!AutoConfig.XML_DEFAULT_GIT.startsWith("#")) {
            String docDir = AutoConfig.XML_DEFAULT_GIT.substring(AutoConfig.XML_DEFAULT_GIT.lastIndexOf("/") + 1).replace(".git", "")
            defaultXml = new File(rootDir.parentFile, "$docDir/$AutoConfig.XML_DEFAULT_NAME")
            if (!defaultXml.exists()) {
                error = "git clone $AutoConfig.XML_DEFAULT_GIT".execute(null, rootDir.parentFile)?.text
            }
            includes += docDir
        }
        if (!defaultXml?.exists() ?: false) {
            defaultXml = new File(rootDir, AutoConfig.XML_DEFAULT_NAME)
            println "clone faile please check url: $AutoConfig.XML_DEFAULT_GIT error : $error"
        }
        if (!defaultXml.exists()) defaultXml.write(AutoConfig.mould_gitproject)


        Node gitNode = new XmlParser().parse(defaultXml)
        submodules = [:]
        projectUrls = [:]
        baseGitUrl = gitNode.@baseUrl
        gitNode.project.each { Node p ->
            String name = p.@name
            String url = p.@url
            if (url?.isEmpty() ?: true) url = "$baseGitUrl/${name}.git"
            String introduce = p.@introduce

            projectUrls.put(name, "$url$AutoConfig.SEPERATOR$introduce")
            icTxt.append(getIncludeLine("include = $name", introduce))

            List<String> subLists = []
            p.submodule.each { Node s ->
                String s_name = s.@name
                String s_introduce = s.@introduce
                subLists += "${s_name}$AutoConfig.SEPERATOR${s_introduce}"
                icTxt.append(getIncludeLine("     |- - $s_name", s_introduce))
            }
            if (!subLists.isEmpty()) submodules.put(name, subLists)
        }
        gradle.ext.submodules = submodules
        gradle.ext.projectUrls = projectUrls
        gradle.ext.baseGitUrl = baseGitUrl
    }

    String getIncludeLine(String includeKey, String introduce) {
        StringBuilder sb = new StringBuilder(includeKey)
        int space = 50 - sb.length()
        for (int i = 0; i < space; i++) {
            sb.append(" ")
        }
        return sb.append("//-- $introduce \\n")
    }

    void saveToFile(Map<String, String> realInclude) {
        StringBuilder sb =  new StringBuilder("//Auto Add By Modularization")
        realInclude.each { map ->
            sb.append("\\ninclude (':$map.key') \\n")
                    .append("project(':$map.key').projectDir = new File('$map.value')")
        }
        outIncludeFile.write(sb.toString())
    }

    void save() {
        StringBuilder icTxt = new StringBuilder()
        formatXml(icTxt)
        formatInclude(icTxt)
        formatLocalPath(localProject, rootDir.parentFile, 3)
        Map<String, String> realInclude = [:]

        includes.each { key ->
            String url = localProject.find { it.key == key }?.value
            //如果本地存在工程目录，直接导入,否则尝试从网络导入
            if (url != null && !url.isEmpty()) {
                realInclude.put(key, url)
            } else fromGit(realInclude, key)
        }
        saveToFile(realInclude)
        gradle.ext.localProject = localProject
        //让save不显示灰色
        if (false) save()
    }
    /**
     * 从git中自动下载依赖
     */
    void fromGit(Map<String, String> realInclude, String moduleName) {
        boolean find = false
        projectUrls.each { map ->
            if (find) return
            String gitUrl = map.value.split(AutoConfig.SEPERATOR)[0]
            StringBuilder localPath = new StringBuilder(rootDir.parentFile.path)
            if (map.key == moduleName) {//当前工程就是目标
                localPath.append("/$moduleName")
                //子工程包含目标模块
            } else if (submodules[map.key]?.find {
                it.startsWith("$moduleName$AutoConfig.SEPERATOR")
            } != null) {
                localPath.append("/$map.key/$moduleName")
            } else return
            if (username == null || password == null
                    || username.isEmpty() || password.isEmpty()) throw new RuntimeException("you must config git username and password before clone code from git!!!!!")
            //如果本地不存在该目录
            File localDir = new File(localPath.toString())
            String urlWitUser = gitUrl.replace("//", "//$username:$password@")

            String error = ""
            if (!localDir.exists()) {
                println("clone .... $urlWitUser")
                error = "git clone ${urlWitUser}".execute(null, rootDir.parentFile)?.text
                println("clone end.... $urlWitUser")
            }
            if (!localDir.exists()) throw new RuntimeException("clone faile please check url: $urlWitUser error : $error")
            realInclude.put(moduleName, localDir.path)
            //如果重新clone，则重新加载本地工程目录数据
            formatLocalPath(localProject, rootDir.parentFile, 3)
            find = true
        }
    }
    /**
     *  解析本地已经存在的module工程
     * @param dir
     * @param deep 层级，如果小于0 停止获取
     * @return
     */
    void formatLocalPath(Map<String, String> locals, File dir, int deep) {
        if (deep < 0) return
        File buildGradle = new File(dir, "build.gradle")
        if (buildGradle.exists()) locals.put("$dir.name", dir.path.replace("\\\\", "/"))
        dir.eachDir { formatLocalPath(locals, it, deep - 1) }
    }
}

class AutoConfig {

    static final String TAG_AUTO_ADD = "Auto Add By Modularization"

    /**
     * 默认的defaultxml路径
     */
    static final String XML_DEFAULT_GIT = "#{defaultXmlGitUrl}"
    /**
     * 代码分割线
     */
    static final String SEPERATOR = "###"
    /**
     * gitxml路径解析
     */
    static final String XML_DEFAULT_NAME = "default.xml"
    /**
     * 本地配置文件路径
     */
    static final String TXT_INCLUDE = "include.txt"
    /**
     * 隐藏的导入文件，批量上传时使用
     */
    static final String TXT_HIDEINCLUDE = "hideInclude.txt"

    /**
     * git配置模板
     */
    static final String mould_gitproject = "<git baseUrl=\\\"http://192.168.3.200/android\\\">\\n" +
            "    <project name = \\\"test\\\"  url=\\\"\\\" introduce = \\\"Example Project\\\">\\n" +
            "        <submodule name =\\\"sub_test\\\" introduce = \\\"Example SubProject\\\"/>\\n" +
            "    </project>\\n" +
            "</git>"

    static
    final String mould_include = "username = \\npassword = \\nemail= \\ninclude = \\ntargetInclude = \\n\\n"
}
