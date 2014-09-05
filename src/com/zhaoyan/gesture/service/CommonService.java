package com.zhaoyan.gesture.service;

import com.zhaoyan.common.utils.Utils;
import com.zhaoyan.gesture.R;
import com.zhaoyan.gesture.activity.MusicActivity;
import com.zhaoyan.gesture.appgesture.FlashLightManager;
import com.zhaoyan.gesture.music.MediaPlaybackService;
import com.zhaoyan.gesture.music.MusicConf;
import com.zhaoyan.gesture.music.ui.MusicBrowserActivity;
import com.zhaoyan.gesture.sos.MessageSender;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
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
	
	private NotificationManager mNotificationManager;
	private Builder mBuilder;
	private Notification mNotification;

	private void broadcastEvent(String what) {
		Intent i = new Intent(what);
		sendBroadcast(i);
	}
	
	public CommonService(){
	}

	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate>>>>");
		mFlashLightManager = new FlashLightManager();
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		updateNotification(this, R.drawable.status_bar_flashlight);
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

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void updateNotification(Context context, int flashLightResId) {
		Log.d(TAG, "showNotificaiton>>>>");
		RemoteViews views = new RemoteViews(getPackageName(),
				R.layout.main_notification);
		
		views.setImageViewResource(R.id.iv_statis_bar_flashlight, flashLightResId);

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

//		mBuilder = new NotificationCompat.Builder(this);
//		mBuilder.setContent(views);
//		mBuilder.setAutoCancel(false);
//		mBuilder.setOngoing(true);
//		mBuilder.setSmallIcon(R.drawable.notifi_bg, NotificationCompat.PRIORITY_MIN);
//		mBuilder.setContentIntent(PendingIntent.getService(this, 0, intent,
//				0));
//		mNotificationManager.notify(GESTURE_NOTIFICATION, mBuilder.build());
		
		mNotification = new Notification.Builder(this).getNotification();
		mNotification.contentView = views;
        mNotification.flags = Notification.FLAG_ONGOING_EVENT;
        mNotification.icon = R.drawable.ic_launcher;
        mNotification.priority = Notification.PRIORITY_MIN;
        mNotification.contentIntent = PendingIntent.getService(context, 0, intent, 0);
        startForeground(GESTURE_NOTIFICATION, mNotification);
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
