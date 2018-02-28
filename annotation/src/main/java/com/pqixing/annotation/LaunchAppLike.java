package com.pqixing.annotation;

/**
 * 标记自动加载的AppLike接口
 */
public @interface LaunchAppLike {
    String name() default "";
    String group() default "";
}
