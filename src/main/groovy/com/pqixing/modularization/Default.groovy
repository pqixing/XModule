package com.pqixing.modularization

import java.util.regex.Pattern

class Default {
    static final keyPattern = Pattern.compile("#\\d?\\{(?s).*?}")

    static final taskGroup = "modularization"

    static final uploadKey = "releaseUpload"
    static final groupName ="com.dachen"
    static final maven_url_test = "http://192.168.3.7:9527/nexus/content/repositories/androidtest/"
    static final maven_url_release = "http://192.168.3.7:9527/nexus/content/repositories/android/"
    static final maven_url_debug = "http://192.168.3.7:9527/nexus/content/repositories/androidsnap/"

    static final maven_user = "admin"
    static final maven_password = "admin123"

    static final defaultImplRepo = []

}