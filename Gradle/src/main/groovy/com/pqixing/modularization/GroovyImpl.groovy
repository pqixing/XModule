package com.pqixing.modularization

import com.pqixing.modularization.impl.ExtHelper
import com.pqixing.modularization.iterface.IExtHelper

public class GroovyImpl {
    public static final <T> T getImpl(Class<T> t) {
        if (t == IExtHelper) {
            return new ExtHelper();
        }
        return null
    }
}
