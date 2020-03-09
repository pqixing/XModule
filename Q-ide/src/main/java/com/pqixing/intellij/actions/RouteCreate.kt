package com.pqixing.intellij.actions

import com.dachen.creator.utils.AndroidUtils
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.*
import com.pqixing.tools.TextUtils
import java.io.IOException

open class RouteCreate : AnAction() {
    lateinit var project: Project
    override fun actionPerformed(e: AnActionEvent) {
        project = e.getData(PlatformDataKeys.PROJECT) ?: return
        e.getData(PlatformDataKeys.PSI_FILE)?.children?.filter { it is PsiClass && !classFilter(it) }
                ?.forEach { p ->
                    WriteCommandAction.runWriteCommandAction(project) {
                        writeProxyClass(p as PsiClass)
                    }
                }

    }

    private fun classFilter(clazz: PsiClass): Boolean {
        if (clazz.hasModifierProperty("public")) {
            Messages.showWarningDialog("Class can not be public", "Warning")
            return true
        }
        if (!clazz.hasModifierProperty("final")) {
            Messages.showWarningDialog("Class must be final", "Warning")
            return true
        }
        return false
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.getData(PlatformDataKeys.EDITOR) != null
    }


    fun writeProxyClass(clazz: PsiClass) {
        val className = clazz.name + ".java"
        val pkgDir = AndroidUtils.getPackageByName(clazz, className, "proxy")
        val virtualFile = pkgDir?.findChild(className)
        if (virtualFile != null) try {
            // 如果存在就删除
            virtualFile.delete(null)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val initFile = PsiFileFactory.getInstance(project).createFileFromText(
                className, JavaFileType.INSTANCE, generatorCode(clazz))
        // 加到包目录下
        PsiManager.getInstance(project).findDirectory(pkgDir!!)!!.add(initFile)
    }

    fun generatorCode(clazz: PsiClass): String {
        val importList = arrayOf("android.os.Bundle", "android.content.Intent", "android.app.Activity", "android.content.Context", "android.os.Parcelable", "java.io.Serializable", "android.util.Size")

        val importRex = Regex(" *import .*;.*")
        val fullClassName: String = clazz.qualifiedName ?: ""
        val pkg = fullClassName.substring(0, fullClassName.lastIndexOf("."))
        val className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1, fullClassName.length)
        val code = StringBuilder()
                .append("package $pkg.proxy;\n")
                .append(clazz.containingFile.text.lines().filter { importRex.matches(it) }.joinToString("\n"))
                .append(importList.joinToString("\n") { "import $it;" })
                .append("\npublic final class $className {\n")
        val fullPath = "${clazz.qualifiedName}.${className}"
        for (f in clazz.allFields) when {
            f.name.startsWith("Activity_") -> {
                gnCode(code, fullPath, f)
                code.append("public void startActivity(Context context,Class clazz,int requestCode){if(clazz==null){ clazz = com.pqixing.annotation.AnnotationInfo.findClassByPath(PATH);}Intent intent = new Intent(context, clazz);intent.putExtras(bundle);if (context instanceof Activity) { ((Activity) context).startActivityForResult(intent, requestCode); } else { intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);context.startActivity(intent); }}")
                code.append("public void startActivity(Context context,int requestCode){startActivity(context,com.pqixing.annotation.AnnotationInfo.findClassByPath(PATH),requestCode);}}\n")
            }
            f.name.startsWith("Fragment_") -> {
                gnCode(code, fullPath, f)
                code.append("public Object findFragment(){return com.pqixing.annotation.AnnotationInfo.findObjectByPath(PATH);}}\n")

            }
            f.name.startsWith("Server_") -> gnServers(code, fullPath, f)
        }

        code.append("}")
        return code.toString()
    }

    private fun gnServers(code: StringBuilder, fullPath: String, f: PsiField) {
        val clazzName = f.name

        val tartClazz = f.children.find { it is PsiClassObjectAccessExpression }?.text?.replace(".class", "")
        code.append("\npublic static class $clazzName{\n")
                .append("public static final String PATH=\"$fullPath.${f.name}\";\n")
                .append("public static  $tartClazz s = null;")
        code.append("public static $tartClazz getServers(boolean keep){ $tartClazz o = s==null?($tartClazz)com.pqixing.annotation.AnnotationInfo.findObjectByPath(PATH):s;s = keep?o:null;return o;}}\n")
    }

    private fun gnCode(code: StringBuilder, fullPath: String, f: PsiField) {
        val clazzName = f.name
        code.append("\npublic static class $clazzName{\n")
                .append("public static final String PATH=\"$fullPath.${f.name}\";\n")
                .append("public final Bundle bundle = new Bundle();")
                .append("public static $clazzName with(Bundle bundle){$clazzName a = new $clazzName();a.bundle.putAll(bundle);return a;};     ")
                .append("public static $clazzName with(){$clazzName a = new $clazzName();return a;};     ")
                .append("private  $clazzName(){};\n")

        f.children.find { it is PsiLiteralExpression }
                ?.text?.replace("\"", "")
                ?.split(",")?.forEach {
                    val s = it.split(":")
                    if (s.size >= 2) code.append("public ${s[1]} get${TextUtils.firstUp(s[0])}(){return bundle.get${TextUtils.firstUp(s[1])}(\"${s[0]}\");}")
                            .append("public $clazzName set${TextUtils.firstUp(s[0])}(${s[1]} ${s[0]}){bundle.put${TextUtils.firstUp(s[1])}(\"${s[0]}\",${s[0]});return this;}\n")
                }


    }
}