package com.zhaoyan.gesture.camera;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.Prediction;
import android.graphics.Camera;
import android.util.Log;
import android.widget.Toast;

import com.zhaoyan.gesture.R;
import com.zhaoyan.gesture.GestureManager.GestureHandler;
import com.zhaoyan.gesture.activity.AppLauncherFromKeyguard;
import com.zhaoyan.gesture.util.WakeupUtil;

public class CameraGuesture implements GestureHandler {
	private static final String TAG = CameraGuesture.class.getSimpleName();
	private Context mContext;

	public CameraGuesture(Context context) {
		mContext = context;
	}

	@Override
	public List<String> getGesterNames() {
		List<String> gestures = new ArrayList<String>();
		gestures.add(mContext.getString(R.string.gesture_camera));
		return gestures;
	}

	private void handleGesture() {
		WakeupUtil.wakeup(mContext);
		String cameraPackage = CameraSetting.getCameraAppPackageName(mContext);
		Intent intent = CameraSetting
				.getCameraActivity(mContext, cameraPackage);
		AppLauncherFromKeyguard.launchByActivity(mContext, intent);
	}

	@Override
	public void handleSystemGesture(String gestureName) {
		handleGesture();
	}

	@Override
	public void handleGesture(Gesture gesture, Prediction prediction) {
		Log.d(TAG, "handleGesture name = " + prediction.name + ", score = "
				+ prediction.score + ", length = " + gesture.getLength());
		Intent intent = new Intent(Intent.ACTION_CAMERA_BUTTON);
		mContext.sendBroadcast(intent);
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void release() {

	}

}
