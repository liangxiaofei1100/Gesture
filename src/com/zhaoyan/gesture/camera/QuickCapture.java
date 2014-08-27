package com.zhaoyan.gesture.camera;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import com.zhaoyan.gesture.R;

public class QuickCapture extends Activity implements SurfaceHolder.Callback {
	private static final String TAG = QuickCapture.class.getSimpleName();

	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;

	private Camera mCamera;
	private Parameters mParameters;
	private int mCameraBackId = -1;
	private int mCameraFrontId = -1;
	private int mCameraId;
	private int mCameraOrientation;
	private String mAutoFocusMode;

	private AudioManager mAudioManager;
	private int mVolumn;
	private int mTakePictureNumber;

	private PhotoSaver mPhotoSaver;
	private HapticFeedback mHapticFeedback;

	private static final int MSG_AUTO_FOCUS = 1;

	private boolean mIsCaptureSuccess = false;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_AUTO_FOCUS:
				if (mCamera != null && !mIsCaptureSuccess) {
					Log.d(TAG, "MSG_AUTO_FOCUS");
					try {
						mCamera.autoFocus(mAutoFocusCallback);
					} catch (Exception e) {
						Log.e(TAG, "MSG_AUTO_FOCUS " + e);
					}
					mHandler.sendEmptyMessageDelayed(MSG_AUTO_FOCUS, 2000);
				} else {
					mHandler.removeMessages(MSG_AUTO_FOCUS);
				}
				break;

			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		setContentView(R.layout.quick_capture);
		initView();

		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		mPhotoSaver = new PhotoSaver(this);
		mHapticFeedback = new HapticFeedback();
		mHapticFeedback.init(this, true);

		int orientationBack = 0;
		int orientationFront = 0;
		int cameraNumber = Camera.getNumberOfCameras();
		for (int i = 0; i < cameraNumber; i++) {
			CameraInfo cameraInfo = new CameraInfo();
			Camera.getCameraInfo(i, cameraInfo);
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
				orientationBack = cameraInfo.orientation;
				mCameraBackId = i;
			} else {
				orientationFront = cameraInfo.orientation;
				mCameraFrontId = i;
			}
		}

		if (mCameraBackId != -1) {
			mCameraId = mCameraBackId;
			mCameraOrientation = orientationBack;
		} else {
			mCameraId = mCameraFrontId;
			mCameraOrientation = orientationFront;
		}
	}

	private void initView() {
		mSurfaceView = (SurfaceView) findViewById(R.id.sv_camera);
		mSurfaceView.getHolder().addCallback(this);
	}

	private void openCamera() {
		Log.d(TAG, "openCamera");
		mCamera = Camera.open(mCameraId);

		mParameters = mCamera.getParameters();
		// Turn off flash.
		mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
		// Enable auto focus.
		List<String> focusModes = mParameters.getSupportedFocusModes();
		if (focusModes.contains(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
			mAutoFocusMode = Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
			mParameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
		} else if (focusModes.contains(Parameters.FOCUS_MODE_AUTO)) {
			mAutoFocusMode = Parameters.FOCUS_MODE_AUTO;
			mParameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
		}
		Log.d(TAG, "mAutoFocusMode = " + mAutoFocusMode);
		// rotation
		mParameters.setRotation(mCameraOrientation);
		// preview and picture size
		setPreviewAndPictureSize();

		mCamera.setParameters(mParameters);

		setCameraOrientation(mSurfaceView, mCamera);

		try {
			mCamera.setPreviewDisplay(mSurfaceHolder);
			mCamera.startPreview();
			mCamera.autoFocus(mAutoFocusCallback);
		} catch (IOException e) {
			Log.e(TAG, "open camera error. " + e);
		} catch (Exception e) {
			Log.e(TAG, "open camera error. " + e);
		}
	}

	private void setPreviewAndPictureSize() {
		List<Size> pictureSizes = mParameters.getSupportedPictureSizes();
		List<Size> previewSizes = mParameters.getSupportedPreviewSizes();
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		Size pictureSize = findBestFullScreenSize(pictureSizes, metrics);
		Size previewSize = findBestFullScreenSize(previewSizes, metrics);

		Log.d(TAG, "pictureSize " + pictureSize.width + " x "
				+ pictureSize.height);
		Log.d(TAG, "previewSize = " + previewSize.width + " x "
				+ previewSize.height);

		mParameters.setPictureSize(pictureSize.width, pictureSize.height);
		mParameters.setPreviewSize(previewSize.width, previewSize.height);
	}

	private Size findBestFullScreenSize(List<Size> sizes, DisplayMetrics metrics) {
		double fullscreen;
		if (metrics.widthPixels > metrics.heightPixels) {
			fullscreen = (double) metrics.widthPixels / metrics.heightPixels;
		} else {
			fullscreen = (double) metrics.heightPixels / metrics.widthPixels;
		}
		for (int i = sizes.size() - 1; i >= 0; i--) {
			if (toleranceRatio(fullscreen,
					(double) sizes.get(i).width / sizes.get(i).height)) {
				return sizes.get(i);
			}
		}
		// If no matched size, find width / height = 4 / 3
		for (int i = sizes.size() - 1; i >= 0; i--) {
			if (toleranceRatio(4 / 3,
					(double) sizes.get(i).width / sizes.get(i).height)) {
				return sizes.get(i);
			}
		}
		return sizes.get(sizes.size() - 1);
	}

	private boolean toleranceRatio(double d1, double d2) {
		boolean result = false;
		if (d2 > 0) {
			result = Math.abs(d1 - d2) <= 0.01;
		}
		return result;
	}

	private void setCameraOrientation(View display, Camera camera) {
		if (display.getRotation() == Surface.ROTATION_0
				|| display.getRotation() == Surface.ROTATION_180) {
			// portait
			int orientation = display.getRotation() == Surface.ROTATION_0 ? 90
					: 270;
			mCamera.setDisplayOrientation(orientation);
		} else if (display.getRotation() == Surface.ROTATION_90
				|| display.getRotation() == Surface.ROTATION_270) {
			// landscape
			int orientation = display.getRotation() == Surface.ROTATION_90 ? 0
					: 180;
			mCamera.setDisplayOrientation(orientation);
		}
	}

	private void closeCamera() {
		Log.d(TAG, "closeCamera");
		mCamera.cancelAutoFocus();
		mCamera.stopPreview();
		mCamera.release();
	}

	private void takePicture() {
		mTakePictureNumber = 3;
		mCamera.takePicture(mShutterCallback, mRawPictureCallback,
				mJpegPictureCallback);
		mIsCaptureSuccess = true;
	}

	private ShutterCallback mShutterCallback = new ShutterCallback() {

		@Override
		public void onShutter() {
			Log.d(TAG, "onShutter");
			mHapticFeedback.vibrate();
		}
	};

	private PictureCallback mRawPictureCallback = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] arg0, Camera arg1) {
			Log.d(TAG, "RawPictureCallback");
		}
	};

	private PictureCallback mJpegPictureCallback = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera arg1) {
			Log.d(TAG, "JpegPictureCallback");
			mPhotoSaver.savePhoto(data, new Date(),
					mParameters.getPictureSize().width,
					mParameters.getPictureSize().height);

			mTakePictureNumber--;
			if (mTakePictureNumber > 0) {
				mCamera.startPreview();
				mCamera.takePicture(mShutterCallback, mRawPictureCallback,
						mJpegPictureCallback);
			} else {
				// finish.
				finish();
			}
		}
	};

	private AutoFocusCallback mAutoFocusCallback = new AutoFocusCallback() {

		@Override
		public void onAutoFocus(boolean focus, Camera camera) {
			Log.d(TAG, "onAutoFocus " + (focus ? "success. " : "fail."));
			if (focus) {
				Log.d(TAG, "onAutoFocus mAutoFocusMode " + mAutoFocusMode);
				takePicture();
			} else {
				if (Parameters.FOCUS_MODE_AUTO.equals(mAutoFocusMode)) {
					mHandler.sendEmptyMessage(MSG_AUTO_FOCUS);
				}
			}
		}
	};

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mSurfaceHolder = holder;
		openCamera();
		silenceVolume();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mSurfaceHolder = null;
		closeCamera();
		restoreVolume();
	}

	private void silenceVolume() {
		mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
		mVolumn = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
		if (mVolumn != 0) {
			mAudioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0,
					AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0,
					AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
		}
	}

	private void restoreVolume() {
		mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, mVolumn,
				AudioManager.FLAG_ALLOW_RINGER_MODES);
	}

}
