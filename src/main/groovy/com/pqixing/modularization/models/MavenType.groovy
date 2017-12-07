package com.pqixing.modularization.models;

/**
 * Created by pqixing on 17-12-7.
 */

class MavenType extends BaseContainerExtension {
    boolean uploadEnable = false

    String pom_version = ""
    String url = ""
    String artifactId = ""
    String userName = "admin"
    String password = "admin123"
    String groupName =""
    String uploadKey = ""

    Map<String ,String > repoVersions

    MavenType(String name) {
        super(name)
    }
}
