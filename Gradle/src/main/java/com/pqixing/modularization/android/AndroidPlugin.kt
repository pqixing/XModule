package com.pqixing.modularization.android


import com.pqixing.modularization.JGroovyHelper
import com.pqixing.modularization.Keys
import com.pqixing.modularization.android.dps.DpsExtends
import com.pqixing.modularization.android.dps.DpsManager
import com.pqixing.modularization.base.BasePlugin
import com.pqixing.modularization.iterface.IExtHelper
import org.gradle.api.Project

/**
 * Created by pqixing on 17-12-7.
 */

abstract class AndroidPlugin : BasePlugin() {

    protected abstract val androidPlugin: String

    override fun initBeforeApply() {
        //根据情况进行不同的Android插件依赖
        project.apply(mapOf("plugin" to androidPlugin))
    }

    override fun apply(project: Project) {
        super.apply(project)

        //创建配置读取
        val dpsExt = project.extensions.create(Keys.CONFIG_DPS, DpsExtends::class.java, project)
        val moduleConfig = CompatDps(project, dpsExt)
        project.extensions.add(Keys.CONFIG_MODULE, moduleConfig)

        val extHelper = JGroovyHelper.getImpl(IExtHelper::class.java)
        extHelper.setExtMethod(project, "endConfig", null)

        //在工程处理后，处理组件依赖
        project.afterEvaluate { DpsManager(this@AndroidPlugin).resolveDps(dpsExt) }
    }
}
