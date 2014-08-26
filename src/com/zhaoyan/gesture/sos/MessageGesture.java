package com.zhaoyan.gesture.sos;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.gesture.Gesture;
import android.gesture.Prediction;
import android.util.Log;

import com.zhaoyan.gesture.GestureManager.GestureHandler;
import com.zhaoyan.gesture.R;

public class MessageGesture implements GestureHandler {
	private Context mContext;

	public MessageGesture(Context context) {
		mContext = context;
	}

	@Override
	public void handleGesture(Gesture gesture, Prediction prediction) {
		// TODO Auto-generated method stub
		Log.e("ArbiterLiu", "send sos message");
		MessageSender.sendMessage(mContext);

	}

	@Override
	public List<String> getGesterNames() {
		List<String> gestures = new ArrayList<String>();
		gestures.add(mContext.getString(R.string.gesture_message));
		return gestures;
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
