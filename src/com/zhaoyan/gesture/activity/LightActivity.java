package com.zhaoyan.gesture.activity;

import com.zhaoyan.gesture.R;

import android.os.Bundle;

public class LightActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(0);
		initTitle(R.string.main_light);
		mBaseIntroductionView.setIntentExtraName(getString(R.string.main_light));
		mBaseIntroductionView.setIntroductionText(getString(R.string.introduction_light));
	}

}
