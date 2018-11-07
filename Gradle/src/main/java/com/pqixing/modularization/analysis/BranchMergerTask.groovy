package com.pqixing.modularization.analysis
//package com.pqixing.modularization.analysis
//
//import com.pqixing.modularization.android.dps.Dependencies
//import org.gradle.api.tasks.TaskAction
///**
// * Created by pqixing on 17-12-20.
// * 检查分支上提交的过的依赖包,生成快速提交到主线的功能
// */
//
//class BranchMergerTask extends DpendentAnalysisTask {
//
//
//    @TaskAction
//    void run() {
//        init()
//        StringBuilder sb = new StringBuilder("$project.name 模块 master 与$project.branchName 分支依赖关系对比对比 \n")
//                .append("待更新  ").append("层级   ").append("    master版本        ").append("         分支版本        ").append("             依赖模块名称\n")
//        generatorCompareFile(sb,config.mavenTypes.test)
//    }
//
//    @Override
//    boolean isCheck(Dependencies.DpItem item, Dependencies.DpItem compareItem) {
//        return item.moduleName.contains("-b-")
//    }
//}
