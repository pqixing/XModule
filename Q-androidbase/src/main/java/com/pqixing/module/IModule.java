package com.pqixing.module;

public interface IModule {
    void onCreateOnUi();
    void onCreateOnThread();

    void onBackGroup();
    void onForceGroup();
    boolean onDispatch(int type,String url,Object param);
}
