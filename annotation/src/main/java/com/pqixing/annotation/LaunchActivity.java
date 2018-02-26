package com.pqixing.annotation;

/**
 * 标记待启动的Activity页面
 */
public @interface LaunchActivity {
    String name() default "";
}
