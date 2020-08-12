package com.pqixing.mvp.views.activity;


import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.pqixing.mvp.constract.BaseContract;
import com.pqixing.mvp.utils.PresenterInitUtil;

/**
 * @param <P> 具体presenter实现类
 */
public abstract class MVPBaseActivity<P extends BaseContract.IPresenter> extends AppCompatActivity implements BaseContract.IView {
	//public Dialog dialog;
	private InputMethodManager imm;
	protected P mPresenter;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
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
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(mPresenter != null){
			mPresenter.pause();
		}
	}


	@Override
	protected void onStop() {
		super.onStop();
		if(mPresenter != null){
			mPresenter.stop();
		}
	}

	@Override
	protected void onDestroy() {
		if(mPresenter != null){
			mPresenter.destroy();
		}
		super.onDestroy();
	}



	/**
	 * 关闭输入法
	 */
	@Override
	public void closeInput(){
		 
		   //得到InputMethodManager的实例 
		   if (imm != null && imm.isActive()) { //关闭输入法
			   //如果开启 
			   if(this.getCurrentFocus() != null && this.getCurrentFocus().getWindowToken() != null)
				   imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
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
		Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
	}

	@Override
	public Context getContext() {
		return this;
	}

	@Override
	public Application getDcApplication() {
		return getApplication();
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
