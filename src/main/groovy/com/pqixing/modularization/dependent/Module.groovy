package com.pqixing.modularization.dependent

import com.pqixing.modularization.base.BaseExtension
import com.pqixing.modularization.base.BasePlugin
import com.pqixing.modularization.common.GlobalConfig
import com.pqixing.modularization.utils.CheckUtils
/**
 * Created by pqixing on 17-12-25.
 */

class Module extends BaseExtension {
    public static final String SCOP_COMPILE = "compile"
    public static final String SCOP_RUNTIME = "runtimeOnly"
    public static final String scop_compileonly = "compileOnly"
    public static final String SCOP_IMPL = "implementation"

    /**
     * 当前模块是否使用了本地依赖
     */
    boolean onLocalCompile
    String moduleName
    String artifactId
    /**
     * 依赖模式
     * runtimeOnly , compileOnly , implementation , compile
     */
    String scope = scop_runtime

    String groupId = GlobalConfig.groupName
    String version
    /**
     * 最后更新时间
     */
    long updateTime
    /**
     * 更新说明
     */
    String gitLog
    LinkedList<Map<String, String>> excludes = new LinkedList<>()
    /**
     * 依赖中的依赖树
     */
    Set<Module> modules = new HashSet<>()

    void excludeGroup(String[] groups) {
        groups.each {
            excludes += ["group": it]
        }
    }

    void excludeModule(String[] modules) {
        modules.each {
            excludes += ["module": it]
        }
    }

    void exclude(Map<String, String> exclude) {
        excludes += exclude
    }

    String getUpdateTimeStr() {
        return new Date(updateTime).toLocaleString()
    }


    Module() {
        super(BasePlugin.rootProject)
    }

    String getArtifactId() {
        if (CheckUtils.isEmpty(artifactId)) return moduleName
        return artifactId
    }

    @Override
    LinkedList<String> getOutFiles() {
        return null
    }
}
