package com.pqixing.modularization.runtype

import auto.Runtype
import com.pqixing.modularization.Keys
import com.pqixing.modularization.ModuleConfig
import com.pqixing.modularization.base.BaseContainer
import com.pqixing.modularization.common.BuildConfig
import com.pqixing.modularization.common.GlobalConfig
import com.pqixing.modularization.utils.CheckUtils
import com.pqixing.modularization.utils.FileUtils
import com.pqixing.modularization.utils.TextUtils
import com.pqixing.modularization.wrapper.ManifestWrapper
import com.pqixing.modularization.wrapper.XmlWrapper
import org.gradle.api.Project

import java.text.SimpleDateFormat
/**
 * Created by pqixing on 17-12-7.
 */

class RunType extends BaseContainer {
    boolean asApp
    String app_icon
    String app_name
    String app_theme
    //版本号
    Integer versionCode
    //版本名称
    String versionName

    //配置appId
    String applicationId

    String applicationName

    RunType(String name) {
        super(name)
    }

    @Override
    void onCreate(Project project) {
        super.onCreate(project)
        if (name == Keys.DEFAULT) {
            asApp = true
            app_name = wrapper.artifactId
            app_theme = "@android:style/Theme.Light.NoTitleBar"
            versionName = new SimpleDateFormat("MM.dd.HH.mm").format(new Date())
            versionCode = versionName.replace(".", "").toInteger()
            applicationId = "${wrapper.getExtends(BuildConfig.class).packageName}.${TextUtils.numOrLetter(wrapper.artifactId)}"
            applicationName = GlobalConfig.groupName.contains("dachen")?"com.dachen.common.DaChenApplication":"com.pqixing.moduleapi.VirtualApplication"
        }
    }

    void mergerData() {
        RunType defType = wrapper.getExtends(ModuleConfig.class).runTypes.getByName(Keys.DEFAULT)
        if (CheckUtils.isEmpty(app_icon)) app_icon = defType.app_icon
        if (CheckUtils.isEmpty(app_name)) app_name = defType.app_name
        if (CheckUtils.isEmpty(app_theme)) app_theme = defType.app_theme
        if (CheckUtils.isEmpty(applicationId)) applicationId = defType.applicationId
        if (CheckUtils.isEmpty(applicationName)) applicationName = defType.applicationName
        if (CheckUtils.isEmpty(versionName)) versionName = defType.versionName
        if (CheckUtils.isEmpty(versionCode)) versionCode = defType.versionCode
        asApp |= defType.asApp
    }

    @Override
    LinkedList<String> getOutFiles() {
        mergerData()
        if (wrapper.pluginName == Keys.NAME_APP || !asApp) return []//不独立运行，不生产缓存类文件

        BuildConfig buildConfig = wrapper.getExtends(BuildConfig.class)

        Runtype file = new Runtype()
        file.params += properties
        file.params += buildConfig.properties

        //输出Activity类
        FileUtils.write(FileUtils.getFileForClass(buildConfig.cacheJavaDir, "${buildConfig.javaPackage}.DefaultActivity"), file.lauchActivity)

        //清单文件解析
        ManifestWrapper manifestWrapper = new ManifestWrapper(FileUtils.read(new File(project.projectDir, "src/main/$Keys.MANIFEST")))

        manifestWrapper.appLabel = app_name
        manifestWrapper.appTheme = app_theme
        manifestWrapper.application = applicationName
        //添加启动Activity配置//保存新的清单文件到缓存目录
        manifestWrapper.updateReplace()
                .addComponent(new XmlWrapper(file.activityMeta).node)
                .writeTo(new File(buildConfig.cacheDir, Keys.MANIFEST))

        return [FileUtils.write(new File(buildConfig.cacheDir, "runType.gradle"), file.runTypeGradle)]
    }
}
