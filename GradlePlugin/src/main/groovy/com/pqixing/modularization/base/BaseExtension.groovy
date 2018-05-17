package com.pqixing.modularization.base

import com.pqixing.modularization.wrapper.ProjectWrapper
import org.gradle.api.Project

/**
 * Created by pqixing on 17-12-7.
 */

abstract class BaseExtension {
    public Project project
    public ProjectWrapper wrapper
    BaseExtension(){
    }
    BaseExtension(Project project) {
        setProject(project)
    }

    void setProject(Project project) {
        this.project = project
        project?.ext?."${getClass().name}" = this//初始化赋值
        wrapper = ProjectWrapper.with(project)
    }
/**
 * 配置解析
 * @param closure
 */
    void configure(Closure closure) {
        closure.delegate = this
        closure.setResolveStrategy(Closure.DELEGATE_ONLY)
        closure(this)
    }

    LinkedList<String> getOutFiles() { [] }

}
