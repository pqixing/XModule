package com.pqixing.modularization.tasks

import com.pqixing.modularization.utils.Print
import org.gradle.api.tasks.TaskAction
/**
 * Created by pqixing on 17-12-20.
 * 检查分支上提交的过的依赖包,生成快速提交到主线的功能
 */

class BranchCheckTask extends BaseCheckTask {
    @TaskAction
    void run() {
        HashMap<String, LevelItem> sortMaps = new HashMap<String, LevelItem>()
        sortByLevel(lastDependencies,0,sortMaps)
        sortMaps.toSpreadMap().sort{it.value.level}.each {
        Print.ln("$it.key : $it.value.level realNma $it.value.name")
        }
    }

}
