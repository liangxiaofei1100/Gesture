package com.zhaoyan.gesture.activity;

import com.zhaoyan.gesture.R;
import com.zhaoyan.gesture.R.anim;
import com.zhaoyan.gesture.R.id;
import com.zhaoyan.gesture.R.layout;
import com.zhaoyan.gesture.R.string;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BaseActivity extends Activity implements OnMenuItemClickListener,
		OnGestureListener {
	private static final String TAG = "BaseActivity";
	// title view
	protected View mCustomTitleView;
	protected TextView mTitleNameView;
	protected TextView mTitleNumView;

	// menubar
	protected View mMenuBarView;
	protected LinearLayout mMenuHolder;

	// 视图模式
	private GestureDetector mGestureDetector;;
	private int verticalMinDistance = 20;
	private int minVelocity = 0;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mGestureDetector = new GestureDetector(this, this);

	}

	@Override
	public void setContentView(int layoutResID) {
		// TODO Auto-generated method stub
		super.setContentView(R.layout.base_layout);
		if (layoutResID > 0)
			LayoutInflater.from(this).inflate(layoutResID,
					(ViewGroup) findViewById(R.id.root_layout));
	}

	protected void initTitle(int titleName) {
		mCustomTitleView = findViewById(R.id.title);

		// title name view
		mTitleNameView = (TextView) mCustomTitleView
				.findViewById(R.id.tv_title_name);
		mTitleNameView.setText(titleName);
		mTitleNumView = (TextView) mCustomTitleView
				.findViewById(R.id.tv_title_num);
	}

	protected void setTitleNumVisible(boolean visible) {
		mTitleNumView.setVisibility(visible ? View.VISIBLE : View.GONE);
	}

	protected void updateTitleNum(int selected, int count) {
		if (selected == -1) {
			mTitleNumView.setText(getString(R.string.num_format, count));
		} else {
			mTitleNumView.setText(getString(R.string.num_format2, selected,
					count));
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (onBackKeyPressed()) {
				finish();
				overridePendingTransition(0, R.anim.activity_right_out);
				return true;
			} else {
				return false;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	public boolean onBackKeyPressed() {
		return true;
	}

	/**
	 * start activity by class name
	 * 
	 * @param pClass
	 */
	protected void openActivity(Class<?> pClass) {
		openActivity(pClass, null);
	}

	/**
	 * start activity by class name & include data
	 * 
	 * @param pClass
	 * @param bundle
	 */
	protected void openActivity(Class<?> pClass, Bundle bundle) {
		Intent intent = new Intent(this, pClass);
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		if (bundle != null) {
			intent.putExtras(bundle);
		}
		startActivity(intent);
		overridePendingTransition(R.anim.activity_right_in, 0);
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		if (e1.getX() - e2.getX() > verticalMinDistance
				&& Math.abs(velocityX) > minVelocity) {
			/** 向左手势 */
		} else if (e2.getX() - e1.getX() > verticalMinDistance
				&& Math.abs(velocityX) > minVelocity) {
			/** 向右手势 */
			this.finish();
		}
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		return mGestureDetector.onTouchEvent(event);
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}
}
