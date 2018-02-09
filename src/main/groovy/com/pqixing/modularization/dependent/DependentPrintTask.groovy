package com.pqixing.modularization.dependent

import com.alibaba.fastjson.JSON
import com.pqixing.modularization.Keys
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.common.BuildConfig
import com.pqixing.modularization.utils.FileUtils
import com.pqixing.modularization.utils.TextUtils

/**
 * Created by pqixing on 17-12-18.
 * 输出打印依赖
 */

class DependentPrintTask extends BaseTask {
    File outDir
    BuildConfig buildConfig
    File androidDp

    DependentPrintTask() {
        buildConfig = wrapper.getExtends(BuildConfig.class)
        outDir = new File(buildConfig.outDir, Keys.DIR_DEPENDENT)
    }

    @Override
    void start() {
        androidDp = new File(outDir, Keys.FILE_ANDROID_DP)
        project.task(TextUtils.onlyName, type: org.gradle.api.tasks.diagnostics.DependencyReportTask) {
            outputFile = androidDp
        }.execute()
    }

    @Override
    void runTask() {
        def strList = new LinkedList<String>()
        androidDp.eachLine {
            if (it.startsWith("No dependencies")) {
                strList.removeLast()
                strList.removeLast()
            } else {
                strList.add(it + "\n")
            }
        }
        FileUtils.write(androidDp, "Inner Dp :\n ${JSON.toJSONString(wrapper.getExtends(Dependencies).modules, true)}\n Android Dp : \n$strList")
    }

    @Override
    void end() {

    }
}
