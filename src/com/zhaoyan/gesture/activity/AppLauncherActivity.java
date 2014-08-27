package com.zhaoyan.gesture.activity;

import com.zhaoyan.gesture.R;

import android.os.Bundle;

public class AppLauncherActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(0);
		initTitle(R.string.main_app_launcher);
	}

}
