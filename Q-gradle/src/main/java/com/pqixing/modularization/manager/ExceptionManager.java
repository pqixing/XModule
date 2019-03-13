package com.pqixing.modularization.manager;

import org.gradle.api.GradleException;

public class ExceptionManager {

    public static int EXCEPTION_SYNC = 0;
    public static int EXCEPTION_TASK = 1;
    public static int EXCEPTION_PROJECT = 2;

    public static void thow(int type, String error) {
        throw new GradleException(error);
    }
}
