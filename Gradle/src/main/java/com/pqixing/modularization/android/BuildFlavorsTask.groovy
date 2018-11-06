package com.pqixing.modularization.android

import com.alibaba.fastjson.JSON
import com.pqixing.modularization.Keys
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.common.BuildConfig
import com.pqixing.modularization.utils.FileUtils
import com.pqixing.modularization.utils.Print
import com.pqixing.tools.TextUtils

class BuildFlavorsTask extends BaseTask {
    String outDir
    List<String> builds = []

    BuildFlavorsTask() {
        this.dependsOn "DependentPrint"
        this.dependsOn "assembleRelease"
        project.tasks.each { t ->
            if (t.name == "assemblePreTestRelease") t.enabled = false
            else if (t.name.matches("assemble.*Release")) builds.add(t.name)
        }
    }

    @Override
    void start() {
        outDir = TextUtils.getSystemEnv(Keys.ENV_OUT_DIR)
    }

    @Override
    void runTask() {
        if (outDir == null) {
            Print.lnIde("BuildFlavorsTask=${JSON.toJSONString(builds)}")
            return
        }
        def out = new File(outDir)
        if (!out.exists()) out.mkdirs()
        def build = wrapper.getExtends(BuildConfig)


        FileUtils.copy(new File(build.outDir), new File(outDir))
        def apk = new File(outDir,"apk")
        apk.mkdirs()
        readApk(new File(project.buildDir, "outputs"), []).each { f ->
            f.renameTo(new File(apk, f.name))
        }
        Print.lnIde("BuildFlavorsTask=${JSON.toJSONString(builds)}")
    }

    List<File> readApk(File dir, List<File> container) {
        dir.eachFile { f ->
            if (f.isDirectory()) readApk(f, container)
            else if (f.name.endsWith(".apk")) container.add(f)
        }
        return container
    }


    @Override
    void end() {

    }
}
