package com.pqixing.modularization.tasks

import com.pqixing.modularization.models.Dependencies
import org.gradle.api.tasks.TaskAction
/**
 * Created by pqixing on 17-12-20.
 * 检测该功能中release仓库和test仓库的依赖的版本差异,生成快速提交代码到release环境的功能
 */

public class MasterCheckTask extends BaseCheckTask {

    @TaskAction
    void run() {
        init()
        StringBuilder sb = new StringBuilder("$project.name 模块 release环境 与test环境  分支依赖关系对比对比 \n")
                .append("待更新  ").append("层级   ").append("    release版本        ").append("         test版本        ").append("             依赖模块名称\n")
        generatorCompareFile(sb,config.mavenTypes.release)
    }


    @Override
    boolean isCheck(Dependencies.DpItem item, Dependencies.DpItem compareItem) {
        return item.lastUpdate > compareItem.lastUpdate
    }
}
