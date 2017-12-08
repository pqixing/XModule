package com.pqixing.modularization

import java.util.regex.Pattern

class Default {
    static final keyPattern = Pattern.compile("#\\d?\\{(?s).*?}")

    static final taskGroup = "modularization"

    static final uploadKey = "releaseUpload"
    static final groupName ="com.dachen"
    static final maven_url_test = ""
    static final maven_url_release = ""
    static final maven_url_debug = ""

    static final maven_user = "admin"
    static final maven_password = "admin123"

    static final defaultImplRepo = []
}