package com.pqixing.modularization.tasks

import com.pqixing.modularization.Default
import org.gradle.api.DefaultTask;

/**
 * Created by pqixing on 17-12-18.
 */

public class UpdateVersionsTask extends DefaultTask {
    String outPath
    String mavenUrl
    String comileGroup
    HashSet<String> modules

    UpdateVersionsTask(){
        group = Default.taskGroup
        modules = new HashSet<>()
    }
}
