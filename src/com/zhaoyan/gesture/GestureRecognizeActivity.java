package com.zhaoyan.gesture;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGestureListener;
import android.gesture.Prediction;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.Toast;

import com.zhaoyan.gesture.music.MediaPlaybackService;
import com.zhaoyan.gesture.music.MusicBrowserActivity;
import com.zhaoyan.gesture.sos.MessageSender;
import com.zhaoyan.gesture.util.CopyFile;

public class GestureRecognizeActivity extends Activity implements
		OnGestureListener {
	private static final String TAG = GestureRecognizeActivity.class.getSimpleName();
	
	private GestureLibrary mLibrary;
	private GestureOverlayView mGestureOverlayView;
	
	private Camera mCamera;
	private Parameters mParameters;
	private boolean mIsFlashOn = false;
	private boolean mIsMusicOn = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gestures_main);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		final String path = new File(Environment.getExternalStorageDirectory(),
				"gestures").getAbsolutePath();
		CopyFile.copyFile(this, path, null);
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
				if ("求救".equals(prediction.name)) {
					MessageSender.sendMessage(this);
				}
				
				if ("电筒".equals(prediction.name)) {
					if (mIsFlashOn) {
						closeFlashlight();
					} else {
						openFlashlight();
					}
				} else if ("音乐".equals(prediction.name)) {
					if (mIsMusicOn ) {
						Intent intent = new Intent();
				        intent.setAction(MediaPlaybackService.TOGGLEPAUSE_ACTION);
				        sendBroadcast(intent);
					} else {
						Intent intent = new Intent();
						intent.setClass(this, MusicBrowserActivity.class);
						startActivity(intent);
						mIsMusicOn = true;
					}
				} else if ("上一首".equals(prediction.name)) {
					Intent intent = new Intent();
			        intent.setAction(MediaPlaybackService.PREVIOUS_ACTION);
			        sendBroadcast(intent);
				} else if ("下一首".equals(prediction.name)) {
					Intent intent = new Intent();
			        intent.setAction(MediaPlaybackService.NEXT_ACTION);
			        sendBroadcast(intent);
				}
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
	
	// open flash light
	protected void openFlashlight() {
		Toast.makeText(
				this, "手电筒开启", Toast.LENGTH_SHORT).show();
		try {
			mCamera = Camera.open();
			int textureId = 0;
			mParameters = mCamera.getParameters();
			mParameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
			mCamera.setParameters(mParameters);
			
			mCamera.setPreviewTexture(new SurfaceTexture(textureId));
			mCamera.startPreview();
			mIsFlashOn = true;
		} catch (Exception e) {
			Log.e(TAG, "ERROR:" + e.toString());
		}
	}
	
	//close flash light
	protected void closeFlashlight() {
		Toast.makeText(
				this, "手电筒关闭", Toast.LENGTH_SHORT).show();
		if (mCamera != null) {
			mParameters = mCamera.getParameters();
			mParameters.setFlashMode(Parameters.FLASH_MODE_OFF);
			mCamera.setParameters(mParameters);
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
			mIsFlashOn = false;
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		closeFlashlight();
	}

}
