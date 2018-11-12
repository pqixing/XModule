package com.pqixing.help

import java.util.*

/**
 * 头信息包裹类
 */
class MavenMetadata(val groupUrl: String) {
    var artifactId: String = ""

    fun getPomUrl(version: String) = "$groupUrl/$artifactId/$version/$artifactId-$version.xml"


    var groupId: String = ""

    var release: String = ""

    val versions: LinkedList<String> = LinkedList()
    var lastUpdated: String = ""
}