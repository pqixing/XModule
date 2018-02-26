package com.pqixing.compiler.annotation;

/**
 * 标记自动加载的AppLike接口
 */
public @interface LaunchAppLike {
    String name() default "";
}
