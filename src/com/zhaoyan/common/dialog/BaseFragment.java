package com.zhaoyan.common.dialog;

import com.zhaoyan.common.dialog.ActionMenu.ActionMenuItem;
import com.zhaoyan.common.dialog.ActionMenuInterface.OnMenuItemClickListener;
import com.zhaoyan.common.view.TransportAnimationView;
import com.zhaoyan.gesture.R;
import com.zhaoyan.gesture.image.ImageActivity;
import com.zhaoyan.gesture.image.MenuBarManager;
import com.zhaoyan.gesture.image.ZYConstant.Extra;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class BaseFragment extends Fragment implements OnMenuItemClickListener{
	protected boolean mIsSelectAll = false;
	protected Context mContext = null;
	
	//title
	protected TextView mTitleNameView,mTitleNumView;
	private ViewGroup mViewGroup;
	
	//menubar
	protected View mMenuBarView;
	protected LinearLayout mMenuHolder;
	protected MenuBarManager mMenuBarManager;
	protected ActionMenu mActionMenu;
	
	private ActionMenuInflater mActionMenuInflater;
	
	private ImageActivity mImageActivity;
	
	/**
	 * current fragment file size
	 */
	protected int count = 0;
	
	//视图模式
	protected int mViewType = Extra.VIEW_TYPE_DEFAULT;
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		if(activity instanceof ImageActivity){
			mImageActivity=(ImageActivity) activity;
		}
	}

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
	
	protected void initMenuBar(View view){
		mMenuBarView = view.findViewById(R.id.menubar_bottom);
		mMenuBarView.setVisibility(View.GONE);
		mMenuHolder = (LinearLayout) view.findViewById(R.id.ll_menutabs_holder);
		
		mMenuBarManager = new MenuBarManager(getActivity().getApplicationContext(), mMenuHolder);
		mMenuBarManager.setOnMenuItemClickListener(this);
	}
	
	public void startMenuBar(){
		mMenuBarView.setVisibility(View.VISIBLE);
		mMenuBarView.clearAnimation();
		mMenuBarView.startAnimation(AnimationUtils.loadAnimation(mContext,R.anim.slide_up_in));
		mMenuBarManager.refreshMenus(mActionMenu);
	}
	
	public void destroyMenuBar(){
		mMenuBarView.setVisibility(View.GONE);
		mMenuBarView.clearAnimation();
		mMenuBarView.startAnimation(AnimationUtils.loadAnimation(mContext,R.anim.slide_down_out));
	}
	
	protected void updateTitleNum(int selected){
		mImageActivity.updateTitleNum(selected, count);
//		if (isAdded()) {
//			if (selected == -1) {
//				mTitleNumView.setText(getString(R.string.num_format, count));
//			}else {
//				mTitleNumView.setText(getString(R.string.num_format2, selected, count));
//			}
//		}
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

	@Override
	public void onMenuItemClick(ActionMenuItem item) {
		// TODO Auto-generated method stub
		
	};
	
	protected ActionMenuInflater getActionMenuInflater(){
		if (null == mActionMenuInflater) {
			mActionMenuInflater = new ActionMenuInflater(getActivity().getApplicationContext());
		}
		return mActionMenuInflater;
	}
	
	protected boolean isListView(){
		return Extra.VIEW_TYPE_LIST == mViewType;
	}
}
