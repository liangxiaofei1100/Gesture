package com.zhaoyan.gesture;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGestureListener;
import android.gesture.Prediction;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.Toast;

import com.zhaoyan.gesture.util.CopyFile;

public class GestureRecognizeActivity extends Activity implements
		OnGestureListener {
	private static final String TAG = GestureRecognizeActivity.class
			.getSimpleName();

	private GestureLibrary mLibrary;
	private GestureOverlayView mGestureOverlayView;

	private int mScreenWidth, mScrennHeight;

	private GestureManager mGestureManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gestures_main);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		mScreenWidth = dm.widthPixels;// 宽度
		mScrennHeight = dm.heightPixels;// 高度
		final String path = new File(getFilesDir(), "gestures")
				.getAbsolutePath();
		CopyFile.copyFile(this, path, null);
		mLibrary = GestureLibraries.fromFile(path);
		mLibrary.load();

		mGestureOverlayView = (GestureOverlayView) findViewById(R.id.gestures_overlay);
		mGestureOverlayView.addOnGestureListener(this);

		mGestureManager = new GestureManager();
		mGestureManager.init(this);
	}

	@Override
	public void onGesture(GestureOverlayView overlay, MotionEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onGestureCancelled(GestureOverlayView overlay, MotionEvent event) {

	}

	@Override
	public void onGestureEnded(GestureOverlayView overlay, MotionEvent event) {

		Gesture gesture = overlay.getGesture();
		recognizeGesture(gesture);
	}

	@Override
	public void onGestureStarted(GestureOverlayView overlay, MotionEvent event) {

	}

	private void recognizeGesture(Gesture gesture) {
		if (gesture.getLength() < (mScreenWidth + mScrennHeight) / 10) {
			return;
		}
		ArrayList<Prediction> predictions = mLibrary.recognize(gesture);
		if(!predictions.isEmpty()){
			double score=0;
			Prediction temp = null;
			for(Prediction p:predictions){
				if(p.score>score){
					score=p.score;
					temp=p;
				}
			}
			
			mGestureManager.dispatchGesture(gesture, temp);
		} else {
			Toast.makeText(this, "无匹配手势", Toast.LENGTH_SHORT).show();
		}
		return;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mGestureManager.releaseResource();
	}
}
