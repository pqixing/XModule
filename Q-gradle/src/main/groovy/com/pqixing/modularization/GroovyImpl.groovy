package com.pqixing.modularization

import com.pqixing.modularization.impl.GExtHelper
import com.pqixing.modularization.iterface.IExtHelper

public class GroovyImpl {
    public static final <T> T getImpl(Class<T> t) {
        if (t == IExtHelper) {
            return new GExtHelper();
        }
        return null
    }
}
