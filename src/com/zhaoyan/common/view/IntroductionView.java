package com.zhaoyan.common.view;

import com.zhaoyan.gesture.R;
import com.zhaoyan.gesture.activity.GestureShowActivity;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class IntroductionView extends RelativeLayout implements OnClickListener {
	private Context mContext;
	private View mClkView, mTryGestrueView;
	private TextView mIntroductionTv;
	private Intent intent;
	private String mIntroductionText;
	private ImageView mImageView;
	private String extraString;

	public IntroductionView(Context context) {
		// TODO Auto-generated constructor stub
		super(context);
		this.mContext = context;
		initView("wwwwwwwwwwwwwwwwwwwwwwwww");

	}

	public IntroductionView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;
		initView("wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww");
	}

	public IntroductionView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		initView("wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww");
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.introduction_layout:
			if (mIntroductionTv.getVisibility() == View.GONE) {
				mIntroductionTv.setVisibility(View.VISIBLE);
				mIntroductionTv.setAnimation((AnimationUtils.loadAnimation(mContext,R.anim.slide_down_in)));
				if (mIntroductionText != null)
					mIntroductionTv.setText(mIntroductionText);
				mImageView.setImageResource(R.drawable.setting_item_arrow_up);
			} else {
				mIntroductionTv.setVisibility(View.GONE);
//				mIntroductionTv.setAnimation((AnimationUtils.loadAnimation(mContext,R.anim.slide_up_out)));
				mImageView.setImageResource(R.drawable.setting_item_arrow_down);
			}
			break;
		case R.id.gestrue_try:
			if (intent != null) {
				mContext.startActivity(intent);
			} else if (extraString != null) {
				Intent intent = new Intent();
				intent.setClass(mContext, GestureShowActivity.class);
				intent.putExtra("name", extraString);
				mContext.startActivity(intent);
			}

			break;
		default:
			break;
		}

	}

	/** set introduction text */
	public void setIntroductionText(String introdunction) {
		mIntroductionText = introdunction;
		mIntroductionTv.setText(introdunction);
	}

	/** @deprecated use  {@link #setIntentExtraName(String)} */
	public void setShowGestureIntent(Intent intent) {
		this.intent = intent;
	}

	public void setIntentExtraName(String name) {
		extraString = name;
	}

	private void initView(String introdunction) {
		LayoutInflater inflater = LayoutInflater.from(mContext);
		inflater.inflate(R.layout.introduction_view_layout, this);
		mClkView = findViewById(R.id.introduction_layout);
		mTryGestrueView = findViewById(R.id.gestrue_try);
		mImageView = (ImageView) findViewById(R.id.iv_wo_ts_arrow);
		mIntroductionTv = (TextView) findViewById(R.id.introduction_tv);
		mClkView.setOnClickListener(this);
		mTryGestrueView.setOnClickListener(this);
		mIntroductionTv.setText(introdunction);
	}

}
