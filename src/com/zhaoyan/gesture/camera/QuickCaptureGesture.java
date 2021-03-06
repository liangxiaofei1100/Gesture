package com.zhaoyan.gesture.camera;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.Prediction;
import android.util.Log;
import android.widget.Toast;

import com.zhaoyan.gesture.R;
import com.zhaoyan.gesture.GestureManager.GestureHandler;

public class QuickCaptureGesture implements GestureHandler {
	private static final String TAG = QuickCaptureGesture.class.getSimpleName();
	private Context mContext;

	public QuickCaptureGesture(Context context) {
		mContext = context;
	}

	@Override
	public List<String> getGesterNames() {
		List<String> gestures = new ArrayList<String>();
		gestures.add(mContext.getString(R.string.gesture_quick_capture));
		return gestures;
	}

	@Override
	public void handleSystemGesture(String gestureName) {
		Log.d(TAG, "handleSystemGesture");
		Intent intent = new Intent(mContext, QuickCapture.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		mContext.startActivity(intent);
	}

	@Override
	public void handleGesture(Gesture gesture, Prediction prediction) {
		Log.d(TAG, "handleGesture name = " + prediction.name + ", score = "
				+ prediction.score + ", length = " + gesture.getLength());
		Intent intent = new Intent(mContext, QuickCapture.class);
		mContext.startActivity(intent);
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub

	}

}
