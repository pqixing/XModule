package com.pqixing.modularization.android.dps

import com.pqixing.modularization.base.BaseExtension
import com.pqixing.modularization.utils.CheckUtils
import com.pqixing.tools.TextUtils
import org.gradle.api.Project

/**
 * Created by pqixing on 17-12-25.
 */

class Module extends BaseExtension {
    public static final String SCOP_API = "api"
    public static final String SCOP_RUNTIME = "runtimeOnly"
    public static final String SCOP_COMPILEONLY = "compileOnly"
    public static final String SCOP_IMPL = "implementation"

    //强制使用master分支
    boolean focusMaster = false
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
    String scope = SCOP_RUNTIME

    String version
    /**
     * 最后更新时间
     */
    long updateTime
    /**
     * 更新说明
     */
    String gitLog = ""
    LinkedList<Map<String, String>> excludes = new LinkedList<>()
    /**
     * 依赖中的依赖树
     */
    Set<Module> modules = new HashSet<>()

    Module(Project project) {
        super(project)
    }

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


    String getArtifactId() {
        String a = artifactId
        if (CheckUtils.isEmpty(a)) a = moduleName
        return TextUtils.removeLineAndMark(a)
    }

    @Override
    public String toString() {
        return "Module{" +
                "onLocalCompile=" + onLocalCompile +
                ", moduleName='" + moduleName + '\'' +
                ", artifactId='" + artifactId + '\'' +
                ", scope='" + scope + '\'' +
                ", groupId='" + groupId + '\'' +
                ", version='" + version + '\'' +
                ", updateTime=" + updateTime +
                ", gitLog='" + gitLog + '\'' +
                '}';
    }
}
