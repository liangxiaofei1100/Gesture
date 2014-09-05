package com.zhaoyan.gesture.more;

import com.zhaoyan.gesture.R;

import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;


public class BaseFragmentActivity extends FragmentActivity {

	// title view
	protected View mCustomTitleView;
	protected TextView mTitleNameView;

	protected void initTitle(int titleName) {
		mCustomTitleView = findViewById(R.id.title);

		// title name view
		mTitleNameView = (TextView) mCustomTitleView
				.findViewById(R.id.tv_title_name);
		mTitleNameView.setText(titleName);

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			overridePendingTransition(0, R.anim.activity_right_out);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
