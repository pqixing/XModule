package com.pqixing.compiler.annotation;

/**
 * 标记待启动的Activity页面
 */
public @interface LaunchActivity {
    String name() default "";
}
