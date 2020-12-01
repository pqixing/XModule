package com.pqixing.modularization.maven

import org.apache.commons.codec.digest.DigestUtils
import java.text.SimpleDateFormat
import java.util.*

object VersionParse {
    val dateVersion = SimpleDateFormat("yyyy.MM.dd.HH.mm.ss")
    fun getVersion(target: String) = "${getHash(target)}.${dateVersion.format(Date())}"

    fun getGroupId(groupId: String, branch: String?) = (groupId + (branch?.let { "." + getHash(it) } ?: ""))

    fun getKey(groupId: String, branch: String, module: String, version: String) = "${getGroupId(groupId, branch)}:$module:$version"

    fun getHash(tag: String?) = tag?.let { DigestUtils.md5Hex(it) }?:""
}