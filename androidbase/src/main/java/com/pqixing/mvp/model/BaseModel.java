package com.pqixing.mvp.model;


import android.content.Context;

import com.pqixing.mvp.constract.BaseContract;

/**
 * model层业务基类
 */

public class BaseModel implements BaseContract.IModel {

    @Override
    public void initRequestLife(Context context) {

    }

    @Override
    public void cancelRequest(String key) {

    }

    @Override
    public void cancelAllReq() {

    }
}
