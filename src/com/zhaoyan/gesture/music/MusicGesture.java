package com.zhaoyan.gesture.music;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.Prediction;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;

import com.zhaoyan.gesture.R;
import com.zhaoyan.gesture.GestureManager.GestureHandler;

public class MusicGesture implements GestureHandler{
	private static final String TAG = MusicGesture.class.getSimpleName();
	
	private boolean mIsMusicOn = false;
	private Context mContext;
	
	private String packageName = "";
	
	public MusicGesture(Context context){
		mContext = context;
		
		packageName = MusicConf.getStringPref(context, "package", "com.zhaoyan.gesture");
	}
	
	@Override
	public List<String> getGesterNames() {
		List<String> list = new ArrayList<String>();
		list.add(mContext.getString(R.string.gesture_music));
		list.add(mContext.getString(R.string.gesture_next_song));
		list.add(mContext.getString(R.string.gesture_up_song));
		return list;
	}

	@Override
	public void handleGesture(Gesture gesture, Prediction prediction) {
		Log.d(TAG, "handleGesture.name:" + prediction.name + ",score:" + prediction.score);
		if (prediction.score > 3) {
			String name = prediction.name;
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_MEDIA_BUTTON);
			KeyEvent  keyEvent = null;
			if ("音乐".equals(name)) {
				if (mIsMusicOn) {
					Log.d(TAG, "handleGesture.playpause");
					keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
				} else {
					Log.d(TAG, "handleGesture.open");
					keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY);
					mIsMusicOn = true;
				}
			} else if ("上一首".equals(name)) {
				Log.d(TAG, "handleGesture.previous");
				keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS);
			} else if ("下一首".equals(name)) {
				Log.d(TAG, "handleGesture.next");
				keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT);
			}
			long time = SystemClock.uptimeMillis();
			Log.d(TAG, "handleGesture.package:" + packageName);
			KeyEvent.changeTimeRepeat(keyEvent, time, 0);
			intent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
			intent.setPackage(packageName);
			mContext.sendBroadcast(intent);
			
			keyup();
		}
	}
	
	public void keyup(){
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_MEDIA_BUTTON);
		KeyEvent  keyEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY);
		long time = SystemClock.uptimeMillis();
		System.out.println(time);
		KeyEvent.changeTimeRepeat(keyEvent, time, 0);
		intent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
		intent.setPackage("com.zhaoyan.gesture");
		mContext.sendBroadcast(intent);
	}
	

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void release() {
//		Intent i = new Intent(mContext, MediaPlaybackService.class);
//        mContext.stopService(i);
	}

}
