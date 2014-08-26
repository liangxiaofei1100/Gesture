package com.zhaoyan.gesture.appgesture;

import java.util.List;

import android.content.Context;
import android.gesture.Gesture;
import android.gesture.Prediction;

import com.zhaoyan.gesture.GestureManager.GestureHandler;

public class AppGesture implements GestureHandler {

	public AppGesture(Context context) {

	}

	@Override
	public List<String> getGesterNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleGesture(Gesture gesture, Prediction prediction) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub
		
	}

}
