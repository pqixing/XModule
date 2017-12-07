package com.pqixing.modularization.models;

/**
 * Created by pqixing on 17-12-7.
 */

class RunType extends BaseContainerExtension {
    boolean asApp = false
    String applicationLike = ""
    String launchActivity = ""
    String app_icon = ""
    String app_name = ""
    String app_theme = ""


    RunType(String name) {
        super(name)
    }
}
