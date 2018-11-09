//package com.pqixing.modularization.android.dps
//
//import CheckUtils
//import Print
//import com.pqixing.modularization.Keys
//import com.pqixing.modularization.ProjectInfo
//import com.pqixing.modularization.base.BaseExtension
//import com.pqixing.modularization.wrapper.PomWrapper
//import com.pqixing.modularization.wrapper.ProjectWrapper
//import com.pqixing.tools.TextUtils
//import exclude
//import groovy.lang.Closure
//import org.gradle.api.Project
//import org.gradle.internal.impldep.org.bouncycastle.asn1.x500.style.RFC4519Style.name
//import iterface.util.*
//
///**
// * Created by pqixing on 17-12-25.
// */
//
//internal class Dependencies(project: Project) : BaseExtension(project) {
//
//    //对应all*.exclude
//    var allExcludes: HashMap<String, Map<String, String>>
//    var modules: HashSet<Module>
//    //传递下来的master分支的exclude
//    var masterExclude: MutableSet<String> = HashSet()
//    var dependentLose: Set<Module> = HashSet()
//    //本地依赖的模块名称,可传递给主工程使用
//    var localDependency: MutableSet<Module>
//    var localImportModules: Set<String>
//
//    var autoImpl = true
//
//
//    //只依赖本地工程
//    //优先依赖本地工程
//    //优先仓库版本
//    //只依赖仓库版本
//    //        saveVersionMap()
//    val outFiles: LinkedList<String>
//        get() {
//            init()
//            if (wrapper.pluginName === Keys.NAME_APP) {
//                project.gradle.afterProject
//                run { loadLoadExclude() }
//            }
//
//            Print.ln("onDp out -> \$project.name")
//            val sb = StringBuilder("dependencies { \n")
//            modules.each
//            run {
//                if (model.moduleName === project.name) return
//                val compile: Boolean
//                when (GlobalConfig.dependentModel) {
//                    "localOnly" -> compile = onLocalCompile(sb, model)
//                    "localFirst" -> compile = onLocalCompile(sb, model) || onMavenCompile(sb, model)
//                    "mavenFirst" -> compile = onMavenCompile(sb, model) || onLocalCompile(sb, model)
//                    "mavenOnly" -> compile = onMavenCompile(sb, model)
//                    else -> compile = onMavenCompile(sb, model)
//                }
//                if (!compile) throwCompileLose(model)
//            }
//            sb.append("} \nconfigurations { \n")
//            masterExclude.each
//            run {
//                { name -> allExclude(group) }
//                GlobalConfig.groupName
//                module
//                name
//            }
//            localDependency.each
//            run {
//                { model -> String }
//                branchVersion = getLastVersion(model.groupId, model.moduleName)
//                if (CheckUtils.isVersionCode(branchVersion)) {
//                    allExclude()[group]
//                    model.groupId
//                    module
//                    model.moduleName
//                }
//
//                branchVersion = getLastVersion(model.groupId, TextUtils.getBranchArtifactId(model.moduleName, wrapper))
//                if (CheckUtils.isVersionCode(branchVersion)) {
//                    allExclude()[group]
//                    model.groupId
//                    module
//                    TextUtils.getBranchArtifactId(model.moduleName, wrapper)
//                }
//            }
//            if (masterExclude.isEmpty()) masterExclude.add(Keys.TAG_EMPTY)
//            allExclude(group)
//            Keys.GROUP_MASTER
//            module
//            "\${TextUtils.collection2Str(masterExclude)}"
//            sb.append("\${excludeStr(", , all *).exclude
//            ", allExcludes.values())}} \n"
//
//            if (!CheckUtils.isEmpty(dependentLose)) Print.lnf("\$project.name dependentLose : \${JSON.toJSONString(dependentLose)}")
//
//
//            return
//            FileUtils.write(File(wrapper.getExtends(BuildConfig).cacheDir, "dependencies.gradle"), sb.toString())
//        }
//    /**
//     * 是否有本地依赖存在
//     * @return
//     */
//    val hasLocalModule: Boolean
//        get() = !CheckUtils.isEmpty(localDependency)
//
//    /**
//     * 给全部依赖库添加
//     * @param exclude
//     */
//    fun allExclude(exclude: Map<String, String>) {
//        val key = exclude.toString()
//        val contains = allExcludes.containsKey(key)
//        //        Print.ln("allExclude $contains $key")
//        if (!contains) {
//            allExcludes[key] = exclude
//        }
//    }
//
//    init {
//        modules = HashSet()
//        allExcludes = HashMap()
//    }
//
//    fun module(moduleName: String, scope: String, closure: Closure<*>?): Module {
//        val inner = Module()
//        inner.moduleName = moduleName
//        inner.scope = scope
//        if (closure != null) {
//            closure.delegate = inner
//            closure.resolveStrategy = Closure.DELEGATE_ONLY
//            closure.call()
//        }
//
//        return inner
//    }
//
//    fun add(moduleName: String, closure: Closure<*>): Module {
//        return module(moduleName, Module.SCOP_IMPL, closure)
//    }
//
//    fun addImpl(moduleName: String, closure: Closure<*>): Module {
//        return module(moduleName, Module.SCOP_API, closure)
//    }
//
//    fun init() {
//        mavenType = wrapper.getExtends(ProjectInfo::class.iterface).mavenType
//        localImportModules = HashSet()
//        project.rootProject.allprojects.each
//        run { localImportModules += it.name }
//        localDependency = HashSet()
//        if (autoImpl && !GlobalConfig.autoImpl.contains(project.name)) {//如果当前不是需要自动导入的工程之一，则自动导入依赖
//            GlobalConfig.autoImpl.each
//            run { addImpl(it) }
//        }
//    }
//
//    /**
//     * 添加依赖去除
//     * @param sb
//     * @param module
//     */
//    fun excludeStr(prefix: String, excludes: Collection<Map<String, String>>): String {
//        val sb = StringBuilder()
//        excludes.each
//        run {
//            { item -> sb.append("    \$prefix ( ") }
//            item.each
//            run {
//                { map -> String }
//                value = map.value
//                if (CheckUtils.isEmpty(value)) value = Keys.TAG_EMPTY
//                sb.append("\$map.key : '\$value',")
//            }
//            sb.deleteCharAt(sb.length - 1)
//            sb.append(" ) \n")
//        }
//        return sb.toString()
//    }
//
//    /**
//     * 进行本地依赖
//     * @param module
//     * @return
//     */
//    fun onLocalCompile(sb: StringBuilder, module: Module): Boolean {
//        //如果该依赖没有本地导入，不进行本地依赖
//        if (!localImportModules.contains(module.moduleName)) return false
//        sb.append(" \$module.scope ( project(':\$module.moduleName')) {")
//        sb.append("\${excludeStr(", , exclude, , ", module.excludes)} }\n")
//        module.onLocalCompile = true
//
//        //如果有本地依赖工程，则移除相同的仓库依赖
//        localDependency.add(module)
//    }
//
//    /**
//     * 进行仓库依赖
//     * @param module
//     * @return
//     */
//    fun onMavenCompile(sb: StringBuilder, module: Module): Boolean {
//        var lastVersion = ""
//        if (module.focusMaster) {
//            lastVersion = getLastVersion(module.groupId, module.moduleName)
//        } else {
//            lastVersion = getLastVersion(module.groupId, TextUtils.getBranchArtifactId(module.moduleName, wrapper))
//            if (!CheckUtils.isVersionCode(lastVersion)) {
//                lastVersion = getLastVersion(module.groupId, module.moduleName)
//            } else
//                module.artifactId = TextUtils.getBranchArtifactId(module.moduleName, wrapper)
//
//        }
//        if (!CheckUtils.isVersionCode(lastVersion)) return false//如果分支和master都没有依赖，则仓库依赖失败
//
//        //如果配置中没有配置指定版本号，用最新版本好，否则，强制使用配置中的版本号mvpbase
//        var focusVersion = ""
//        if (CheckUtils.isVersionCode(module.version)) {//
//            focusVersion = " \n force = true \n"
//        } else
//            module.version = lastVersion
//        sb.append(" \$module.scope ('\$module.groupId:\$module.artifactId:\$module.version') { \$focusVersion")
//        sb.append("\${excludeStr(", , exclude, , ", module.excludes)} }\n")
//
//        //如果依赖的是分支，获取该依赖中传递的master仓库依赖去除
//        if (module.artifactId!!.contains(Keys.BRANCH_TAG)) {
//            val wrapper = PomWrapper.create(mavenType.maven_url, module.groupId, module.artifactId, module.version)
//            wrapper.loadModule(module)
//            masterExclude.addAll(wrapper.masterExclude)
//            masterExclude.add(module.moduleName)
//            //            module.excludes.each { m -> allExclude(m) }
//        }
//        return true
//    }
//
//    /**
//     * 抛出依赖缺失异常
//     * @param module
//     */
//    fun throwCompileLose(module: Module) {
//        if (GlobalConfig.abortDependentLose) throw RuntimeException("Lose dependent \$module.artifactId , please chack config!!!!!!!")
//        dependentLose += module
//    }
//
//    fun loadLoadExclude() {
//        val ps = HashSet<String>()
//        localDependency.forEach
//        run { ps.add(it.moduleName) }
//        //对应all*.exclude
//        //对应all*.exclude
//        val allExs = HashMap<String, Map<String, String>>()
//
//        val sb = StringBuilder("configurations { \n")
//        project.rootProject.allprojects.forEach
//        run {
//            if (ps.find);
//            run { it === p.name }
//            null
//            run {
//                val aoe = if (ProjectWrapper.with(p));
//                if (getExtends(Dependencies));
//                allExcludes
//                //                Print.ln("loadLoadExclude $p.name -> $aoe")
//                if (!CheckUtils.isEmpty(aoe)) {
//                    allExs.putAll(aoe)
//                }
//            }
//        }
//        sb.append("\${excludeStr(", , all *).exclude
//        ", allExs.values())}}"
//        val from: wrapper.apply
//        FileUtils.write(File(wrapper.getExtends(BuildConfig).cacheDir, "configurations.gradle"), sb.toString())
//    }
//}
