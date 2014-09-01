package com.zhaoyan.gesture.service;

import com.zhaoyan.gesture.R;
import com.zhaoyan.gesture.appgesture.FlashLightManager;
import com.zhaoyan.gesture.music.MediaPlaybackService;
import com.zhaoyan.gesture.music.ui.MusicBrowserActivity;
import com.zhaoyan.gesture.sos.MessageSender;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
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
		
		intent = new Intent(ACTION_FLASHLIGHT);
		intent.setClass(context, CommonService.class);
		pIntent = PendingIntent.getService(this, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.status_bar_1, pIntent);

		intent = new Intent(ACTION_CAPTURE);
		intent.setClass(context, CommonService.class);
		pIntent = PendingIntent.getService(this, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.status_bar_2, pIntent);
		
		intent = new Intent("com.zhaoyao.gesture.quickcapture");
		pIntent = PendingIntent.getActivity(this, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.status_bar_3, pIntent);
		
		intent = new Intent(ACTION_SOS);
		intent.setClass(context, CommonService.class);
		pIntent = PendingIntent.getService(this, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.status_bar_4, pIntent);
		
		intent = new Intent("com.zhaoyan.action.MUSIC_PAYER");
		pIntent = PendingIntent.getActivity(this, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.status_bar_5, pIntent);

		Notification mNotification = new Notification.Builder(this)
				.getNotification();
		mNotification.contentView = views;
		mNotification.flags = Notification.FLAG_ONGOING_EVENT;
		mNotification.icon = R.drawable.ic_launcher;
		mNotification.contentIntent = PendingIntent.getService(this, 0, intent,
				0);

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
