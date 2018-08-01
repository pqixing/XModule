package com.pqixing.modularization.dependent

import com.alibaba.fastjson.JSON
import com.pqixing.modularization.Keys
import com.pqixing.modularization.ModuleConfig
import com.pqixing.modularization.base.BaseExtension
import com.pqixing.modularization.common.BuildConfig
import com.pqixing.modularization.common.GlobalConfig
import com.pqixing.modularization.git.GitConfig
import com.pqixing.modularization.maven.MavenType
import com.pqixing.modularization.utils.*
import com.pqixing.modularization.wrapper.PomWrapper
import com.pqixing.modularization.wrapper.ProjectWrapper
import org.gradle.api.Project

/**
 * Created by pqixing on 17-12-25.
 */

class Dependencies extends BaseExtension {

    //对应all*.exclude
    HashMap<String, Map<String, String>> allExcludes
    HashSet<Module> modules
    //传递下来的master分支的exclude
    Set<String> masterExclude = new HashSet<>()
    Set<Module> dependentLose = new HashSet<>()
    //本地依赖的模块名称,可传递给主工程使用
    Set<Module> localDependency
    Set<String> localImportModules

    MavenType mavenType

    boolean autoImpl = true
    /**
     * 给全部依赖库添加
     * @param exclude
     */
    void allExclude(Map<String, String> exclude) {
        def key = exclude.toString()
        def contains = allExcludes.containsKey(key)
        Print.ln("allExclude $contains $key")
        if(!contains) {
            allExcludes.put(key, exclude)
        }
    }

    Dependencies(Project project) {
        super(project)
        modules = new HashSet<>()
        allExcludes = new HashMap<>()
    }


    Module add(String moduleName, Closure closure = null) {
        def inner = new Module()
        inner.moduleName = moduleName
        if (closure != null) {
            closure.delegate = inner
            closure.setResolveStrategy(Closure.DELEGATE_ONLY)
            closure.call()
        }
        modules += inner
        return inner
    }

    Module addImpl(String moduleName, Closure closure = null) {
        Module inner = add(moduleName, closure)
        inner.scope = Module.SCOP_API
        return inner
    }

    List<String> getModuleNames() {
        List<String> names = new LinkedList<>()
        modules.each { names += it.moduleName }
        return names
    }

    /**
     * 获取该aar在仓库的最后一个一个版本
     * @param artifactId
     * @return
     */
    String getLastVersion(String group, String artifactId) {
        String version = MavenUtils.getVersion(mavenType.name, wrapper.getExtends(GitConfig).branchName, artifactId.trim())
        return version

    }

    void init() {
        mavenType = wrapper.getExtends(ModuleConfig.class).mavenType
        localImportModules = new HashSet<>()
        project.rootProject.allprojects.each { localImportModules += it.name }
        localDependency = new HashSet<>()
        if (autoImpl && !GlobalConfig.autoImpl.contains(project.name)) {//如果当前不是需要自动导入的工程之一，则自动导入依赖
            GlobalConfig.autoImpl.each { addImpl(it) }
        }
    }

    /**
     * 添加依赖去除
     * @param sb
     * @param module
     */
    String excludeStr(String prefix, Collection<Map<String, String>> excludes) {
        StringBuilder sb = new StringBuilder()
        excludes.each { item ->
            sb.append("    $prefix ( ")
            item.each { map ->
                String value = map.value
                if (CheckUtils.isEmpty(value)) value = Keys.TAG_EMPTY
                sb.append("$map.key : '$value',")
            }
            sb.deleteCharAt(sb.length() - 1)
            sb.append(" ) \n")
        }
        return sb.toString()
    }
    /**
     * 进行本地依赖
     * @param module
     * @return
     */
    boolean onLocalCompile(StringBuilder sb, Module module) {
        //如果该依赖没有本地导入，不进行本地依赖
        if (!localImportModules.contains(module.moduleName)) return false
        sb.append(" $module.scope ( project(':$module.moduleName')) {")
        sb.append("${excludeStr("exclude", module.excludes)} }\n")
        module.onLocalCompile = true

        //如果有本地依赖工程，则移除相同的仓库依赖
        localDependency.add(module)
    }
    /**
     * 进行仓库依赖
     * @param module
     * @return
     */
    boolean onMavenCompile(StringBuilder sb, Module module) {
        String lastVersion = getLastVersion(module.groupId, TextUtils.getBranchArtifactId(module.moduleName, wrapper))
        if (!CheckUtils.isVersionCode(lastVersion)) {
            lastVersion = getLastVersion(module.groupId, module.moduleName)
        } else module.artifactId = TextUtils.getBranchArtifactId(module.moduleName, wrapper)

        if (!CheckUtils.isVersionCode(lastVersion)) return false//如果分支和master都没有依赖，则仓库依赖失败

        //如果配置中没有配置指定版本号，用最新版本好，否则，强制使用配置中的版本号mvpbase
        String focusVersion = ""
        if (CheckUtils.isVersionCode(module.version)) {//
            focusVersion = " \n force = true \n"
        } else module.version = lastVersion
        sb.append(" $module.scope ('$module.groupId:$module.artifactId:$module.version') { $focusVersion")
        sb.append("${excludeStr("exclude", module.excludes)} }\n")

        //如果依赖的是分支，获取该依赖中传递的master仓库依赖去除
        if (module.artifactId.contains(Keys.BRANCH_TAG)) {
            def wrapper = PomWrapper.create(mavenType.maven_url, module.groupId, module.artifactId, module.version)
            wrapper.loadModule(module)
            masterExclude.addAll(wrapper.masterExclude)
            masterExclude.add(module.moduleName)
//            module.excludes.each { m -> allExclude(m) }
        }
        return true
    }
    /**
     * 抛出依赖缺失异常
     * @param module
     */
    void throwCompileLose(Module module) {
        if (GlobalConfig.abortDependentLose) throw new RuntimeException("Lose dependent $module.artifactId , please chack config!!!!!!!")
        dependentLose += module
    }

    @Override
    LinkedList<String> getOutFiles() {
        init()
        Print.ln("onDp out -> $project.name")
        StringBuilder sb = new StringBuilder("dependencies { \n")
        modules.each { model ->
            if (model.moduleName == project.name) return
            boolean compile
            switch (GlobalConfig.dependentModel) {
            //只依赖本地工程
                case "localOnly":
                    compile = onLocalCompile(sb, model)
                    break
            //优先依赖本地工程
                case "localFirst":
                    compile = onLocalCompile(sb, model) || onMavenCompile(sb, model)
                    break
            //优先仓库版本
                case "mavenFirst":
                    compile = onMavenCompile(sb, model) || onLocalCompile(sb, model)
                    break
            //只依赖仓库版本
                case "mavenOnly":
                default:
                    compile = onMavenCompile(sb, model)
                    break
            }
            if (!compile) throwCompileLose(model)
        }
        sb.append("} \nconfigurations { \n")
        masterExclude.each { name ->
            allExclude(group: GlobalConfig.groupName, module: name)
        }
        localDependency.each { model ->
            String branchVersion = getLastVersion(model.groupId, model.moduleName)
            if (CheckUtils.isVersionCode(branchVersion)) {
                allExclude([group: model.groupId, module: model.moduleName])
            }

            branchVersion = getLastVersion(model.groupId, TextUtils.getBranchArtifactId(model.moduleName, wrapper))
            if (CheckUtils.isVersionCode(branchVersion)) {
                allExclude([group: model.groupId, module: TextUtils.getBranchArtifactId(model.moduleName, wrapper)])
            }
        }
        if(masterExclude.isEmpty()) masterExclude.add(Keys.TAG_EMPTY)
        allExclude(group: Keys.GROUP_MASTER, module: "${TextUtils.collection2Str(masterExclude)}")
        sb.append("${excludeStr("all*.exclude", allExcludes.values())}} \n")
//        saveVersionMap()

        if (!CheckUtils.isEmpty(dependentLose)) Print.lnf("$project.name dependentLose : ${JSON.toJSONString(dependentLose)}")
        return [FileUtils.write(new File(wrapper.getExtends(BuildConfig).cacheDir, "dependencies.gradle"), sb.toString())];
    }
    /**
     * 是否有本地依赖存在
     * @return
     */
    boolean getHasLocalModule() {
        return !CheckUtils.isEmpty(localDependency)
    }
}
