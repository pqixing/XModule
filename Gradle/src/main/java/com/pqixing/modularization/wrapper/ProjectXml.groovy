package com.pqixing.modularization.wrapper

import com.pqixing.modularization.manager.GitProject
import com.pqixing.tools.CheckUtils;

public class ProjectXml {

    public static void parse(String txt, HashMap<String, GitProject> projects) {
        def gitNode = new XmlWrapper(txt).node
        String baseUrl = gitNode.@baseUrl

        gitNode.project.each { Node p ->
            String rootName = p.@name
            String introduce = p.@introduce

            //该工程的git地址
            String gitUrl = p.@url
            if (CheckUtils.isEmpty(gitUrl)) gitUrl = "$baseUrl/${rootName}.git"

            def children = p.submodule
            if (children.size() > 0) childens.each { Node s ->
                String name = s.@name
                introduce = s.@introduce
                addProject(projects, name, gitUrl, introduce, rootName)
            } else {
                def name = rootName
                addProject(projects, name, gitUrl, introduce, rootName)
            }
        }
    }

    private static final void addProject(HashMap<String, GitProject> projects, String name, String gitUrl, String introduce, String rootName) {
        def project = new GitProject(name, gitUrl, introduce, rootName)
        projects.put(name, project)
    }
}
