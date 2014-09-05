package com.zhaoyan.gesture.more;

import com.zhaoyan.gesture.R;

import android.os.Bundle;
import android.view.View;
import android.view.Window;

public class SettingsActivity extends BaseFragmentActivity {
	private WoFragment mWoFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		new DirectLogin(this).login();
		setContentView(R.layout.account_setting_b);
		mWoFragment = new WoFragment();
		getFragmentManager().beginTransaction()
				.replace(R.id.root_layout, mWoFragment).commit();
	}

}
