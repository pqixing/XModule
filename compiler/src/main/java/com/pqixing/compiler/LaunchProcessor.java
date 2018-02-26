package com.pqixing.compiler;

import com.google.auto.service.AutoService;
import com.pqixing.annotation.LaunchActivity;
import com.pqixing.annotation.LaunchAppLike;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * 生成启动Activity列表
 * Created by pqixing 2017/12/22
 */
@AutoService(Processor.class)
public class LaunchProcessor extends AbstractProcessor {
    private Filer mFiler;
    private Messager messager;
    private String moduleName;
    private String groupName;


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mFiler = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        moduleName = processingEnv.getOptions().get("moduleName");
        groupName = processingEnv.getOptions().get("groupName");
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> supportTypes = new LinkedHashSet<>();
        supportTypes.add(LaunchActivity.class.getCanonicalName());
        supportTypes.add(LaunchAppLike.class.getCanonicalName());
        return supportTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> activitys = roundEnv.getElementsAnnotatedWith(LaunchActivity.class);
        Set<? extends Element> applikes = roundEnv.getElementsAnnotatedWith(LaunchAppLike.class);
        if (activitys.isEmpty() && applikes.isEmpty()) return false;
        messager.printMessage(Diagnostic.Kind.NOTE, "LaunchProcessor:-> activitys :" + activitys + "  applikes:" + applikes);

        String pkg = "auto." + groupName + "." + TextUtils.numOrLetter(moduleName);
        String javaName = TextUtils.className(moduleName + "Launch");
        StringBuilder classStr = new StringBuilder("package " + pkg + ";\n");
        classStr.append("public class " + javaName + "{ \n");
        addStrField(classStr, "LAUNCH_ACTIVITY", activitys);
        addStrField(classStr, "LAUNCH_APPLIKE", applikes);

        writeJava(mFiler, pkg + "." + javaName, classStr.append("\n}").toString());
        return true;
    }

    public static void addStrField(StringBuilder sb, String keyName, Set<? extends Element> targets) {
        sb.append("public static final String " + keyName + " = \"");
        for (Element e : targets) {
            sb.append(((TypeElement) e).getQualifiedName() + ",");
        }
        sb.append("\";\n");
    }

    public static void writeJava(Filer mFiler, String fullName, String classStr) {
        try {
            JavaFileObject sourceFile = mFiler.createSourceFile(fullName);
            sourceFile.delete();
            Writer writer = sourceFile.openWriter();
            writer.write(classStr);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
