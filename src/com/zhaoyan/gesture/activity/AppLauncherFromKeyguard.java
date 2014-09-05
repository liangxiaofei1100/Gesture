package com.zhaoyan.gesture.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

public class AppLauncherFromKeyguard extends Activity {
	private static final String TAG = AppLauncherFromKeyguard.class
			.getSimpleName();
	private static final String EXTRA_BROADCAST = "broadcast";
	private static final String EXTRA_ACTION = "action";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

		Intent intent = getIntent();
		boolean isBroadcast = intent.getBooleanExtra(EXTRA_BROADCAST, false);
		if (isBroadcast) {
			launchByBroadcast();
		} else {
			launchByActivity();
		}
	}

	private void launchByBroadcast() {
		String action = getIntent().getStringExtra(EXTRA_ACTION);
		Intent intent = new Intent(action);
		sendBroadcast(intent);
	}

	private void launchByActivity() {

	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "onStop");
		finish();
	}

	public static final void launchByBroadcast(Context context, String action) {
		Intent intent = new Intent(context, AppLauncherFromKeyguard.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.putExtra(EXTRA_BROADCAST, true);
		intent.putExtra(EXTRA_ACTION, action);
		context.startActivity(intent);
	}
}
