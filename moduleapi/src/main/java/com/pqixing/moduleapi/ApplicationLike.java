package com.pqixing.moduleapi;

import android.app.Application;

/**
 * 默认实现的AppLike抽象类
 */
public abstract class ApplicationLike implements IApplicationLike {
    private Application application;

    @Override
    public void init(Application application) {
        this.application = application;
    }

    @Override
    public Application getApplication() {
        return application;
    }
}
