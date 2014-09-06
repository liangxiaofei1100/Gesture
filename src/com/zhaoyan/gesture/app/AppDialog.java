package com.zhaoyan.gesture.app;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zhaoyan.common.dialog.Effectstype;
import com.zhaoyan.common.dialog.ZyDialogBuilder;
import com.zhaoyan.gesture.R;

public class AppDialog extends ZyDialogBuilder {
	
	private TextView mNameView;
	private TextView mNumView;
	private ProgressBar mProgressBar;
	
	private int progress;
	private String mName;
	private int max;
	private static final int MSG_UPDATE_PROGRESS = 0x10;
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_UPDATE_PROGRESS:
				mProgressBar.setProgress(progress);
				mNameView.setText(mName);
				mNumView.setText(progress + "/" + max);
				break;

			default:
				break;
			}
		};
	};
	
	public AppDialog(Context context, int size){
		super(context, R.style.dialog_untran);
		max = size;
		
		View view = getLayoutInflater().inflate(R.layout.app_dialog, null);
		
		mProgressBar = (ProgressBar) view.findViewById(R.id.bar_move);
		mProgressBar.setMax(max);
		
		mNameView = (TextView) view.findViewById(R.id.name_view);
		mNumView = (TextView) view.findViewById(R.id.num_view);
		
		setDuration(0);
		setEffect(Effectstype.SlideBottom);
		setMessage(null);
		setCustomView(view, context);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCancelable(false);
	}
	
	public void updateName(String name){
		this.mName = name;
		mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_PROGRESS));
	}
	
	public void updateProgress(int progress){
		this.progress = progress;
		mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_PROGRESS));
	}
	
	public void updateUI(int progress, String fileName){
		this.progress = progress;
		this.mName = fileName;
		mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_PROGRESS));
	}
	
	public int getMax(){
		return max;
	}
}
