package com.zhaoyan.gesture;

import android.app.Application;

public class GestureApplication extends Application {
	private GestureManager mGestureManager = new GestureManager();

	@Override
	public void onCreate() {
		super.onCreate();
		mGestureManager.init(getApplicationContext());
	}

	public GestureManager getGestureManager() {
		return mGestureManager;
	}
}
