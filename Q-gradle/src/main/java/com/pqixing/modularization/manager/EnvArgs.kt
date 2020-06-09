package com.pqixing.modularization.manager

import com.pqixing.help.MavenPom
import com.pqixing.help.XmlHelper
import com.pqixing.modularization.FileNames
import com.pqixing.modularization.maven.VersionManager
import com.pqixing.tools.FileUtils
import com.pqixing.tools.TextUtils
import org.gradle.api.Project
import java.io.File
import java.net.URL
import java.util.*

open class EnvArgs(val project: Project) {

    /**
     *
     */
    var templetBranch:String = "master"

    var rootDir:File = project.rootDir
    var  templetRoot: File =File(rootDir,"templet")
    var xmlFile:File = File(templetRoot,FileNames.PROJECT_XML)

    lateinit var codeRootDir:File

    //内存中只保留10跳
    var pomCache: LinkedList<Pair<String, MavenPom>> = LinkedList()

    fun load(args: ArgsExtends){
        codeRootDir = File(rootDir, args.config.codeRoot)

    }
}