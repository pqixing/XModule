package com.pqixing.regester

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import com.pqixing.regester.utils.ClassModify
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

class RegisterTransform(val filters: Set<String>) : Transform() {
    override fun getName(): String {
        return "qixing"
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
        val outputProvider = transformInvocation.outputProvider;
        val activitys = mutableSetOf<String>()
        val applikes = mutableSetOf<String>()
        val buildConfigClass = mutableListOf<String>();
        var targetInjectJar: JarInput? = null
        transformInvocation.inputs.forEach { input ->
            input.directoryInputs.forEach { dir ->
                handleDir(dir.file, activitys, applikes,buildConfigClass)
                //生成输出路径
                val dest = outputProvider.getContentLocation(dir.name, dir.contentTypes, dir.scopes, Format.DIRECTORY)
                FileUtils.copyDirectory(dir.file, dest)
            }
            input.jarInputs.forEach { jar ->
                if (jar.name.startsWith("com.pqixing.android:annotation:")) {
                    targetInjectJar = jar
                    System.out.println("find target jar -> ${jar.name}")
                } else {
                    if ((filters.isEmpty() || filters.find { jar.name.contains(it) } != null || jar.name.startsWith(":")
                                    /**本地工程**/
                                    ) && jar.file.absolutePath.endsWith(".jar")) {
                        handleJar(jar, activitys, applikes)
                    }// else System.out.println("UnHandle jar ->${jar.name}")
                    val dest = getDestFile(outputProvider, jar)
                    FileUtils.copyFile(jar.file, dest)
                }
            }
        }
        injectCode(targetInjectJar, outputProvider, activitys, applikes,buildConfigClass)
        System.out.println("$name transform end , count -> ${System.currentTimeMillis() - start}")
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

    private fun injectCode(jarInput: JarInput?, outputProvider: TransformOutputProvider, activitys: MutableSet<String>, applikes: MutableSet<String>, buildConfigClass: MutableList<String>) {
        jarInput ?: return
        //生成输出路径
        val dest = getDestFile(outputProvider, jarInput)
        val jarOutputStream = JarOutputStream(FileOutputStream(dest))
        val file = JarFile(jarInput.file)
        val enumeration = file.entries()
        System.out.println("injectCode start ->$activitys -> $applikes")

        while (enumeration.hasMoreElements()) {
            val jarEntry = enumeration.nextElement()
            val entryName = jarEntry.name
            jarOutputStream.putNextEntry(JarEntry(entryName))
            val stream = file.getInputStream(ZipEntry(entryName))
            val sourceClassBytes = IOUtils.toByteArray(stream)
            if (entryName == "com/pqixing/annotation/QLaunchManager.class") {
                val bytes = ClassModify.transform(sourceClassBytes, "com/pqixing/annotation/QLaunchManager", activitys, applikes,buildConfigClass)
                jarOutputStream.write(bytes)
            } else jarOutputStream.write(sourceClassBytes)
            stream.close()
            jarOutputStream.closeEntry()
        }
        jarOutputStream.close()
        file.close()
        System.out.println("injectCode end")

    }

    private fun handleDir(dir: File, activitys: MutableSet<String>, applikes: MutableSet<String>, buildConfigClass: MutableList<String>) {
        dir.listFiles().forEach { f ->
            if (f.isDirectory) handleDir(f, activitys, applikes, buildConfigClass)
            else if (f.isFile && f.name.endsWith(".class") && !f.absolutePath.contains("$")) {
                val path = f.absolutePath
                val className = path.substring(path.indexOf("/classes/") + 9, path.length - 6)
                if(className.endsWith("/BuildConfig")) buildConfigClass.add(className.replace("/","."))
                checkStream(f.inputStream(), className, activitys, applikes);
            }
        }
    }


    /**
     * 读取Activitys 和Likes
     */
    private fun handleJar(jar: JarInput, activitys: MutableSet<String>, applikes: MutableSet<String>) {
        val file = JarFile(jar.file)
        val enumeration = file.entries()
        while (enumeration.hasMoreElements()) {
            val jarEntry = enumeration.nextElement()
            val entryName = jarEntry.name
            if (!entryName.endsWith(".class") || entryName.contains("&")) continue
            val stream = file.getInputStream(ZipEntry(entryName))
            checkStream(stream, entryName.substring(0, entryName.length - 6), activitys, applikes)
        }
        file.close()
    }

    private fun checkStream(stream: InputStream, className: String, activitys: MutableSet<String>, applikes: MutableSet<String>) {
        val sourceClassBytes = IOUtils.toByteArray(stream)
        stream.close()

        val classReader = ClassReader(sourceClassBytes)
        val classVisitor = RegesterVisitor(className, activitys, applikes, Opcodes.ASM5, null)
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
    }
}
