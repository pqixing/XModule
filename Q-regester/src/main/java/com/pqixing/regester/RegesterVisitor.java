package com.pqixing.regester;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;

import java.util.Set;

public class RegesterVisitor extends ClassVisitor {
    String className;
    Set<String> activitys;
    Set<String> likes;

    private static final String A = "Lcom/pqixing/annotation/LaunchActivity;";
    private static final String L = "Lcom/pqixing/annotation/LaunchAppLike;";

    public RegesterVisitor(String className, Set<String> activitys, Set<String> likes, int i, ClassVisitor classVisitor) {
        super(i, classVisitor);
        this.activitys = activitys;
        this.likes = likes;
        this.className = className;
    }

    @Override
    public void visitOuterClass(String owner, String name, String desc) {
//        System.out.println("owner -> " + owner + " name " + name + " desc " + desc);
        this.className = owner;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean b) {
//        System.out.println("visitAnnotation -> desc " + desc + " " + className + " c" + hashCode());
        AnnotationVisitor visitor = super.visitAnnotation(desc, b);
        String d = desc == null ? "" : desc;
        if (d.contains(A)) activitys.add(className);
        if (d.contains(L)) likes.add(className);
        return visitor;
    }
}
