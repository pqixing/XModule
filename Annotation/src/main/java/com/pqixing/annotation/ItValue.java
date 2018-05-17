package com.pqixing.annotation;

public @interface ItValue {
    String key();
    String type() default  "string";
    String value();
}
