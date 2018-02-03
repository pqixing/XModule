package com.pqixing.modularization.git

import com.pqixing.modularization.base.BaseExtension
import com.pqixing.modularization.base.BasePlugin

/**
 * Created by pqixing on 17-12-7.
 */

class GitProject extends BaseExtension {
    /**
     * git工程名称,指目录名称
     */
    String name
    /**
     * 介绍
     */
    String introduce
    /**
     * git地址
     */
    String gitUrl
    /**
     * 包含的子项目,默认格式  name###introduce
     */
    List<String> submodules = []


    GitProject() {
        super(BasePlugin.rootProject)
    }
}
