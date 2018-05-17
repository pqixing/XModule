//package com.pqixing.modularization.git
//
//import auto.Android
//import com.pqixing.modularization.Keys
//import com.pqixing.modularization.base.BaseTask
//import com.pqixing.modularization.utils.FileUtils
//
///**
// * Created by pqixing on 17-12-20.
// * 将本地所有工程切换到master分支
// */
//
//class ModuReleaseTask extends BaseTask {
//    ModuReleaseTask() {
//        group = Keys.GROUP_OTHER
//    }
//
//    @Override
//    void start() {
//    }
//
//    @Override
//    void runTask() {
//        FileUtils.write(new File(project.rootDir, Keys.FOCUS_GRADLE), new Android().releaseGradle)
//    }
//
//    @Override
//    void end() {
//
//    }
//}
