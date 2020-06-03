package com.pqixing.common.model;


import android.content.Context;

import com.pqixing.common.constract.BaseContract;

/**
 * @author libiao
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
