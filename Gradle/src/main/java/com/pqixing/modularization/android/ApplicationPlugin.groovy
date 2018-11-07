package com.pqixing.modularization.android


import com.pqixing.modularization.Keys
import org.gradle.api.Task

/**
 * Created by pqixing on 17-12-7.
 */

class ApplicationPlugin extends AndroidPlugin {
    @Override
    protected String getAndroidPlugin() {
        return Keys.NAME_APP
    }

    @Override
    Set<String> getIgnoreFields() {
        return null
    }

    @Override
    List<Class<? extends Task>> linkTask() {
        return null
    }
}
