package com.zhaoyan.gesture;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class GestureReciever extends BroadcastReceiver {
	private static final String TAG = GestureReciever.class.getSimpleName();
	public static final String ACTION_GESTURE = "com.zhaoyan.gesture.reciever";
	public static final String EXTRA_NAME = "name";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive");
		if (intent == null) {
			Log.e(TAG, "intent == null");
			return;
		}

		if (ACTION_GESTURE.equals(intent.getAction())) {
			String gestureName = intent.getStringExtra(EXTRA_NAME);
			Log.d(TAG, "Receive a gesture: " + gestureName);
			
			if (gestureName != null) {
				GestureApplication application = (GestureApplication) context
						.getApplicationContext();
				GestureManager gestureManager = application.getGestureManager();
				gestureManager.dispatchSystemGesture(gestureName);
			} else {
				Log.e(TAG, "Gesture name is null!");
			}
		}
	}
}
