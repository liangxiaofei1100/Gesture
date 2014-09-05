package com.zhaoyan.gesture.image;

import com.zhaoyan.gesture.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class VideoGridItem extends RelativeLayout implements Checkable {
	private static final String TAG = "VideoGridItem";
	private Context mContext;
	private boolean mChecked;
	public ImageView mIconView;
	private TextView mTimeView;

	public VideoGridItem(Context context) {
		this(context, null, 0);
		// TODO Auto-generated constructor stub
	}
	
	public VideoGridItem(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public VideoGridItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		LayoutInflater.from(context).inflate(R.layout.video_item_grid, this);
		mIconView = (ImageView) findViewById(R.id.iv_video_icon);
		mTimeView = (TextView) findViewById(R.id.tv_video_time);
	}


	@Override
	public void setChecked(boolean checked) {
		mChecked = checked;
		setBackgroundDrawable(checked ? mContext.getResources().getDrawable(R.color.holo_blue_light) : null);
	}

	@Override
	public boolean isChecked() {
		return mChecked;
	}

	@Override
	public void toggle() {
		setChecked(!mChecked);
	}
	
	public void setIconResId(int resId){
		if (null != mIconView) {
			mIconView.setImageResource(resId);
		}
	}
	
	public void setIconBitmap(Bitmap bitmap){
		if (null != mIconView) {
			mIconView.setImageBitmap(bitmap);
		}
	}
	
	public void setVideoTime(long time){
		String timeStr = ZYUtils.mediaTimeFormat(time);
		mTimeView.setText(timeStr);
	}

}
