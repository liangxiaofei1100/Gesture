package com.zhaoyan.gesture.activity;

import com.zhaoyan.gesture.R;

import android.os.Bundle;

public class QuickCaptureActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(0);
		initTitle(R.string.main_quick_capture);
		mBaseIntroductionView.setIntentExtraName(getString(R.string.main_quick_capture));
		mBaseIntroductionView.setIntroductionText(getString(R.string.introduction_quick_capture));
	}

}
