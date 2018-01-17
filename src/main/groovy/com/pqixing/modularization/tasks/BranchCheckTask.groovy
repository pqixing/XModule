package com.pqixing.modularization.tasks

import com.alibaba.fastjson.JSON
import com.pqixing.modularization.models.Dependencies
import com.pqixing.modularization.utils.NormalUtils
import com.pqixing.modularization.utils.Print
import org.gradle.api.tasks.TaskAction
/**
 * Created by pqixing on 17-12-20.
 * 检查分支上提交的过的依赖包,生成快速提交到主线的功能
 */

public class BranchCheckTask extends BaseCheckTask {
    @TaskAction
    void run() {
        HashMap<String, String> lastBranchModule = new HashMap<String, String>()
        addLastBranchModule(lastBranchModule, lastDependencies)
        Print.ln(JSON.toJSONString(lastBranchModule,true))
    }

    void addLastBranchModule(HashMap<String, String> container, Collection<Dependencies.DpItem> dpItems) {
        dpItems.findAll {it.moduleName.contains("-b-")}.each { item ->
            String lastUpdate = container.get(item.moduleName)
            if (NormalUtils.isEmpty(lastUpdate) || lastUpdate.toLong() < item.lastUpdate.toLong())
                container.put(item.moduleName, item.lastUpdate)
            addLastBranchModule(container,item.dpItems)
        }
    }
}
