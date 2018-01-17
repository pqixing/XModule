package com.pqixing.modularization.tasks

import com.pqixing.modularization.Default
import org.gradle.api.DefaultTask
/**
 * Created by pqixing on 17-12-20.
 * 基础的检测任务
 */

public class BaseCheckTask extends DefaultTask {
    BaseCheckTask(){
        group = Default.taskGroup
    }
}
