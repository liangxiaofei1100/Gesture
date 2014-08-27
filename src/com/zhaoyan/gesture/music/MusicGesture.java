package com.zhaoyan.gesture.music;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.Prediction;
import android.util.Log;

import com.zhaoyan.gesture.R;
import com.zhaoyan.gesture.GestureManager.GestureHandler;

public class MusicGesture implements GestureHandler{
	private static final String TAG = MusicGesture.class.getSimpleName();
	
	private boolean mIsMusicOn = false;
	private Context mContext;
	
	public MusicGesture(Context context){
		mContext = context;
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
			if ("音乐".equals(name)) {
				if (mIsMusicOn) {
					intent.setAction(MediaPlaybackService.TOGGLEPAUSE_ACTION);
				} else {
					Intent i = new Intent(mContext, MediaPlaybackService.class);
					i.setAction(MediaPlaybackService.OPEN_ACTION);
			        mContext.startService(i);
					mIsMusicOn = true;
					return;
				}
			} else if ("上一首".equals(name)) {
				intent.setAction(MediaPlaybackService.PREVIOUS_ACTION);
			} else if ("下一首".equals(name)) {
				intent.setAction(MediaPlaybackService.NEXT_ACTION);
			}
			mContext.sendBroadcast(intent);
		}
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void release() {
		Intent i = new Intent(mContext, MediaPlaybackService.class);
        mContext.stopService(i);
	}

}
