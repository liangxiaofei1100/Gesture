package com.zhaoyan.gesture.service;

import com.zhaoyan.gesture.R;
import com.zhaoyan.gesture.activity.MusicActivity;
import com.zhaoyan.gesture.appgesture.FlashLightManager;
import com.zhaoyan.gesture.music.MediaPlaybackService;
import com.zhaoyan.gesture.music.MusicConf;
import com.zhaoyan.gesture.music.ui.MusicBrowserActivity;
import com.zhaoyan.gesture.sos.MessageSender;
import com.zhaoyan.gesture.util.Utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.TextView;

public class CommonService extends Service {
	private static final String TAG = CommonService.class.getSimpleName();
	private final IBinder mBinder = new LocalBinder();

	private static final int GESTURE_NOTIFICATION = 12;

	public static final String ACTION_FLASHLIGHT = "com.zhaoyao.juyou.commonservice.flashlight";
	public static final String ACTION_CAPTURE = "com.zhaoyao.juyou.commonservice.capture";
	public static final String ACTION_QUICK_CAPTURE = "com.zhaoyao.juyou.commonservice.quickcapture";
	public static final String ACTION_SOS = "com.zhaoyao.juyou.commonservice.sos";
	public static final String ACTION_MUSIC = "com.zhaoyao.juyou.commonservice.music";
	public static final String ACTION_VIDEO = "com.zhaoyao.juyou.commonservice.video";
	
	private FlashLightManager mFlashLightManager;

	private void broadcastEvent(String what) {
		Intent i = new Intent(what);
		sendBroadcast(i);
	}
	
	public CommonService(){
	}

	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate>>>>");
		showNotificaition(this);
		
		mFlashLightManager = new FlashLightManager();
		Log.d(TAG, "onCreate<<<<");
	}

	public class LocalBinder extends Binder {
		public CommonService getService() {
			return CommonService.this;
		}
	}

	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand.intent:" + intent);
		if (intent != null) {
			String action = intent.getAction();
			Log.d(TAG, "onStartCommand " + action);
			if (ACTION_FLASHLIGHT.equals(action)) {
				if (mFlashLightManager.isFlashOn()) {
					mFlashLightManager.closeFlashlight();
				} else {
					mFlashLightManager.openFlashlight();
				}
			} else if (ACTION_CAPTURE.equals(action)) {
				Intent captureIntent = new Intent(Intent.ACTION_CAMERA_BUTTON);
				sendBroadcast(captureIntent);
			} else if (ACTION_SOS.equals(action)) {
				MessageSender.sendMessage(this);
			} else if (ACTION_MUSIC.equals(action)) {
				String packageName = MusicConf.getStringPref(getApplicationContext(), "package", "com.zhaoyan.gesture");
				Intent musicIntent = null;
				if (packageName == null || packageName.equals("com.zhaoyan.gesture")) {
					musicIntent = new Intent();
					musicIntent.setClass(this, MusicBrowserActivity.class);
					musicIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				} else {
					musicIntent = getPackageManager().getLaunchIntentForPackage(packageName);
				}
				startActivity(musicIntent);
				
				Utils.collapseStatusBar(getApplicationContext());
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	public void showNotificaition(Context context) {
		Log.d(TAG, "showNotificaiton>>>>");
		RemoteViews views = new RemoteViews(getPackageName(),
				R.layout.main_notification);

		Intent intent;
		PendingIntent pIntent;

		intent = new Intent("com.zhaoyao.gesture.main");
		pIntent = PendingIntent.getActivity(this, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.ll_main_notifi, pIntent);
		
		intent = new Intent(ACTION_CAPTURE);
		intent.setClass(context, CommonService.class);
		pIntent = PendingIntent.getService(this, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.status_bar_1, pIntent);

		intent = new Intent("com.zhaoyao.gesture.quickcapture");
		pIntent = PendingIntent.getActivity(this, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.status_bar_2, pIntent);
		
		intent = new Intent(ACTION_MUSIC);
		intent.setClass(context, CommonService.class);
		pIntent = PendingIntent.getService(this, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.status_bar_3, pIntent);
		
		intent = new Intent("com.zhaoyan.action.voice");
		intent.setClass(context, CommonService.class);
		pIntent = PendingIntent.getService(this, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.status_bar_4, pIntent);
		
		intent = new Intent(ACTION_FLASHLIGHT);
		intent.setClass(context, CommonService.class);
		pIntent = PendingIntent.getService(this, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.status_bar_5, pIntent);
		
		intent = new Intent(ACTION_VIDEO);
		intent.setClass(context, CommonService.class);
		pIntent = PendingIntent.getService(this, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.status_bar_6, pIntent);

		Builder mNotification = new NotificationCompat.Builder(this);
		mNotification.setContent(views);
		mNotification.setAutoCancel(false);
		mNotification.setOngoing(true);
		mNotification.setSmallIcon(R.drawable.notifi_bg);
		mNotification.setContentIntent(PendingIntent.getService(this, 0, intent,
				0));

//		startForeground(GESTURE_NOTIFICATION, mNotification);
		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(GESTURE_NOTIFICATION, mNotification.build());
		Log.d(TAG, "showNotificaiton<<<<");
	}
	
	public void invisibleNotification(){
		stopForeground(true);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
}
