package com.pqixing.modularization.utils;


import com.pqixing.annotation.RunActivity;
import com.pqixing.modularization.android.PqxVisitor;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kotlin.Pair;


/**
 * 生成路由class文件
 */
public class ClassModify extends ClassVisitor {
    PqxVisitor visitor;
    List<String> buildConfigClass;
    String pkg;

    public static byte[] transform(byte[] b, String pkg, PqxVisitor visitor, List<String> buildConfigClass) {
        final ClassReader classReader = new ClassReader(b);
        final ClassWriter cw = new ClassWriter(classReader,
                ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        ClassModify modify = new ClassModify(cw);
        modify.pkg = pkg;
        modify.visitor = visitor;
        modify.buildConfigClass = buildConfigClass;
        classReader.accept(modify, ClassReader.EXPAND_FRAMES);
//        com.pqixing.help.Tools.println("Start transform ->  ");
        return cw.toByteArray();
    }


    public ClassModify(ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {

        MethodVisitor v = super.visitMethod(access, name, desc, signature, exceptions);
        if (name.equals("loadInvokeClass"))
            v = new LoadTransformer(v, access, name, desc, pkg, visitor, buildConfigClass);
        return v;
    }
//
//    /**
//     * HashSet<String> :likes
//     * HashSet<String> :activitys
//     * @param bytes
//     * @param activitys
//     * @param likes
//     * @return
//     */
//    public static byte[] generateClass(final byte[] bytes, String pkg,Set<String> activitys, Set<String> likes) {
//        ClassWriter cw = new ClassWriter(0);
//        new ClassReader(bytes).accept(cw, ClassReader.EXPAND_FRAMES);
//        MethodVisitor mv;
//        // 生成class类标识
////        cw.visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, pkg, null, "java/lang/Object", null);
//        // 默认的构造函数<init>
//        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
//        mv.visitCode();
//        mv.visitVarInsn(Opcodes.ALOAD, 0);
//        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
//        mv.visitInsn(Opcodes.RETURN);
//        mv.visitMaxs(1, 1);
//
//        // 将扫描到的注解生成相对应的路由表 主要写在静态代码块中
//        mv = cw.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
//        mv.visitCode();
////        mv.visitTypeInsn(Opcodes.NEW, "java/util/HashSet");
////        mv.visitInsn(Opcodes.DUP);
////        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false);
////        mv.visitFieldInsn(Opcodes.PUTSTATIC, pkg, "map", "Ljava/util/HashMap;");
//
//        for (String key: activitys) {
//            mv.visitFieldInsn(Opcodes.GETSTATIC, pkg, "activitys", "Ljava/util/HashSet;");
//            mv.visitLdcInsn(key);
//            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/HashSet", "add", "(Ljava/lang/Object;)"+ Type.VOID_TYPE, false);
//            mv.visitInsn(Opcodes.POP);
//        }
//        for (String key: likes) {
//            mv.visitFieldInsn(Opcodes.GETSTATIC, pkg, "likes", "Ljava/util/HashSet;");
//            mv.visitLdcInsn(key);
//            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/HashSet", "add", "(Ljava/lang/Object;)"+ Type.VOID_TYPE, false);
//            mv.visitInsn(Opcodes.POP);
//        }
//        mv.visitInsn(Opcodes.RETURN);
//        mv.visitMaxs(Integer.MAX_VALUE, Integer.MAX_VALUE);
//        mv.visitEnd();
//        cw.visitEnd();
//
//        return cw.toByteArray();
//    }
}

class LoadTransformer extends GeneratorAdapter {
    PqxVisitor visitor;
    List<String> buildConfigClass;
    String pkg;

    LoadTransformer(MethodVisitor delegate, int access, String name, String desc, String pkg, PqxVisitor visitor, List<String> buildConfigClass) {
        super(Opcodes.ASM5, delegate, access, name, desc);
        this.pkg = pkg;
        this.visitor = visitor;
        this.buildConfigClass = buildConfigClass;
    }


    @Override
    public void visitInsn(int opcode) {
        if (opcode == Opcodes.RETURN) {
//
            List<Pair<String, HashSet<String>>> results = visitor.getResults();
            for (String key : results.get(0).getSecond()) {
                super.visitFieldInsn(Opcodes.GETSTATIC, pkg, "runActivity", "Ljava/util/HashSet;");
                super.visitLdcInsn(key.replace("/", "."));
                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/HashSet", "add", "(Ljava/lang/Object;)" + org.objectweb.asm.Type.BOOLEAN_TYPE, false);
                super.visitInsn(Opcodes.POP);
            }
            for (String key : results.get(1).getSecond()) {
                super.visitFieldInsn(Opcodes.GETSTATIC, pkg, "runModules", "Ljava/util/HashSet;");
                super.visitLdcInsn(key.replace("/", "."));
                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/HashSet", "add", "(Ljava/lang/Object;)" + org.objectweb.asm.Type.BOOLEAN_TYPE, false);
                super.visitInsn(Opcodes.POP);
            }
            for (String key : results.get(2).getSecond()) {
                super.visitFieldInsn(Opcodes.GETSTATIC, pkg, "routeActivity", "Ljava/util/HashSet;");
                super.visitLdcInsn(key.replace("/", "."));
                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/HashSet", "add", "(Ljava/lang/Object;)" + org.objectweb.asm.Type.BOOLEAN_TYPE, false);
                super.visitInsn(Opcodes.POP);
            }
            for (String key : results.get(3).getSecond()) {
                super.visitFieldInsn(Opcodes.GETSTATIC, pkg, "routeFragment", "Ljava/util/HashSet;");
                super.visitLdcInsn(key.replace("/", "."));
                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/HashSet", "add", "(Ljava/lang/Object;)" + org.objectweb.asm.Type.BOOLEAN_TYPE, false);
                super.visitInsn(Opcodes.POP);
            }
            for (String key : results.get(4).getSecond()) {
                super.visitFieldInsn(Opcodes.GETSTATIC, pkg, "routeServers", "Ljava/util/HashSet;");
                super.visitLdcInsn(key.replace("/", "."));
                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/HashSet", "add", "(Ljava/lang/Object;)" + org.objectweb.asm.Type.BOOLEAN_TYPE, false);
                super.visitInsn(Opcodes.POP);
            }
            for (String key : buildConfigClass) {
                super.visitFieldInsn(Opcodes.GETSTATIC, pkg, "buildConfigs", "Ljava/util/HashSet;");
                super.visitLdcInsn(key.replace("/", "."));
                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/HashSet", "add", "(Ljava/lang/Object;)" + org.objectweb.asm.Type.BOOLEAN_TYPE, false);
                super.visitInsn(Opcodes.POP);
            }

            super.visitFieldInsn(Opcodes.GETSTATIC, pkg, "configs", "Ljava/util/ArrayList;");
            super.visitLdcInsn(System.currentTimeMillis()+"");
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/ArrayList", "add", "(Ljava/lang/Object;)" + org.objectweb.asm.Type.BOOLEAN_TYPE, false);
            super.visitInsn(Opcodes.POP);

        }
        super.visitInsn(opcode);
    }
}