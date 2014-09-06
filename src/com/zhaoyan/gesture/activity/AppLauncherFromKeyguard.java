package com.zhaoyan.gesture.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

public class AppLauncherFromKeyguard extends Activity {
	private static final String TAG = AppLauncherFromKeyguard.class
			.getSimpleName();

	private Context mContext;
	private static final String EXTRA_BROADCAST = "broadcast";
	private static final String EXTRA_ACTION = "action";

	private static final String EXTRA_PACKAGE_NAME = "package";
	private static final String EXTRA_CLASS_NAME = "class";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		mContext = this;

		Intent intent = getIntent();
		boolean isBroadcast = intent.getBooleanExtra(EXTRA_BROADCAST, false);
		if (isBroadcast) {
			launchByBroadcast();
		} else {
			launchByActivity();
		}
	}

	private void launchByBroadcast() {
		Log.d(TAG, "launchByBroadcast");
		String action = getIntent().getStringExtra(EXTRA_ACTION);
		Intent intent = new Intent(action);
		sendBroadcast(intent);
	}

	private void launchByActivity() {
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				Log.d(TAG, "launchByActivity");
				String packageName = getIntent().getStringExtra(
						EXTRA_PACKAGE_NAME);
				String className = getIntent().getStringExtra(EXTRA_CLASS_NAME);
				Intent intent = new Intent();
				intent.setComponent(new ComponentName(packageName, className));
				try {
					startActivity(intent);
				} catch (Exception e) {
					Log.e(TAG, "launchByActivity");
					Toast.makeText(mContext, "该应用无法启动", Toast.LENGTH_SHORT)
							.show();
				}
			}
		}, 500);
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

	public static final void launchByActivity(Context context,
			Intent launchIntent) {
		Intent intent = new Intent(context, AppLauncherFromKeyguard.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TASK);

		intent.putExtra(EXTRA_PACKAGE_NAME, launchIntent.getComponent()
				.getPackageName());
		intent.putExtra(EXTRA_CLASS_NAME, launchIntent.getComponent()
				.getClassName());
		context.startActivity(intent);
	}
}
