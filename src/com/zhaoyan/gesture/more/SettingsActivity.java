package com.zhaoyan.gesture.more;


import android.os.Bundle;

public class SettingsActivity extends BaseFragmentActivity {
	private WoFragment mWoFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mWoFragment = new WoFragment();
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, mWoFragment).commit();
	}

}
