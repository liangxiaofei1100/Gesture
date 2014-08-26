package com.zhaoyan.gesture.camera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

public class CameraLauncher extends Activity {
	private static final String TAG = CameraLauncher.class.getSimpleName();
	private static final int REQUEST_CAMERA = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = new Intent(Intent.ACTION_CAMERA_BUTTON);
		sendBroadcast(intent);
		finish();
		// startActivityForResult(intent, REQUEST_CAMERA);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_CAMERA:
			if (resultCode == RESULT_OK) {
				Log.d(TAG, "resultCode == RESULT_OK");
			} else if (resultCode == RESULT_CANCELED) {
				Log.d(TAG, "resultCode == RESULT_CANCELED");
			}
			finish();
			break;

		default:
			break;
		}
	}
}
