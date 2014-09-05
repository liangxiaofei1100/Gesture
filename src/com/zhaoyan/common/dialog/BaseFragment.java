package com.zhaoyan.common.dialog;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhaoyan.common.view.TransportAnimationView;
import com.zhaoyan.gesture.R;
import com.zhaoyan.gesture.common.ZYConstant.Extra;


public class BaseFragment extends Fragment{
	protected boolean mIsSelectAll = false;
	protected Context mContext = null;
	
	//title
	protected TextView mTitleNameView,mTitleNumView;
	private ViewGroup mViewGroup;
	
	/**
	 * current fragment file size
	 */
	protected int count = 0;
	
	//视图模式
	protected int mViewType = Extra.VIEW_TYPE_DEFAULT;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();
		
		//get View Type
		mViewType =  Extra.VIEW_TYPE_DEFAULT;
				
		setHasOptionsMenu(true);
	}
	
	protected void initTitle(View view, int title_resId){
		mViewGroup = (ViewGroup) view;
		mTitleNameView = (TextView) view.findViewById(R.id.tv_title_name);
		mTitleNameView.setText(title_resId);
		mTitleNumView = (TextView) view.findViewById(R.id.tv_title_num);
		mTitleNumView.setVisibility(View.VISIBLE);
	}
	
	/**
	 * Show transport animation.
	 * 
	 * @param startViews The transport item image view.
	 */
	public void showTransportAnimation(ImageView... startViews) {
		TransportAnimationView transportAnimationView = new TransportAnimationView(
				mContext);
		transportAnimationView.startTransportAnimation(mViewGroup,
				mTitleNameView, startViews);
	}
	
	/**
	 * start activity by class name
	 * @param pClass
	 */
	protected void openActivity(Class<?> pClass){
		openActivity(pClass, null);
	}
	
	/**
	 * start activity by class name & include data
	 * @param pClass
	 * @param bundle
	 */
	protected void openActivity(Class<?> pClass, Bundle bundle){
		Intent intent = new Intent(getActivity(), pClass);
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		if (bundle != null) {
			intent.putExtras(bundle);
		}
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.activity_right_in, 0);
	}
	
	/**
	 * when user pressed back key
	 */
	public boolean onBackPressed(){
		return true;
	}
	
	public void onDestroy() {
		super.onDestroy();
	}
	
	protected boolean isListView(){
		return Extra.VIEW_TYPE_LIST == mViewType;
	}
}
