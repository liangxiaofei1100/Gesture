package com.zhaoyan.gesture;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.RemoteViews;
import android.widget.SimpleAdapter;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity implements OnItemClickListener,
		OnClickListener {
	private static final String TAG = MainActivity.class.getSimpleName();

	private Context mContext;
	private GridView mGridView;
	private ListAdapter mAdapter;
	private ArrayList<HashMap<String, Object>> mData;

	private final String KEY_ITEM_ICON = "icon";
	private final String KEY_ITEM_TEXT = "text";
	private final String KEY_ITEM_CLASS_NAME = "class";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate()");
		setContentView(R.layout.activity_main);
		mContext = this;

		initLaunchers();
		initView();
		
		//notification test
		RemoteViews views = new RemoteViews(getPackageName(), R.layout.main_notification);
		
		Intent intent;
		PendingIntent pIntent;

		intent = new Intent("com.zhaoyan.gesutre.test");
		intent.putExtra("test", "11111111111111111");
		pIntent = PendingIntent.getActivity(this, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.iv_add, pIntent);
		
		intent = new Intent("com.zhaoyan.gesutre.test");
		intent.putExtra("test", "2222222222222222222");
		pIntent = PendingIntent.getActivity(this, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.iv_add2, pIntent);
		
		
		intent = new Intent("com.zhaoyan.gesutre.test");
		intent.putExtra("test", "333333333333333333");
		pIntent = PendingIntent.getActivity(this, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.iv_add3, pIntent);
		
		Notification mNotification = new Notification.Builder(this).getNotification();
		mNotification.contentView = views;
		mNotification.flags = Notification.FLAG_ONGOING_EVENT;
		mNotification.contentIntent = PendingIntent.getActivity(this, 0,
				intent, 0);
		
		NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		manager.notify(222, mNotification);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		try {
			startActivity(getLaunchIntent(mContext,
					mData.get(position).get(KEY_ITEM_CLASS_NAME).toString()));
		} catch (Exception e) {
			Log.e(TAG,
					"onItemClick item "
							+ mData.get(position).get(KEY_ITEM_TEXT));
		}
	}

	@Override
	public void onClick(View v) {

	}

	private void initLaunchers() {
		mData = new ArrayList<HashMap<String, Object>>();
		String[] names = getMainLauncherName(mContext);
		String[] classname = getMainLauncherClassName(mContext);
		int[] icons = getMainLauncherIcons(mContext);
		for (int i = 0; i < names.length; i++) {
			HashMap<String, Object> item = new HashMap<String, Object>();
			item.put(KEY_ITEM_ICON, icons[i]);
			item.put(KEY_ITEM_TEXT, names[i]);
			item.put(KEY_ITEM_CLASS_NAME, classname[i]);
			mData.add(item);
		}
	}

	private void initView() {
		mGridView = (GridView) findViewById(R.id.gv_main_launchers);
		final int gridRowNumber = getResources().getInteger(
				R.integer.main_gridview_row);
		mAdapter = new SimpleAdapter(mContext, mData,
				R.layout.main_item, new String[] { KEY_ITEM_ICON,
						KEY_ITEM_TEXT }, new int[] { R.id.iv_gj_item_icon,
						R.id.tv_gj_item_text }) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				convertView = super.getView(position, convertView, parent);
				AbsListView.LayoutParams param = (LayoutParams) convertView
						.getLayoutParams();
				param.width = LayoutParams.MATCH_PARENT;
				param.height = mGridView.getHeight() / gridRowNumber;
				convertView.setLayoutParams(param);
				return convertView;
			}
		};
		mGridView.setAdapter(mAdapter);
		mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		mGridView.setOnItemClickListener(this);
	}

	/**
	 * Get guanjia main page launcher icon resource id for display icons.
	 * 
	 * @param context
	 * @return
	 */
	private int[] getMainLauncherIcons(Context context) {
		TypedArray icons = context.getResources().obtainTypedArray(
				R.array.main_item_icon);
		int iconsIds[] = new int[icons.length()];
		for (int i = 0; i < iconsIds.length; i++) {
			iconsIds[i] = icons.getResourceId(i, -1);
		}
		icons.recycle();
		return iconsIds;
	}

	/**
	 * Get guanjia main page launcher name for display.
	 * 
	 * @param context
	 * @return
	 */
	private String[] getMainLauncherName(Context context) {
		String[] names = context.getResources().getStringArray(
				R.array.main_item_text);
		return names;
	}

	/**
	 * Get guanjia main page launcher class name for launch activity.
	 * 
	 * @param context
	 * @return
	 */
	private String[] getMainLauncherClassName(Context context) {
		String[] classname = context.getResources().getStringArray(
				R.array.main_item_classname);
		return classname;
	}

	/**
	 * Get Intent for launch activity.
	 * 
	 * @param context
	 * @param className
	 * @return
	 */
	private Intent getLaunchIntent(Context context, String className) {
		Intent intent = new Intent();
		intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		intent.setClassName(context, className);
		return intent;
	}

}
