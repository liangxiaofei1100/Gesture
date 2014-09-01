package com.zhaoyan.gesture.appgesture;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.gesture.Gesture;
import android.gesture.Prediction;

import com.zhaoyan.gesture.GestureManager.GestureHandler;
import com.zhaoyan.gesture.R;

/** how to release camera when exit */
public class FlashLightGuestrue implements GestureHandler {
	private final String TAG = FlashLightGuestrue.class.getSimpleName();
	private Context mContext;

	private FlashLightManager mFlashLightManager;
	
	public FlashLightGuestrue(Context context) {
		mContext = context;
		mFlashLightManager = new FlashLightManager();
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
			if (!mFlashLightManager.isFlashOn()) {
				mFlashLightManager.openFlashlight();
			} else {
				mFlashLightManager.closeFlashlight();
			}
		}
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return true;
	}


	@Override
	public void release() {
		// TODO Auto-generated method stub
		mFlashLightManager.closeFlashlight();
	}
}
