package com.pqixing.modularization.analysis
//package com.pqixing.modularization.analysis
//
//import com.pqixing.modularization.android.dps.Dependencies
//import org.gradle.api.tasks.TaskAction
///**
// * Created by pqixing on 17-12-20.
// * 对比不同仓库中的依赖，生成快速合并提交到仓库的脚本
// */
//
//public class MavenMergerTask extends DpendentAnalysisTask {
//
//    @TaskAction
//    void run() {
//        init()
//        StringBuilder sb = new StringBuilder("$project.name 模块 release环境 与test环境  分支依赖关系对比对比 \n")
//                .append("待更新  ").append("层级   ").append("    release版本        ").append("         test版本        ").append("             依赖模块名称\n")
//        generatorCompareFile(sb,config.mavenTypes.release)
//    }
//
//
//    @Override
//    boolean isCheck(Dependencies.DpItem item, Dependencies.DpItem compareItem) {
//        return item.lastUpdate > compareItem.lastUpdate
//    }
//}
