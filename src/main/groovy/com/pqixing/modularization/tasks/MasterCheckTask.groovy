package com.pqixing.modularization.tasks

import org.gradle.api.tasks.TaskAction
/**
 * Created by pqixing on 17-12-20.
 * 检测该功能中release仓库和test仓库的依赖的版本差异,生成快速提交代码到release环境的功能
 */

public class MasterCheckTask extends BaseCheckTask {

    @TaskAction
    void run() {
    }
}
