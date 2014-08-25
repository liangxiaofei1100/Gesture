package com.zhaoyan.gesture;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGestureListener;
import android.gesture.Prediction;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.widget.Toast;

public class GestureRecognizeActivity extends Activity implements
		OnGestureListener {
	private GestureLibrary mLibrary;
	private GestureOverlayView mGestureOverlayView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gestures_main);
		final String path = new File(Environment.getExternalStorageDirectory(),
				"gestures").getAbsolutePath();
		mLibrary = GestureLibraries.fromFile(path);
		mLibrary.load();

		mGestureOverlayView = (GestureOverlayView) findViewById(R.id.gestures_overlay);
		mGestureOverlayView.addOnGestureListener(this);

	}

	@Override
	public void onGesture(GestureOverlayView overlay, MotionEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onGestureCancelled(GestureOverlayView overlay, MotionEvent event) {

	}

	@Override
	public void onGestureEnded(GestureOverlayView overlay, MotionEvent event) {

		Gesture gesture = overlay.getGesture();
		recognizeGesture(gesture);
	}

	@Override
	public void onGestureStarted(GestureOverlayView overlay, MotionEvent event) {

	}

	private void recognizeGesture(Gesture gesture) {
		ArrayList<Prediction> predictions = mLibrary.recognize(gesture);
		if (!predictions.isEmpty()) {
			Prediction prediction = predictions.get(0);
			if (prediction.score >= 5) {
				Toast.makeText(
						this,
						"匹配成功，手势：" + prediction.name + "，匹配度："
								+ prediction.score, Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(
						this,
						"匹配度低，手势：" + prediction.name + "，匹配度："
								+ prediction.score, Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(this, "无匹配手势", Toast.LENGTH_SHORT).show();
		}
	}
}
