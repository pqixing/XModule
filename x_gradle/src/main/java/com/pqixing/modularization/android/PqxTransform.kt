package com.pqixing.modularization.android

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import com.pqixing.Tools
import com.pqixing.annotation.*
import com.pqixing.modularization.utils.ClassModify
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

class PqxTransform(val filters: Set<String> = emptySet()) : Transform() {
    override fun getName(): String {
        return "pqx"
    }

    override fun getInputTypes(): Set<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes() = TransformManager.SCOPE_FULL_PROJECT!!

    override fun isIncremental(): Boolean {
        return false
    }

    override fun transform(transformInvocation: TransformInvocation) {
        val start = System.currentTimeMillis()
//        Tools.println("$name transform start  ")
        val outputProvider = transformInvocation.outputProvider;
        val buildConfigClass = mutableListOf<String>();
        var targetInjectJar: JarInput? = null
        val visitor = PqxVisitor()
        transformInvocation.inputs.forEach { input -> input.jarInputs.forEach { jar -> if (jar.name.startsWith(PqxVisitor.TART_INJECT_JAR)) targetInjectJar = jar } }

        if (targetInjectJar != null) transformInvocation.inputs.forEach { input ->
            input.directoryInputs.forEach { dir ->
                handleDir(dir.file.absolutePath.length + 1, dir.file, visitor, buildConfigClass)
                //生成输出路径
                val dest = outputProvider.getContentLocation(dir.name, dir.contentTypes, dir.scopes, Format.DIRECTORY)
                FileUtils.copyDirectory(dir.file, dest)
            }
            input.jarInputs.forEach { jar ->
                if (!jar.name.startsWith(PqxVisitor.TART_INJECT_JAR)) {
                    /**本地工程**/
                    if ((filters.isEmpty()
                                    || filters.find { jar.name.contains(it) } != null
                                    || jar.name.startsWith(":"))
                            && jar.file.absolutePath.endsWith(".jar")) {
                        handleJar(jar, visitor)
                    }// else com.pqixing.Tools.println("UnHandle jar ->${jar.name}")
                    val dest = getDestFile(outputProvider, jar)
                    FileUtils.copyFile(jar.file, dest)
                }
            }
        }
        injectCode(targetInjectJar, outputProvider, visitor, buildConfigClass)
        Tools.println("$name transform target ->${targetInjectJar?.name} , count -> ${System.currentTimeMillis() - start} -> ${visitor.results}")
    }

    private fun getDestFile(outputProvider: TransformOutputProvider, jarInput: JarInput): File {
        var jarName = jarInput.name
        var md5Name = DigestUtils.md5Hex(jarInput.file.absolutePath)
        if (jarName.endsWith(".jar")) {
            jarName = jarName.substring(0, jarName.length - 4)
        }
        //生成输出路径
        return outputProvider.getContentLocation(jarName + md5Name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
    }

    private fun injectCode(jarInput: JarInput?, outputProvider: TransformOutputProvider, visitor: PqxVisitor, buildConfigClass: MutableList<String>) {
        jarInput ?: return
        //生成输出路径
        val dest = getDestFile(outputProvider, jarInput)
        val jarOutputStream = JarOutputStream(FileOutputStream(dest))
        val file = JarFile(jarInput.file)
        val enumeration = file.entries()
//        com.pqixing.Tools.println("injectCode start ->$activitys -> $applikes")

        while (enumeration.hasMoreElements()) {
            val jarEntry = enumeration.nextElement()
            val entryName = jarEntry.name
            jarOutputStream.putNextEntry(JarEntry(entryName))
            val stream = file.getInputStream(ZipEntry(entryName))
            val sourceClassBytes = IOUtils.toByteArray(stream)
            if (entryName == "${PqxVisitor.TART_INJECT_CLASS}.class") {
                val bytes = ClassModify.transform(sourceClassBytes, PqxVisitor.TART_INJECT_CLASS, visitor, buildConfigClass)
                jarOutputStream.write(bytes)
            } else jarOutputStream.write(sourceClassBytes)
            stream.close()
            jarOutputStream.closeEntry()
        }
        jarOutputStream.close()
        file.close()
//        com.pqixing.Tools.println("injectCode end")

    }

    private fun handleDir(classStart: Int, dir: File, visitor: PqxVisitor, buildConfigClass: MutableList<String>) {

        dir.listFiles()?.forEach { f ->
            if (f.isDirectory) handleDir(classStart, f, visitor, buildConfigClass)
            else if (f.isFile && f.name.endsWith(".class") && !f.absolutePath.contains("$")) {
                val path = f.absolutePath
                val className = path.substring(classStart, path.length - 6)
                if (className.endsWith("/BuildConfig")) buildConfigClass.add(className.replace("/", "."))
                checkStream(f.inputStream(), className, visitor);
            }
        }
    }


    /**
     * 读取Activitys 和Likes
     */
    private fun handleJar(jar: JarInput, visitor: PqxVisitor) {
        val file = JarFile(jar.file)
        val enumeration = file.entries()
        while (enumeration.hasMoreElements()) {
            val jarEntry = enumeration.nextElement()
            val entryName = jarEntry.name
            if (!entryName.endsWith(".class") || entryName.contains("&")) continue
            val stream = file.getInputStream(ZipEntry(entryName))
            checkStream(stream, entryName.substring(0, entryName.length - 6), visitor)
        }
        file.close()
    }

    private fun checkStream(stream: InputStream, className: String, visitor: PqxVisitor) {
        val sourceClassBytes = IOUtils.toByteArray(stream)
        stream.close()

        val classReader = ClassReader(sourceClassBytes)
        visitor.className = className
        classReader.accept(visitor, ClassReader.EXPAND_FRAMES)
    }
}

open class PqxVisitor : ClassVisitor(Opcodes.ASM5, null) {
    val results: List<Pair<String, HashSet<String>>> = FILTERS.map { it to HashSet<String>() }
    var className = ""
    override fun visitOuterClass(owner: String, name: String?, desc: String?) {
        className = owner
    }

    override fun visitAnnotation(desc: String?, visible: Boolean): AnnotationVisitor? {
        if (visible) {
            results.find { it.first == desc }?.second?.add(className)
        }
        return super.visitAnnotation(desc, visible)

    }

    companion object {
        val TART_INJECT_JAR = "com.pqixing.gradle:annotation:"
        val TART_INJECT_CLASS = AnnotationInfo::class.java.name.replace(".", "/")

        private var FILTERS = arrayOf<String>(
                RunActivity::class.java.name
                , RunModule::class.java.name
                , RouterActivity::class.java.name
                , RouterFragment::class.java.name
                , RouteSevers::class.java.name).map { "L" + it.replace(".", "/") + ";" }
    }

}
