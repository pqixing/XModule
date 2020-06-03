package com.pqixing.common.views.fragment;


import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;

import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.pqixing.common.constract.BaseContract;
import com.pqixing.common.utils.PresenterInitUtil;


/**
 * @author libiao
 * @param <P> 具体presenter实现类
 */
public abstract class MVPBaseFragment<P extends BaseContract.IPresenter> extends Fragment implements BaseContract.IView {
	private InputMethodManager imm;
	protected P mPresenter;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		mPresenter = PresenterInitUtil.createPresenter(this, getRealPresenter());
	}

	/**
	 * 设置头顶标题
	 */
	/*public void setHeadTitle(String title) {
		TextView tx = (TextView) findViewById(R.id.title_tv);
		tx.setText(title);
		findViewById(R.id.back_lay).setOnClickListener(new OnClickListener() {
			public void onClick(IView view) {
				// 返回
				finish();
			}
		});
	}*/


	
	@Override
	public void onDestroy() {
		super.onDestroy();
		destroy();
	}

	@Override
	public void onPause() {
		super.onPause();
		if(mPresenter != null){
			mPresenter.pause();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		if(mPresenter != null){
			mPresenter.stop();
		}
	}

	private void destroy() {
		if(mPresenter != null){
			mPresenter.destroy();
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		destroy();
	}


	/**
	 * 关闭输入法
	 */
	@Override
	public void closeInput(){
		 
		   //得到InputMethodManager的实例 
		   if (imm != null && imm.isActive()) { //关闭输入法
			   //如果开启 
			   if(getActivity().getCurrentFocus() != null && getActivity().getCurrentFocus().getWindowToken() != null)
				   imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
		   } 
	}

	/**
	 * 显示
	 */
	@Override
	public void showInputOrClose(){
		//得到InputMethodManager的实例  
		if (imm != null && imm.isActive()) {  
			//如果开启  
			imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);   
			//关闭软键盘，开启方法相同，这个方法是切换开启与关闭状态的  
		}  
	}


	@Override
	public void showLoading() {
	}

	@Override
	public void hideLoading() {
	}

	@Override
	public void showToastMsg(String message) {
		Toast.makeText(getContext(),message,Toast.LENGTH_SHORT).show();
	}

	@Override
	public Context getContext() {
		return getActivity();
	}

	@Override
	public Application getDcApplication() {
		return getActivity().getApplication();
	}

	@Override
	public void showFailView(String failMsg) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void hideFailView() {
		// TODO Auto-generated method stub
		
	}

}
