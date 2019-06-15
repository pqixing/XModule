package com.pqixing.aspect.utils;


import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.util.List;
import java.util.Set;


/**
 * 生成路由class文件
 */
public class ClassModify extends ClassVisitor {
    Set<String> activitys;
    Set<String> likes;
    List<String> buildConfigClass;
    String pkg;

    public static byte[] transform(byte[] b, String pkg, Set<String> activitys, Set<String> likes, List<String> buildConfigClass) {
        final ClassReader classReader = new ClassReader(b);
        final ClassWriter cw = new ClassWriter(classReader,
                ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        ClassModify modify = new ClassModify(cw);
        modify.pkg = pkg;
        modify.activitys = activitys;
        modify.likes = likes;
        modify.buildConfigClass = buildConfigClass;
        classReader.accept(modify, ClassReader.EXPAND_FRAMES);
//        System.out.println("Start transform ->  ");
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
            v = new LoadTransformer(v, access, name, desc, pkg, activitys, likes, buildConfigClass);
        return v;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
//        System.out.println("visitField  -> name = "+name +" desc ="+desc + "value = "+value );
        switch (name) {
            case "buildTimeStr":
                value = System.currentTimeMillis()+"";
                break;
            case "buildTime":
                value = System.currentTimeMillis();
                break;
            case "BUILD_CONFIG_NAME":
//                value = RegesterPlugin.android.getDefaultConfig().getApplicationId() + ".BuildConfig";
                break;
        }
        return super.visitField(access, name, desc, signature, value);
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
    Set<String> activitys;
    Set<String> likes;
    String buildConfigClass;
    String pkg;

    LoadTransformer(MethodVisitor delegate, int access, String name, String desc, String pkg, Set<String> activitys, Set<String> likes, List<String> buildConfigClass) {
        super(Opcodes.ASM5, delegate, access, name, desc);
        this.pkg = pkg;
        this.activitys = activitys;
        this.likes = likes;
        this.buildConfigClass = buildConfigClass.size() > 0 ? buildConfigClass.get(0) : "";
    }


    @Override
    public void visitInsn(int opcode) {
        if (opcode == Opcodes.RETURN) {
//            System.out.println(" visitInsn ->    "+pkg);
//            super.visitFieldInsn(Opcodes.GETSTATIC, pkg, "buildTimeStr", "Lcom/pqixing/annotation/ObjectSet;");
//            super.visitLdcInsn(DateFormat.getDateTimeInstance().format(new Date()));
//            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/pqixing/annotation/ObjectSet", "setO", "(Ljava/lang/Object;)"+ Type.VOID_TYPE, false);
//            super.visitInsn(Opcodes.POP);
            super.visitFieldInsn(Opcodes.GETSTATIC, pkg, "infos", "Ljava/util/ArrayList;");
            super.visitLdcInsn(System.currentTimeMillis()+"");
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/ArrayList", "add", "(Ljava/lang/Object;)" + org.objectweb.asm.Type.BOOLEAN_TYPE, false);
            super.visitInsn(Opcodes.POP);

            super.visitFieldInsn(Opcodes.GETSTATIC, pkg, "infos", "Ljava/util/ArrayList;");
            super.visitLdcInsn(buildConfigClass);
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/ArrayList", "add", "(Ljava/lang/Object;)" + org.objectweb.asm.Type.BOOLEAN_TYPE, false);
            super.visitInsn(Opcodes.POP);


            for (String key : activitys) {
                super.visitFieldInsn(Opcodes.GETSTATIC, pkg, "activitys", "Ljava/util/HashSet;");
                super.visitLdcInsn(key.replace("/", "."));
                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/HashSet", "add", "(Ljava/lang/Object;)" + org.objectweb.asm.Type.BOOLEAN_TYPE, false);
                super.visitInsn(Opcodes.POP);
            }
            for (String key : likes) {
                super.visitFieldInsn(Opcodes.GETSTATIC, pkg, "likes", "Ljava/util/HashSet;");
                super.visitLdcInsn(key.replace("/", "."));
                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/HashSet", "add", "(Ljava/lang/Object;)" + org.objectweb.asm.Type.BOOLEAN_TYPE, false);
                super.visitInsn(Opcodes.POP);
            }
        }
        super.visitInsn(opcode);
    }
}