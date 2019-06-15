package com.pqixing.annotation;

/**
 * 标记待启动的Activity页面
 */
public @interface LaunchActivity {
    String name() default "";

    String group() default "";

    int index() default 0;

    String type() default "activity";
}
