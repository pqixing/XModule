package com.pqixing.modularization

import java.util.regex.Pattern

class Default {
    static final keyPattern = Pattern.compile("#\\d?\\{(?s).*?}")

    static final taskGroup = "modularization"

    static final uploadKey = "releaseUpload"
    static final groupName ="com.dachen"
    static final maven_url_test = "uri('/home/pqixing/testRepo')"
    static final maven_url_release = "uri('/home/pqixing/releaseRepo')"
    static final maven_url_debug = "uri('/home/pqixing/debugRepo')"

    static final maven_user = "admin"
    static final maven_password = "admin123"

    static final defaultImplRepo = []
}