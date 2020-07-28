package com.pqixing.modularization.utils

import org.junit.Test
import java.io.File

class GitUtilsTest {

    @Test
    fun testClone() {

        val clone = GitUtils.clone("git@gitlab.gz.cvte.cn:robot_application/robot_android/basic.git", File("/Users/pqx/Desktop/codetest"),"master")
       assert(clone!=null)
    }
}