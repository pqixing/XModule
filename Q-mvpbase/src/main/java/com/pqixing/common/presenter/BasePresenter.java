/**
 * Copyright (C) 2014 android10.org. All rights reserved.
 * @author Fernando Cejas (the android10 coder)
 */
package com.pqixing.common.presenter;


import android.app.Application;
import android.content.Context;

import com.pqixing.common.constract.BaseContract;

/**
 * @author libiao
 *
 * @param <V> view层通信接口
 * @param <M> model层业务实现类
 */
@SuppressWarnings("unchecked")
public abstract class BasePresenter<V extends BaseContract.IView, M extends BaseContract.IModel>
		implements BaseContract.IPresenter{
	/**
	 * 绑定的视图数据
	 */
	public V mViewer;
	/**
	 * 绑定的业务数据
	 */
	public M mMode;

	/**
	 * Method that control the lifecycle of the view. It should be called in the
	 * view's (Activity or Fragment) onResume() method.
	 */
	@Override
	public void init(){
	}

	public String getHttpTaskKey() {
		return this.getClass().getSimpleName() + "_" + this.hashCode();
	}

	/**
	 * Method that control the lifecycle of the view. It should be called in the
	 * view's (Activity or Fragment) onPause() method.
	 */
	@Override
	public void destroy(){
		if(mMode != null){
			mMode.cancelRequest(getHttpTaskKey());
		}
		mMode = null;
		mViewer = null;
	}

	@Override
	public void stop() {

	}

	@Override
	public void pause() {

	}

	public void setViewer(V viewer){
		mViewer = viewer;
		try {
			//this.mMode = (M) PresenterInitUtil.getGenericType(this, 1).newInstance();
			this.mMode = (M) getRealModel().newInstance();
			if(mMode != null){
				mMode.initRequestLife(getAppContext());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	/**
	 * 默认构造方法
	 */
	public BasePresenter() {
	}


	public void showLoading() {
		mViewer.showLoading();
	}

	public void hideLoading() {
		mViewer.hideLoading();
	}

	public void showToastMsg(String message) {
		mViewer.showToastMsg(message);
	}

	public Context getAppContext() {
		return mViewer.getContext();
	}

	public Application getDcApplication() {
		return mViewer.getDcApplication();
	}

	public void showFailView(String failMsg) {
		mViewer.showFailView(failMsg);
	}

	public void hideFailView() {
		mViewer.hideFailView();
	}

	/**
	 * 关闭输入法
	 */
	public void closeInput(){
		mViewer.closeInput();
	}

	/**
	 * 显示
	 */
	public void showInputOrClose(){
		mViewer.showInputOrClose();
	}

}
