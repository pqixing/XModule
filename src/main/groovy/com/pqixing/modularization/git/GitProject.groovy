package com.pqixing.modularization.git

import com.pqixing.modularization.base.BaseExtension
import com.pqixing.modularization.base.BasePlugin

/**
 * Created by pqixing on 17-12-7.
 */

class GitProject extends BaseExtension {
    /**
     * git地址
     */
    String gitUrl
    /**
     * 包含的子项目
     */
    List<Map<String,String>> childs = []


    GitProject() {
        super(BasePlugin.rootProject)
    }
}
