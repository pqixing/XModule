package com.pqixing.modularization.manager;

import org.eclipse.jgit.api.Git;

/**
 * Created by pqixing on 17-12-7.
 */

public class GitProject {
    /**
     * git工程名称,指目录名称
     */
    public String name;

    /**
     * git地址
     */
    public String gitUrl;

    /**
     *
     */
    public String introduce;
    /**
     * 根目录的名称
     */
    public String rootName;

    public GitInfo gitInfo;


    public GitProject(String name, String gitUrl, String introduce, String rootName) {
        this.name = name;
        this.gitUrl = gitUrl;
        this.introduce = introduce;
        this.rootName = rootName;
    }

    public GitProject() {
    }

    public void loadGitInfo(Git git) {

    }

    public static class GitInfo {

    }
}
