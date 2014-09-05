package com.zhaoyan.gesture;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.gesture.Gesture;
import android.gesture.Prediction;
import android.util.Log;
import android.widget.Toast;

import com.zhaoyan.gesture.appgesture.AppGesture;
import com.zhaoyan.gesture.appgesture.FlashLightGuestrue;
import com.zhaoyan.gesture.camera.CameraGuesture;
import com.zhaoyan.gesture.camera.QuickCaptureGesture;
import com.zhaoyan.gesture.music.MusicGesture;
import com.zhaoyan.gesture.sos.MessageGesture;

public class GestureManager {
	private static final String TAG = GestureManager.class.getSimpleName();
	private Context mContext;
	/** GestureHandlers handle gesture from this App. */
	private Map<String, GestureHandler> mGestureHandlers;
	/** SystemGestureHandlers handle gesture from Android framework. */
	private Map<String, GestureHandler> mSystemGestureHandlers;

	public void init(Context context) {
		Log.d(TAG, "init");
		mContext = context;
		GestureHandler messageGesture = new MessageGesture(context);
		GestureHandler appGesture = new AppGesture(context);
		GestureHandler flashLightGesture = new FlashLightGuestrue(context);
		GestureHandler musicGesture = new MusicGesture(context);
		GestureHandler cameraGesture = new CameraGuesture(context);
		GestureHandler quickCaptureGesture = new QuickCaptureGesture(context);

		mGestureHandlers = new HashMap<String, GestureHandler>();
		addAppGesture(messageGesture);
		addAppGesture(appGesture);
		addAppGesture(flashLightGesture);
		addAppGesture(musicGesture);
		addAppGesture(cameraGesture);
		addAppGesture(quickCaptureGesture);

		mSystemGestureHandlers = new HashMap<String, GestureHandler>();
		addSystemGesture("camera", cameraGesture);
		addSystemGesture("quick_capture", quickCaptureGesture);
		addSystemGesture("music_play", musicGesture);
		addSystemGesture("music_pre", musicGesture);
		addSystemGesture("music_next", musicGesture);
		addSystemGesture("sos", messageGesture);
		// TODO need update video.
		addSystemGesture("video", flashLightGesture);
	}

	private void addSystemGesture(String gestureName, GestureHandler gesture) {
		if (gesture.isEnabled()) {
			mSystemGestureHandlers.put(gestureName, gesture);
		}
	}

	private void addAppGesture(GestureHandler gesture) {
		if (gesture.isEnabled()) {
			for (String name : gesture.getGesterNames()) {
				mGestureHandlers.put(name, gesture);
			}
		}
	}

	public void dispatchSystemGesture(String gestureName) {
		GestureHandler handler = mSystemGestureHandlers.get(gestureName);
		if (handler != null) {
			Log.d(TAG, "dispatchSystemGesture success.");
			handler.handleSystemGesture(gestureName);
		} else {
			Log.d(TAG, "dispatchSystemGesture fail.");
		}
	}

	public void dispatchGesture(Gesture gesture, Prediction prediction) {
		Toast.makeText(mContext,
				prediction.name + "   匹配度: " + prediction.score,
				Toast.LENGTH_SHORT).show();
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

		/**
		 * 
		 * @param gestureName
		 */
		void handleSystemGesture(String gestureName);

		boolean isEnabled();

		void release();
	}
}
