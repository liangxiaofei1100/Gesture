package com.zhaoyan.gesture.activity;

import java.util.ArrayList;
import java.util.List;

import com.zhaoyan.common.dialog.ZyDialogBuilder;
import com.zhaoyan.gesture.R;
import com.zhaoyan.gesture.app.AppLauncherActivity;
import com.zhaoyan.gesture.camera.AppChooseDialog;
import com.zhaoyan.gesture.camera.AppInfo;
import com.zhaoyan.gesture.camera.CameraSetting;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class CameraActivity extends BaseActivity {
	private static final String TAG = CameraActivity.class.getSimpleName();

	private Context mContext;
	private TextView mCameraAppName;
	private ImageView mCameraAppIcon;

	private String mCameraPackageName;
	private PackageManager mPackageManager;

	private static final int REQUEST_GET_CAMERA = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_main);

		mContext = this;
		mPackageManager = getPackageManager();

		initView();

		mCameraPackageName = CameraSetting.getCameraAppPackageName(mContext);

		updateApp();
	}

	private void initView() {
		initTitle(R.string.main_camera);
		mBaseIntroductionView
				.setIntentExtraName(getString(R.string.main_camera));
		mBaseIntroductionView
				.setIntroductionText(getString(R.string.introduction_camer));

		mCameraAppName = getView(R.id.tv_camera_label);
		mCameraAppIcon = getView(R.id.iv_camera_logo);
	}

	public void openCamera(View view) {
		if (mCameraPackageName != null) {
			Intent intent = CameraSetting.getCameraActivity(mContext,
					mCameraPackageName);
			try {
				startActivity(intent);
			} catch (Exception e) {
				Toast.makeText(mContext, "该应用无法启动", Toast.LENGTH_SHORT).show();
			}
		} else {
			Log.e(TAG, "openCamera fail. No camera app. package="
					+ mCameraPackageName);
		}
	}

	public void setCamera(View view) {
		final List<ResolveInfo> resolveInfos = CameraSetting
				.getAllCameraApp(mContext);

		List<AppInfo> appInfos = new ArrayList<AppInfo>();
		for (ResolveInfo resolveInfo : resolveInfos) {
			AppInfo appInfo = new AppInfo();
			appInfo.packageName = resolveInfo.activityInfo.packageName;
			appInfo.label = resolveInfo.activityInfo.loadLabel(mPackageManager)
					.toString();
			appInfo.logo = resolveInfo.activityInfo.loadIcon(mPackageManager);
			appInfos.add(appInfo);
		}

		int currentChoicePostion = -1;
		for (int i = 0; i < appInfos.size(); i++) {
			AppInfo info = appInfos.get(i);
			if (info.packageName.equals(mCameraPackageName)) {
				currentChoicePostion = i;
				break;
			}
		}

		if (currentChoicePostion == -1) {
			Log.d(TAG, "setCamera custom camera.");
			appInfos.add(0, getCurrentCameraAppInfo());
			currentChoicePostion = 0;
		}

		final AppChooseDialog dialog = new AppChooseDialog(this, appInfos);
		dialog.setCurrentChoice(currentChoicePostion);
		dialog.setDialogTitle("设置默认相机");
		dialog.setButtonClick(Dialog.BUTTON1, "取消", new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.cancel();
			}
		});

		dialog.setButtonClick(ZyDialogBuilder.BUTTON2, "确定",
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						AppInfo appInfo = dialog.getCurrentChoice();

						mCameraPackageName = appInfo.packageName;

						CameraSetting
								.setCameraApp(mContext, mCameraPackageName);

						updateApp();
						dialog.dismiss();
					}
				});
		dialog.setLoadMoreClick(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(mContext, AppLauncherActivity.class);

				Bundle bundle = new Bundle();
				bundle.putBoolean("selectMode", true);
				bundle.putString("title", "选择默认相机");

				intent.putExtras(bundle);

				startActivityForResult(intent, REQUEST_GET_CAMERA);
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	private AppInfo getCurrentCameraAppInfo() {
		AppInfo appInfo = new AppInfo();
		String preLabel = "";
		Drawable preDrawable = null;
		try {
			Intent cameraIntent = CameraSetting.getCameraActivity(mContext,
					mCameraPackageName);

			if (cameraIntent != null) {
				ActivityInfo activityInfo = mPackageManager.getActivityInfo(
						CameraSetting.getCameraActivity(mContext,
								mCameraPackageName).getComponent(), 0);

				preLabel = activityInfo.loadLabel(mPackageManager).toString();
				preDrawable = activityInfo.loadIcon(mPackageManager);
			} else {
				ApplicationInfo applicationInfo = mPackageManager
						.getApplicationInfo(mCameraPackageName, 0);
				preLabel = (String) applicationInfo.loadLabel(mPackageManager);
				preDrawable = applicationInfo.loadIcon(mPackageManager);
			}

			appInfo.packageName = mCameraPackageName;
			appInfo.label = preLabel;
			appInfo.logo = preDrawable;
			return appInfo;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void updateApp() {
		try {
			Intent cameraIntent = CameraSetting.getCameraActivity(mContext,
					mCameraPackageName);

			if (cameraIntent != null) {
				ActivityInfo info = mPackageManager.getActivityInfo(
						CameraSetting.getCameraActivity(mContext,
								mCameraPackageName).getComponent(), 0);

				mCameraAppName.setText(info.loadLabel(mPackageManager));
				mCameraAppIcon.setImageDrawable(info.loadIcon(mPackageManager));
			} else {
				ApplicationInfo info = mPackageManager.getApplicationInfo(
						mCameraPackageName, 0);
				mCameraAppName.setText(info.loadLabel(mPackageManager));
				mCameraAppIcon.setImageDrawable(info.loadIcon(mPackageManager));
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (RESULT_OK == resultCode && requestCode == REQUEST_GET_CAMERA) {
			String packageName = data.getPackage();
			Log.d(TAG, "onActivityResult packageName=" + packageName);

			if (packageName != null) {
				mCameraPackageName = packageName;
				CameraSetting.setCameraApp(mContext, mCameraPackageName);
				updateApp();
			} else {
				Toast.makeText(mContext, "无法设置该应用", Toast.LENGTH_SHORT).show();
			}
		}
	}
}
