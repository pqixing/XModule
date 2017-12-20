package com.pqixing.modularization.tasks

import com.pqixing.modularization.Default;
import org.gradle.api.tasks.Copy;

/**
 * Created by pqixing on 17-12-20.
 * 同步文档的任务
 */

public class DocSyncTask extends Copy {
    DocSyncTask(){
        group = Default.taskGroup
    }
}
