package com.zhaoyan.gesture;

import java.io.File;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.zhaoyan.gesture.service.MusicPlayerService;

public class GestureRecognizeActivity extends Activity implements
		OnGestureListener {
	private static final String TAG = GestureRecognizeActivity.class.getSimpleName();
	
	private GestureLibrary mLibrary;
	private GestureOverlayView mGestureOverlayView;
	
	private Camera mCamera;
	private Parameters mParameters;
	private boolean mIsFlashOn = false;
	private boolean mIsMusicOn = false;
	
	private MusicPlayerService mMusicPlayerService = null;
	
	private ServiceConnection mMusicServiceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			Log.d(TAG, "onServiceConnected");
			mMusicPlayerService = ((MusicPlayerService.LocalBinder) service)
					.getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			Log.d(TAG, "onServiceDisconnected");
			mMusicPlayerService = null;
		}
	};

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

		bindService(new Intent(this,MusicPlayerService.class), mMusicServiceConnection, Context.BIND_AUTO_CREATE);
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
			if ("电筒".equals(prediction.name)) {
				if (mIsFlashOn) {
					closeFlashlight();
				} else {
					openFlashlight();
				}
			} else if ("音乐".equals(prediction.name)) {
				boolean isPlaying = mMusicPlayerService.isPlaying();
				if (mIsMusicOn && isPlaying) {
					mMusicPlayerService.pause();
				} else if (mIsMusicOn && !isPlaying) {
					mMusicPlayerService.start();
				} else {
					mMusicPlayerService.startMusic();
					mIsMusicOn = true;
				}
			} else if ("上一首".equals(prediction.name)) {
				mMusicPlayerService.previous();
			} else if ("下一首".equals(prediction.name)) {
				mMusicPlayerService.next();
			}
		} else {
			Toast.makeText(this, "无匹配手势", Toast.LENGTH_SHORT).show();
		}
	}
	
	// open flash light
	@SuppressLint("NewApi")
	protected void openFlashlight() {
		Toast.makeText(
				this, "手电筒开启", Toast.LENGTH_SHORT).show();
		try {
			mCamera = Camera.open();
			int textureId = 0;
			mCamera.setPreviewTexture(new SurfaceTexture(textureId));
			mCamera.startPreview();

			mParameters = mCamera.getParameters();
			mParameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
			mCamera.setParameters(mParameters);
			mIsFlashOn = true;
		} catch (Exception e) {
			Log.e("Yuri", "ERROR:" + e.toString());
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
		unbindService(mMusicServiceConnection);
	}

}
