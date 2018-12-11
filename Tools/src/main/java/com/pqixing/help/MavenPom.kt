package com.pqixing.help

/**
 * pom信息信息包裹类
 */
class MavenPom {
    var artifactId: String = ""

    var groupId: String = ""

    var version: String = ""
    var packaging: String = ""
    var name: String = ""

    var allExclude: HashSet<String> = HashSet()
    var dependency: HashSet<String> = HashSet()
}