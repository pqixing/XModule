package com.pqixing.modularization.plugins

import com.pqixing.modularization.Keys
import com.pqixing.modularization.analysis.BranchMergerTask
import com.pqixing.modularization.analysis.MavenMergerTask
import com.pqixing.modularization.base.BasePlugin
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.configs.BuildConfig
import com.pqixing.modularization.dependent.DependentPrintTask
import com.pqixing.modularization.dependent.VersionsUpdateTask
import com.pqixing.modularization.git.DocSyncTask
import com.pqixing.modularization.git.GitConfig
import com.pqixing.modularization.models.ModuleConfig
import com.pqixing.modularization.runtype.RunType
import com.pqixing.modularization.utils.FileUtils
import com.pqixing.modularization.utils.Print
import org.gradle.api.Project
/**
 * Created by pqixing on 17-12-7.
 */

abstract class AndroidPlugin extends BasePlugin {
    abstract String pluginType()

    @Override
    void apply(Project project) {
        super.apply(project)
        ModuleConfig moduleConfig = new ModuleConfig(project, project.container(RunType)
                , project.container(MavenType), pluginType())
        project.extensions.add(Keys.CONFIG_MODULE, moduleConfig)

        loadRemoteGradle()
        //添加直接使用的配置
        project.ext.support_v7 = { moduleConfig.androidConfig.support_v7 }
        project.ext.support_v4 = { moduleConfig.androidConfig.support_v4 }
        project.ext.printPros = { pro -> Print.lnPro(pro) }

        //允许配置全局使用的配置文件
        project.ext.endConfig = {
            loadLocalGradle()
            if (it instanceof Closure) it.call(moduleConfig)
            //更新依赖的版本信息
            BaseTask.task(project, VersionsUpdateTask.class).execute()

            //配置结束
            moduleConfig.onConfigEnd()
            moduleConfig.outFiles.findAll { it != null }.each { wrapper.apply from: it }
            project.afterEvaluate {
                moduleConfig.afterApplyAndroid()
            }
            //添加打印依赖的task
            BaseTask.task(project, DependentPrintTask.class)
            BaseTask.task(project, DocSyncTask.class)
            //添加分析的任务
            BaseTask.task(wrapper.project, wrapper.getExtends(GitConfig.class)
                    .branchName == "master" ? MavenMergerTask.class : BranchMergerTask.class)
        }
    }

    /**
     * 加载远程配置
     */
    void loadRemoteGradle() {
        String remotePath = wrapper.get(Keys.REMOTE_GRADLE)
        if (remotePath.startsWith(Keys.PREFIX_NET)) {
            File remoteFile = new File(BuildConfig.rootOutDir, Keys.REMOTE_GRADLE)
            remotePath = FileUtils.write(remoteFile, Net.get(remotePath, true))
        }
        wrapper.apply from: remotePath
    }
    /**
     * 加载本地配置
     */
    void loadLocalGradle() {
        File localConfig = project.file(Keys.LOCAL_GRADLE)
        if (!localConfig.exists()) FileUtils.write(localConfig, Keys.LOCAL_GRADLE_MOULD)
        wrapper.apply from: localConfig.path
        wrapper.apply from: new File(project.rootDir, Keys.FOCUS_GRADLE).path
    }


    Set<String> getIgnoreFields() {
        return [Keys.LOCAL_GRADLE]
    }
}
