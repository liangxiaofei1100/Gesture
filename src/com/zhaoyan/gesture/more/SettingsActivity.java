package com.zhaoyan.gesture.more;



import android.os.Bundle;
import android.view.Window;

public class SettingsActivity extends BaseFragmentActivity {
	private WoFragment mWoFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		new DirectLogin(this).login();
		mWoFragment = new WoFragment();
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, mWoFragment).commit();
	}

}
