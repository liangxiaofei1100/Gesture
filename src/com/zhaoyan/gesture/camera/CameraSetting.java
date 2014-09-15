package com.zhaoyan.gesture.camera;

import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.provider.MediaStore;
import android.util.Log;

public class CameraSetting {
	private static final String TAG = CameraSetting.class.getSimpleName();

	public static final String PREF_NAME = "camera_setting";
	public static final String PREF_PACKAGE = "camera_package";

	private static Intent getAndSetSystemCamera(Context context) {
		Log.d(TAG, "getAndSetSystemCamera");
		List<ResolveInfo> list = getAllCameraApp(context);

		PackageManager pm = context.getPackageManager();
		String packageName = null;
		String activityName = null;
		for (ResolveInfo info : list) {
			Log.d(TAG, "getAndSetSystemCamera " + info.activityInfo.packageName
					+ ", " + info.activityInfo.name);
			packageName = info.activityInfo.packageName;
			activityName = info.activityInfo.name;
			try {
				PackageInfo packageInfo = pm.getPackageInfo(packageName, 0);
				if (isSystemApp(packageInfo) || isSystemUpdateApp(packageInfo)) {
					break;
				}
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}

		Intent result = null;
		if (packageName != null && activityName != null) {
			result = new Intent();
			result.setClassName(packageName, activityName);

			setCameraApp(context, packageName);
		}
		return result;
	}

	private static boolean isSystemApp(PackageInfo pInfo) {
		return ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
	}

	private static boolean isSystemUpdateApp(PackageInfo pInfo) {
		return ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0);
	}

	public static void resetSetting(Context context) {
		Intent intent = getAndSetSystemCamera(context);
		setCameraApp(context, intent.getComponent().getPackageName());
	}

	public static List<ResolveInfo> getAllCameraApp(Context context) {
		Log.d(TAG, "getAllCameraApp");
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		PackageManager pm = context.getPackageManager();
		List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
		return list;
	}

	public static String getCameraAppPackageName(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREF_NAME,
				Context.MODE_PRIVATE);
		String packageName = prefs.getString(PREF_PACKAGE, null);

		if (packageName == null) {
			Intent intent = getAndSetSystemCamera(context);
			packageName = intent.getComponent().getPackageName();
		}
		return packageName;
	}

	public static void setCameraApp(Context context, String packageName) {
		Log.d(TAG, "setCameraApp packageName=" + packageName);
		SharedPreferences prefs = context.getSharedPreferences(PREF_NAME,
				Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putString(PREF_PACKAGE, packageName);
		editor.apply();
	}

	public static Intent getCameraActivity(Context context, String packageName) {
		Intent result = null;

		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.setPackage(packageName);
		PackageManager pm = context.getPackageManager();
		List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
		if (list != null && !list.isEmpty()) {
			result = new Intent();
			result.setComponent(new ComponentName(packageName,
					list.get(0).activityInfo.name));
		} else {
			result = pm.getLaunchIntentForPackage(packageName);
		}
		return result;
	}
}
