package com.pqixing.modularization.helper;


public class JGroovyHelper {


    public static IExtHelper getImpl(Class<?> tClass) {
        return getImpl();
    }

    public static IExtHelper getImpl() {
        if (helper == null) {
            try {
                helper = (IExtHelper) Class.forName("com.pqixing.modularization.impl.GExtHelper").newInstance();
            } catch (Exception e) {

            }
        }
        return helper;
    }

    private static IExtHelper helper;
}
