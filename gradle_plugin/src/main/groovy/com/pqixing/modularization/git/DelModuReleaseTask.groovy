//package com.pqixing.modularization.git
//
//import com.pqixing.modularization.Keys
//import com.pqixing.modularization.base.BaseTask
//
///**
// * Created by pqixing on 17-12-20.
// * 将本地所有工程切换到master分支
// */
//
//class DelModuReleaseTask extends BaseTask {
//    DelModuReleaseTask() {
//        group = Keys.GROUP_OTHER
//    }
//
//    @Override
//    void start() {
//    }
//
//    @Override
//    void runTask() {
//        new File(project.rootDir, Keys.FOCUS_GRADLE).delete()
//    }
//
//    @Override
//    void end() {
//
//    }
//}
