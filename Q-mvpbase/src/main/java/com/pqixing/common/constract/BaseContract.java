package com.pqixing.common.constract;

import android.app.Application;
import android.content.Context;


/**
 * @author libiao
 * 管理mvp 各层结构的基类
 */

public interface BaseContract {
    interface IView {
        /**
         * 显示进度视图
         */
        void showLoading();

        /**
         * 隐藏进度视图
         */
        void hideLoading();

        /**
         * 显示提示信息
         *
         * @param message
         *            提示信息
         */
        void showToastMsg(String message);

        /**
         * 获取上下文对象
         */
        Context getContext();

        /**
         * 获取application对象
         * @return
         */
        Application getDcApplication();

        /**
         * 加载失败后显示视图
         * @param failMsg	失败提示
         */
        void showFailView(String failMsg);
        /**
         * 隐藏加载失败后的视图
         */
        void hideFailView();

        /**
         * 关闭输入法
         */
        void closeInput();

        /**
         * 显示
         */
        void showInputOrClose();
        /**
         * 获取真实的prestener实现类
         * @return
         */
        Class<? extends IPresenter> getRealPresenter();
    }

    interface IPresenter {
        /**
         * 初始化方法
         */
        void init();

        /**
         * 销毁回收资源
         */
        void destroy();

        /**
         * 对应activity pause生命周期
         */
        void pause();

        /**
         * 对应activity stop生命周期
         */
        void stop();

        /**
         * 获取真实的model实现类
         * @return
         */
        Class<? extends IModel> getRealModel();

    }

    interface IModel {
        /**
         * 设置http请求类
         * @param context
         */
        void initRequestLife(Context context);
        /**
         * 按照key取消网络请求
         * @param key
         */
        void cancelRequest(String key);

        /**
         * 取消所以请求
         */
        void cancelAllReq();
    }
}
