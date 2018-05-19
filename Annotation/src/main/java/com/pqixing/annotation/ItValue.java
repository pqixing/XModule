package com.pqixing.annotation;

public @interface ItValue {
    String key();
    Class type() default  String.class;
    String value();
}
