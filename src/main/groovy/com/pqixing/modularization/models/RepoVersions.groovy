package com.pqixing.modularization.models

import org.gradle.api.Project

/**
 * Created by pqixing on 17-12-25.
 */

class RepoVersions extends BaseExtension {

    LinkedList<String> configPaths
    Map<String, String> versions
    Project project

    void addPath(String path) {
        configPaths += path
    }

    void addVersion(Map<String, String> maps) {
        versions += maps
    }

    RepoVersions(Project project) {
        configPaths = new LinkedList<>()
        versions = new HashMap<>()
        this.project = project
    }


    @Override
    public LinkedList<String> generatorFiles() {
        return []
    }

    void endConfig() {
        def newVersions = new HashMap<String, String>()
        configPaths.each { path ->
            File f = new File(path)
            if (!f.exists()) return
            Properties p = new Properties()
            p.load(f.newInputStream())
            newVersions.putAll(p.toSpreadMap())
        }
        newVersions.putAll(versions)
        versions = newVersions
    }
}
