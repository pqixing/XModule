//package com.pqixing.modularization.android.tasks
//
//
//import com.pqixing.modularization.Keys
//import com.pqixing.modularization.base.BaseExtension
//import com.pqixing.modularization.common.BuildConfig
//import com.pqixing.modularization.git.GitConfig
//import com.pqixing.modularization.templet
//import com.pqixing.modularization.android.dps.Dependencies
//import com.pqixing.modularization.modularization.FileUtils
//import com.pqixing.tools.TextUtils
//import org.gradle.api.Project
//
///**
// * Created by pqixing on 17-12-7.
// */
//
//class PreWriteConfig extends BaseExtension {
//    HashMap<String, String> configs
//
//    PreWriteConfig(Project project) {
//        super(project)
//        configs = new HashMap<>()
//    }
//
//    void addConfig(Map<String, String> configs) {
//        this.configs += configs
//    }
//
//    @Override
//    LinkedList<String> getOutFiles() {
//
//        def buildConfig = wrapper.getExtends(BuildConfig)
//        def moduleConfig = wrapper.getExtends(templet)
//        def gitConfig = wrapper.getExtends(GitConfig)
//        def dependent = wrapper.getExtends(Dependencies)
//
//        StringBuilder dpNames = new StringBuilder()
//        dependent.dpComponents.each { m ->
//            dpNames.append("$Keys.PREFIX_PKG.${m.groupId}.${TextUtils.numOrLetter(m.moduleName).toLowerCase()}.${TextUtils.className(m.moduleName)}Config,")
//        }
//        if (dpNames.length() > 1) dpNames.deleteCharAt(dpNames.length() - 1)
//        String launchClass = "${buildConfig.javaPackage}.${TextUtils.className(project.name)}Launch"
//
//        addConfig("NAME": buildConfig.projectName, "DP_CONFIGS_NAMES": dpNames.toString())
//        addConfig("LAUNCH_CONFIG": launchClass)
////        addConfig("DEPENDENCIES": JSON.toJSONString(dependent.dpComponents).replace("\"", ""))
//
//        addConfig(["BUILD_TIME": System.currentTimeMillis().toString(), "BUILD_TIME_STR": new Date().toLocaleString()])
//        addConfig(["GIT_COMMIT_LOG": gitConfig.lastLog, "GIT_COMMIT_NUM": gitConfig.revisionNum])
//        def confStr = new StringBuilder()
//        configs.each { confStr.append("public static final String $it.key = \"$it.value\"; \n") }
//
//        def writes = new auto.Prewrite()
//        writes.params += buildConfig.properties
//        String className = TextUtils.className("${project.name}Config")
//        writes.params += ["preConfigs": confStr.toString(), "className": className]
//
//        String fullName = "${buildConfig.javaPackage}.$className"
//        FileUtils.write(FileUtils.getFileForClass(buildConfig.javaDir, fullName), writes.configClass)
//
//        File enterFile = FileUtils.getFileForClass(buildConfig.javaDir, Keys.NAME_ENTER_CONFIG)
//        boolean isApp = wrapper.pluginName == Keys.NAME_APP || (moduleConfig.runType?.asApp ?: false)
//        if (!isApp) enterFile.delete()
//        else {
//            writes.params += ["configClass": fullName]
//            writes.params += ["launchClass": launchClass]
//            FileUtils.write(enterFile, writes.enter)
//        }
//
//        return [FileUtils.write(new File(buildConfig.cacheDir, "iterface.gradle"), writes.sourceSet)]
//    }
//}
