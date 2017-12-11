package com.pqixing.modularization.models

import com.pqixing.modularization.Default
import com.pqixing.modularization.tasks.UploadTask
import com.pqixing.modularization.utils.FileUtils
import com.pqixing.modularization.utils.NormalUtils
import com.pqixing.modularization.utils.Print
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
/**
 * Created by pqixing on 17-12-7.
 */

class ModuleConfig extends BaseExtension {
    protected final Project project

    final NamedDomainObjectContainer<RunType> runTypes
    final NamedDomainObjectContainer<MavenType> mavenTypes
    final BuildConfig buildConfig
    final AndroidConfig androidConfig

    final String pluginType

    LinkedList<String> reposPaths
    LinkedList<String> defaultImplRepo

    private final HashMap<String, String> repoVersions

    String selectRunType = ""
    String selectMavenType = "debug"

    String pom_version
    boolean uploadEnable = false

    ModuleConfig(Project project
                 , NamedDomainObjectContainer<RunType> runTypes
                 , NamedDomainObjectContainer<MavenType> mavenTypes, String pluginType) {
        this.pluginType = pluginType
        this.project = project
        buildConfig = new BuildConfig(project)

        androidConfig = new AndroidConfig(project)
        androidConfig.updateMeta(project)
        repoVersions = new HashSet<>()


        reposPaths = new LinkedList<>()
        if (project.hasProperty("reposPaths")) {
            reposPaths += project.ext.get("reposPaths")
        }

        defaultImplRepo = project.hasProperty("defaultImplRepo") ?
                new GroovyShell().evaluate(project.ext.get("defaultImplRepo"))
                : Default.defaultImplRepo

        this.mavenTypes = mavenTypes
        mavenTypes.whenObjectAdded { it.onCreate(project) }

        mavenTypes.add(new MavenType("release"))
        mavenTypes.add(new MavenType("debug"))
        mavenTypes.add(new MavenType("test"))

        this.runTypes = runTypes
        runTypes.whenObjectAdded { it.onCreate(project) }
    }

    MavenType getMavenType() {
        if (mavenTypes.hasProperty(selectMavenType)) return mavenTypes.getByName(selectMavenType)
        return null
    }

    RunType getRunType() {
        if (runTypes.hasProperty(selectRunType)) return runTypes.getByName(selectRunType)
        return null
    }

    String getCompilePluginType() {
        if ("application" == pluginType) return pluginType
        if (runType?.asApp) return "application"
        return "library"
    }

    void runTypes(String selectItem = this.selectRunType, Closure closure) {
        this.selectRunType = selectItem
        runTypes.configure(closure)
    }

    void mavenTypes(String selectItem = this.selectMavenType, Closure closure) {
        this.selectMavenType = selectItem
        mavenTypes.configure(closure)
    }

//    void buildConfig(Closure closure) {
//        buildConfig.configure(closure)
//    }

    void androidConfig(Closure closure) {
        androidConfig.configure(closure)
    }

    void onConfigEnd() {
        reposPaths.each { path ->
            Properties p = new Properties()
            p.load(new File(path).newInputStream())
            repoVersions.putAll(p.getProperties())
        }
        repoVersions.putAll(mavenType.repoVersions)

        Print.outputFile = new File(buildConfig.outDir, "log.txt")
    }

    void afterApplyAndroid() {
        if ("library" == pluginType)  uploadToMavenTask()
        Print.ln(toString())
    }
/**
 * 上传数据到maven仓库
 */
    private void uploadToMavenTask() {
        def listTask = []
        //添加上传的任务
        mavenTypes.each { m ->
            switch (m.name) {
                case "release":
                case "test":
                case "debug":
                    m.onCreate(project)
                    break
            }
            if (NormalUtils.isEmpty(m.pom_version)) m.pom_version = pom_version
            if (NormalUtils.isEmpty(m.uploadEnable)) m.uploadEnable = uploadEnable

            if (m.uploadEnable && ("release" != m.name || Default.uploadKey == m.uploadKey)) {
                String taskName = "up-$project.name$m.name"
                listTask += project.task(taskName, type: UploadTask) { mavenInfo = m }
            }
        }
        project.afterEvaluate {
            listTask.each { it.dependsOn project.assembleRelease }
        }

//        project.task("uploadAll") {
//            group = Default.taskGroup
//            doFirst { listTask.each { it.execute() } }
//        }
    }

/**
 * 获取依赖的字符串
 * @param key
 * @param value
 * @return
 */
    String getRepoVersionStr(String key, String value = "") {
        key = key.replace(":", "")
        if (NormalUtils.isEmpty(value)) value = repoVersions[key]
        if (NormalUtils.isEmpty(value)) value = "+"
        BuildConfig buildConfig = project.buildConfig
        return buildConfig.groupName + ".android:$key:$value"
    }

    @Override
    LinkedList<String> generatorFiles() {
        LinkedList<String> files = []
        files += androidConfig.generatorFiles()

        if (!NormalUtils.isEmpty(runType)) files += runType.generatorFiles()
        if (!NormalUtils.isEmpty(defaultImplRepo)) {
            StringBuilder sb = new StringBuilder("dependencies { \n")
            defaultImplRepo.each { repoKey ->
                sb.append("    implementation '${getRepoVersionStr(repoKey)}' \n")
            }
            files += FileUtils.write(new File(project.buildConfig.cacheDir, "dependencies.gradle"), sb.append("}").toString())
        }
        return files
    }


    @Override
    public String toString() {
        return "ModuleConfig{" +
                "\nrunTypes=" + runTypes +
                ",\n mavenTypes=" + mavenTypes +
                ",\n buildConfig=" + buildConfig +
                ",\n androidConfig=" + androidConfig +
                ", pluginType='" + pluginType + '\'' +
                ",\n reposPaths=" + reposPaths +
                ",\n defaultImplRepo=" + defaultImplRepo +
                ",\n repoVersions=" + repoVersions +
                ", selectRunType='" + selectRunType + '\'' +
                ", selectMavenType='" + selectMavenType + '\'' +
                ", pom_version='" + pom_version + '\'' +
                ", uploadEnable=" + uploadEnable +
                '}';
    }
}
