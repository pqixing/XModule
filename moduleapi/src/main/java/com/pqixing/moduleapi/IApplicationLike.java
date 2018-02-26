package com.pqixing.moduleapi;

import android.app.Application;

public interface IApplicationLike {

    public Application getApplication();

    /**
     * 只在模拟运行时调用，可以初始化一些主工程中需要调用的东西
     */
    @Deprecated
    void onVirtualCreate(Application application);

    /**
     * 都会调用
     *
     * @param application
     */
    void onCreateOnUI(Application application);

    /**
     * 在子线程中运行
     *
     * @param application
     */
    void onCreateOnThread(Application application);

    String getModuleName();

    void init(Application application);
}
