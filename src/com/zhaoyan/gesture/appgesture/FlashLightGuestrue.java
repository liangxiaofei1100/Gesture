package com.zhaoyan.gesture.appgesture;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.gesture.Gesture;
import android.gesture.Prediction;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;
import android.widget.Toast;

import com.zhaoyan.gesture.GestureManager.GestureHandler;
import com.zhaoyan.gesture.R;

/** how to release camera when exit */
public class FlashLightGuestrue implements GestureHandler {
	private final String TAG = FlashLightGuestrue.class.getSimpleName();
	private Camera mCamera;
	private Parameters mParameters;
	private boolean mIsFlashOn = false;
	private Context mContext;

	public FlashLightGuestrue(Context context) {
		mContext = context;
	}

	@Override
	public List<String> getGesterNames() {
		// TODO Auto-generated method stub
		List<String> list = new ArrayList<String>();
		list.add(mContext.getString(R.string.gesture_flashlight));
		return list;
	}

	@Override
	public void handleGesture(Gesture gesture, Prediction prediction) {
		// TODO Auto-generated method stub
		if (prediction.score > 3) {
			if (!mIsFlashOn) {
				openFlashlight();
			} else {
				closeFlashlight();
			}
		}
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return true;
	}

	protected void openFlashlight() {
		Toast.makeText(mContext, "手电筒开启", Toast.LENGTH_SHORT).show();
		try {
			mCamera = Camera.open();
			int textureId = 0;
			mParameters = mCamera.getParameters();
			mParameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
			mCamera.setParameters(mParameters);

			mCamera.setPreviewTexture(new SurfaceTexture(textureId));
			mCamera.startPreview();
			mIsFlashOn = true;
		} catch (Exception e) {
			Log.e(TAG, "ERROR:" + e.toString());
		}
	}

	// close flash light
	protected void closeFlashlight() {
		Toast.makeText(mContext, "手电筒关闭", Toast.LENGTH_SHORT).show();
		if (mCamera != null) {
			mParameters = mCamera.getParameters();
			mParameters.setFlashMode(Parameters.FLASH_MODE_OFF);
			mCamera.setParameters(mParameters);
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
			mIsFlashOn = false;
		}
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub
		closeFlashlight();
	}
}
