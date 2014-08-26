package com.zhaoyan.gesture;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.gesture.Gesture;
import android.gesture.Prediction;

import com.zhaoyan.gesture.appgesture.AppGesture;
import com.zhaoyan.gesture.appgesture.FlashLightGuestrue;
import com.zhaoyan.gesture.sos.MessageGesture;

public class GestureManager {
	private Map<String, GestureHandler> mGestureHandlers;

	public void init(Context context) {
		mGestureHandlers = new HashMap<String, GestureHandler>();
		addGesture(new MessageGesture(context));
		addGesture(new AppGesture(context));
		addGesture(new FlashLightGuestrue(context));
	}

	private void addGesture(GestureHandler gesture) {
		if (gesture.isEnabled()) {
			for (String name : gesture.getGesterNames()) {
				mGestureHandlers.put(name, gesture);
			}
		}
	}

	public void dispatchGesture(Gesture gesture, Prediction prediction) {
		GestureHandler handler = mGestureHandlers.get(prediction.name);
		if (handler != null)
			handler.handleGesture(gesture, prediction);
	}

	public void releaseResource() {
		for (Entry<String, GestureHandler> entry : mGestureHandlers.entrySet()) {
			entry.getValue().release();
		}
	}

	public interface GestureHandler {
		List<String> getGesterNames();

		/**
		 * 
		 * @param gesture
		 * @param prediction
		 */
		void handleGesture(Gesture gesture, Prediction prediction);

		boolean isEnabled();

		void release();
	}
}
