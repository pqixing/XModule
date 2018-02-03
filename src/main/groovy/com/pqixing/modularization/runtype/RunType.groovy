package com.pqixing.modularization.runtype

import auto.Runtype
import com.pqixing.modularization.Keys
import com.pqixing.modularization.ModuleConfig
import com.pqixing.modularization.base.BaseContainer
import com.pqixing.modularization.configs.BuildConfig
import com.pqixing.modularization.utils.CheckUtils
import com.pqixing.modularization.utils.FileUtils
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
    String versionCode
    //版本名称
    String versionName

    //配置appId
    String applicationId
    boolean afterLogin

    RunType(String name) {
        super(name)
    }

    @Override
    void onCreate(Project project) {
        super.onCreate(project)
        asApp = true
        afterLogin = false
        app_name = wrapper.artifactId
        app_theme = "@android:style/Theme.Light.NoTitleBar"
        versionName = new SimpleDateFormat("MM.dd.HH.mm").format(new Date())
        versionCode = versionName.replace(".", "")
    }

    void mergerData() {
        RunType defType = wrapper.getExtends(ModuleConfig.class).runTypes.getByName(Keys.DEFAULT)
        if (CheckUtils.isEmpty(app_icon)) app_icon = defType.app_icon
        if (CheckUtils.isEmpty(app_name)) app_name = defType.app_name
        if (CheckUtils.isEmpty(app_theme)) app_theme = defType.app_theme
        if (CheckUtils.isEmpty(applicationId)) applicationId = defType.applicationId
        afterLogin &= defType.afterLogin
        asApp &= defType.asApp
    }

    @Override
    LinkedList<String> getOutFiles() {
        mergerData()
        if (wrapper.pluginName == Keys.NAME_APP || !asApp) return []//不独立运行，不生产缓存类文件

        BuildConfig buildConfig = wrapper.getExtends(BuildConfig.class)
        if (CheckUtils.isEmpty(applicationId)) {
            applicationId = "${buildConfig.packageName}.$wrapper.artifactId"
        }

        Runtype file = Runtype.with(properties)
        if (!afterLogin) file.params.remove("afterLogin")
        file += [buildConfig.properties]

        String applicationPkg = "${buildConfig.javaPackage}.ModularizationApp"

        //输出Application
        FileUtils.write(FileUtils.getFileForClass(buildConfig.cacheJavaDir, applicationPkg), file.modularizationApp)
        //输出登录回调
        FileUtils.write(FileUtils.getFileForClass(buildConfig.cacheJavaDir, "${buildConfig.javaPackage}.LoginCallBack"), file.loginCallBack)
        //输出Activity类
        FileUtils.write(FileUtils.getFileForClass(buildConfig.cacheJavaDir, "${buildConfig.javaPackage}.DefaultActivity"), file.lauchActivity)

        //清单文件解析
        ManifestWrapper manifestWrapper = new ManifestWrapper(FileUtils.read(new File(project.path, "src/main/$Keys.MANIFEST")))

        manifestWrapper.appLabel = app_name
        manifestWrapper.appTheme = app_theme
        manifestWrapper.application = applicationPkg
        //添加启动Activity配置//保存新的清单文件到缓存目录
        manifestWrapper.updateReplace()
                .addComponent(XmlWrapper.parse(file.activityMeta))
                .writeTo(new File(buildConfig.cacheDir, Keys.MANIFEST))

        return [FileUtils.write(new File(buildConfig.cacheDir, "runType.gradle"), file.runTypeGradle)]
    }
}
