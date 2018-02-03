package com.pqixing.modularization.plugins

import auto.Moulds
import com.pqixing.modularization.Default
import com.pqixing.modularization.Keys
import com.pqixing.modularization.base.BasePlugin
import com.pqixing.modularization.configs.BuildConfig
import com.pqixing.modularization.utils.FileUtils
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.Exec
/**
 * Created by pqixing on 17-12-20.
 */

class GitPlugin extends BasePlugin {
    public static final String mouldVersion = "//1.0"
    /**
     * 设置页面文件
     */
    public static final String SETTING_FILE = "moulds.gradle"
    private HashMap<String, String> gitProject

    @Override
    Set<String> getIgnoreFields() {
        return ["config.gradle", "local.gradle", "giturl.properties", "config.gradle"]
    }

    @Override
    void apply(Project project) {
        super.apply(project)
        addMouldGradle()
        if (writeMouldGradle()) {
            throw new RuntimeException("init setting file, please sync again -- 初始化设置，请重新同步")
        }
        readGitProject(project.gradle)
    }
    /**
     * 从Gradle中获取git工程的信息
     * @param gradle
     */
    void readGitProject(Gradle gradle) {

    }
    /**
     * 修改原来的setting文件
     */
    void addMouldGradle() {
        File setting = new File(project.rootDir, "settings.gradle").with {
            if (!exists()) createNewFile()
        }
        if (!setting.text.contains(Keys.TAG_AUTO_ADD))
            setting.append("file('$BuildConfig.dirName/$SETTING_FILE').with {  if(exists()) apply from: path}")
    }
    /**
     * 输出模板设置文件
     */
    boolean writeMouldGradle() {
        File mouldFile = new File(BuildConfig.rootOutDir, SETTING_FILE)
        //如果模板已经存在，并且版本号不小于当前，则不需要重写
        if (mouldFile.exists() && mouldFile.readLines()[0].trim() >= mouldVersion) return false
        Moulds moulds = Moulds.with()
        moulds.params += ["AutoInclude", moulds.autoInclude]
        FileUtils.write(mouldFile, "$mouldVersion\n$moulds.settingGradle")
        return true
    }

    @Override
    String getPluginName() {
        return Keys.NAME_GIT
    }

    void initGitConfig() {
        boolean hasInit = project.gradle.hasProperty("gitUserName")
        project.ext.gitUserName = hasInit ? project.gradle.gitUserName : ""
        project.ext.gitPassWord = hasInit ? project.gradle.gitPassWord : ""
    }

    void addPatchFile() {
        StringBuilder batStr = new StringBuilder().append("cd ../ \n")
        StringBuilder shStr = new StringBuilder().append("cd ../ \n")
        def gitUserName = project.gitUserName
        def gitPassWord = project.gitPassWord
        gitProject.each { map ->
            String cloneUrl = "git clone http://${gitUserName}:${gitPassWord}@${map.value}"
            if (map.value.toString().isEmpty()) cloneUrl = "git clone http://${gitUserName}:${gitPassWord}@192.168.3.200/android/${map.key}.git"
            shStr.append("echo ------ $map.key \n")
            shStr.append("if [ ! -d ${map.key} ]; then ").append("$cloneUrl ;").append("fi;\n")
            batStr.append("if not exist ${map.key} (  ").append("$cloneUrl").append(" ) \n")
            String pullStr = "cd $map.key \n git pull \n cd ../ \n"
            shStr.append(pullStr)
            batStr.append(pullStr)
        }
        FileUtils.write(new File(project.projectDir, ".modularization/clone.sh"), "#！/BIN/BASH \n" + shStr.toString())
        FileUtils.write(new File(project.projectDir, ".modularization/clone.bat"), batStr.toString())

    }

    void createCloneTask() {
        project.task("cloneAllProjects", type: Exec) {
            doFirst {
                if (CheckUtils.isEmpty(project.gitUserName) || CheckUtils.isEmpty(project.gitPassWord)) {
                    throw new RuntimeException("git gitUserName or gitPassWord can not be null, please config in config.gradle")
                }
            }
            group = Default.taskGroup
            if (System.getProperty('os.name').toLowerCase(Locale.ROOT).contains('windows')) {
                commandLine 'cmd', '/c', '.modularization\\clone.bat'
            } else {
                commandLine 'sh', './.modularization/clone.sh'
            }
        }
    }

    void initGitProject() {
        gitProject = new HashMap<>()
        gitProject += [
                "DachenBase"          : "",
                "CommonLibraryProject": "",
                "DachenBase"          : "",
//                "DachenProject":"",
                "ImSdkProject"        : "",
                "MedicineProject"     : "",
                "phoneMeeting"        : "",
                "MedicalProject"      : "",
                "microschool"         : "",
//                "drugSDK":"",
//                "drug":"",
                "androidEDA"          : "",
                "Document"            : "",
                "AnnotationProject"   : "",
                "mdclogin"            : "",
                "DcRouter"            : "",
                "promotionsdk"        : "",
                "wwhappy"             : "",
        ]
    }
    /**
     * 修改原来的setting文件
     */
    void modifySourceSetting() {
        File settings = new File(project.rootDir, "settings.gradle")
        if (!settings.exists()) settings.createNewFile()
        if (!settings.text.contains("Auto Add For GitManager")) settings.append('''
//Auto Add For GitManager
def f = file(".modularization/settings.gradle")
if (f.exists()) apply from: f.path
''')
    }

    /**
     * 添加设置gradle
     */
    boolean addSettingGradle() {
        File settings = new File(project.projectDir, ".modularization/settings.gradle")
        boolean exists = settings.exists()

        FileUtils.write(settings, '''
ext.gitUserName = ""
ext.gitPassWord = ""
ext.modules = new HashSet<String>()
modules += getLocalModule()
def cf1 = file("../config.gradle")
if (cf1.exists()) apply from: cf1.path

def cf2 = file("../config2.gradle")
if (cf2.exists()) apply from: cf2.path

gradle.ext.gitUserName = gitUserName
gradle.ext.gitPassWord = gitPassWord

/********导入工程*******/
//所有工程的父目录
Map<String, String> modulePaths = getModulePaths(rootDir.parentFile, 3)
modulePaths.remove(rootDir.name)
Map<String, String> includePaths = new HashMap<>()
modules.each {name ->
    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(name.replace(":",""))
    includePaths += modulePaths.findAll { it.key.matches(pattern) }
}

StringBuilder mStr = new StringBuilder()
includePaths.each { map ->
     mStr.append("include ':$map.key' \\n project(':$map.key').projectDir = new File('$map.value') \\n")
}
def moduleGradle = file("module.gradle")
moduleGradle.write(mStr.toString())
apply from: moduleGradle.path
/********导入工程*******/

/**
*获取本地工程
**/
Set<String> getLocalModule(){
    def set = new HashSet<String>()
    if(hasProperty("focusLocal")&& "Y" == focusLocal && hasProperty("localModules")){
        localModules.toString().split(",").each {set+=it}
    }
    return set
}
/**
 * @param dir
 * @param deep 层级，如果小于0 停止获取
 * @return
 */
Map<String, String> getModulePaths(File dir, int deep) {
    if (deep < 0) return [:]
    def map = [:]
    if (new File(dir, "build.gradle").exists()) map.put(dir.name, dir.path.replace("\\\\", "/"))
    dir.listFiles(new FilenameFilter() {
        @Override
        boolean accept(File file, String s) {
            return file.isDirectory()
        }
    }).each { childDir -> map += getModulePaths(childDir, deep - 1) }

    return map
}
''')
        return exists
    }

    void addConfigGradle() {
        File config = new File(project.projectDir, "config.gradle")
        if (config.exists()) return
        FileUtils.write(config, '''
gitUserName = ""
//set git password
gitPassWord = ""
//add module into this project
//modules+="moduleName"
''')
    }
}
