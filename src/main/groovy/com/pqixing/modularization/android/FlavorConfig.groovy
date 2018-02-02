package com.pqixing.modularization.android

import com.pqixing.modularization.base.BaseExtension
import org.gradle.api.Project;

/**
 * Created by pqixing on 18-2-2.
 */

public class FlavorConfig extends BaseExtension {
    boolean flavorEnable = false
    FlavorConfig(Project project) {
        super(project)
    }

    @Override
    LinkedList<String> getOutFiles() {
        return null
    }
}
