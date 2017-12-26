package com.pqixing.modularization.plugins

import com.pqixing.modularization.Default
import com.pqixing.modularization.utils.Print
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec

import java.util.regex.Pattern

/**
 * Created by pqixing on 17-12-20.
 */

public class GitManager implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        Print.init(project)
        project.buildDir.mkdirs()

        File ignoreFile = project.file(".gitignore")
        if (!ignoreFile.exists()) ignoreFile.createNewFile()
        if (!ignoreFile.text.contains("config.gradle")) ignoreFile.append("\nconfig.gradle \n")

        def settingFile = new File(project.buildDir, "setting.gradle")
        boolean exit = settingFile.exists()
        settingFile.write(configGradleTxt + "\n" + settingGradleTxt)
        if (!exit) throw new RuntimeException("generator setting file, please sync again")
        project.task("cloneAllProjects", type: Exec) {
            group = Default.taskGroup
            if (System.getProperty('os.name').toLowerCase(Locale.ROOT).contains('windows')) {
                commandLine 'cmd', '/c', 'build\\clone.bat'
            } else {
                commandLine 'sh', './build/clone.sh'
            }
        }

        //所有工程的父目录
        Map<String, String> modulePaths = getModulePaths(rootDir.parentFile, 3)
        List<String> modules
        Map<String, String> includePaths = new HashMap<>()

        modules.each {name ->
            Pattern pattern = Pattern.compile(name)
            includePaths  modulePaths.findAll { it.key.matches(pattern) }
        }

        StringBuilder mStr = new StringBuilder()
        modules.each { moduleName ->
            def path = modulePaths[moduleName]
            if (path != null) mStr.append("include ':$moduleName' \nproject(':$moduleName').projectDir = new File('$path') \n")
            else throw new RuntimeException("can't find $moduleName on local ,please check name or clone project use task cloneAllProjects first !!")
        }
    }

    String getConfigGradleTxt() {
        return '''
def config =file("../config.gradle")
if(!config.exists()){
config.write(\'\'\'//set git userName 
gitUserName = ""
//set git password
gitPassWord = ""
//add module into this project
//modules+="router"
    \'\'\'
)
}
'''
    }
    /**
     * 输出config
     * @param project
     */
    String getSettingGradleTxt() {
        return '''
/********生成config.gradle文件**********/
ext.gitUserName = ""
ext.gitPassWord = ""
ext.modules = []
apply from: "../config.gradle"
/********生成config.gradle文件*******/
/********生成批量clone脚本*******/
def gitUrls = new Properties()
gitUrls.load(file("../giturl.properties").newInputStream())
StringBuilder batStr = new StringBuilder().append("cd ../ \\n")
StringBuilder shStr = new StringBuilder().append("cd ../ \\n")

gitUrls.toSpreadMap().each { map ->
//    println("key = $map.key value = $map.value")
    String cloneUrl = "git clone http://${gitUserName}:${gitPassWord}@${map.value}"
    if (map.value.toString().isEmpty()) cloneUrl = "git clone http://${gitUserName}:${gitPassWord}@192.168.3.200/android/${map.key}.git"
    shStr.append("if [ ! -d ${map.key} ]; then ").append("$cloneUrl ;").append("fi;\\n")
    batStr.append("if not exist ${map.key} (  ").append("$cloneUrl").append(" ) \\n")
}

shStr.append("echo chone end ${gitUrls.toSpreadMap()}")
file("clone.sh").write("#！/BIN/BASH \\n" + shStr.toString())
file("clone.bat").write(batStr.toString())
/********生成批量clone脚本*******/

/********导入工程*******/
//所有工程的父目录
Map<String, String> modulePaths = getModulePaths(rootDir.parentFile, 3)
Map<String, String> includePaths = new HashMap<>()
modules.each {name ->
    Pattern pattern = Pattern.compile(name)
    includePaths += modulePaths.findAll { it.key.matches(pattern) }
}

StringBuilder mStr = new StringBuilder()
includePaths.each { map ->
     mStr.append("include ':$map.key' \\n project(':map.key').projectDir = new File('$map.value') \\n")
}

println("modules = $modules modulePaths = $modulePaths")
def moduleGradle = file("module.gradle")
moduleGradle.write(mStr.toString())
apply from: moduleGradle.path
/********导入工程*******/

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
'''
    }
}
