package com.pqixing.help

import java.util.*

/**
 * 头信息包裹类
 */
class MavenMetadata {
    var artifactId: String = ""

    var groupId: String = ""

    var release: String = ""

    val versions: LinkedList<String> = LinkedList()
    var lastUpdated: String = ""
}