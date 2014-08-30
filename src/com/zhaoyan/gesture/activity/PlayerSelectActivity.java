package com.zhaoyan.gesture.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.zhaoyan.gesture.R;
import com.zhaoyan.gesture.music.MusicConf;
import com.zhaoyan.gesture.music.utils.MusicUtils;

public class PlayerSelectActivity extends Activity implements OnItemClickListener, OnClickListener {
	private static final String TAG = PlayerSelectActivity.class.getSimpleName();
	
	
	private List<ResolveInfo> mApps = new ArrayList<ResolveInfo>();
	
	private ListView mListView;
	private MusicSelectAdapter mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_default_player_select);
		mApps = getMusicApps();
		
		String packagename = MusicConf.getStringPref(getApplicationContext(), "package", "com.zhaoyan.gesture");
		
		mListView = (ListView) findViewById (R.id.player_select_listview);
		
		View footView = LayoutInflater.from(this).inflate(R.layout.dialog_default_player_select_footview, null);
		View loadMore = footView.findViewById(R.id.rl_footview);
		loadMore.setOnClickListener(this);
		mListView.addFooterView(footView);
		
		mAdapter = new MusicSelectAdapter(this, packagename);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
	}
	
	private List<ResolveInfo> getMusicApps() {
		Intent intent = new Intent(Intent.ACTION_MAIN, null);
		intent.setAction("android.intent.action.MUSIC_PLAYER");
		intent.addCategory(Intent.CATEGORY_APP_MUSIC);
		List<ResolveInfo> apps = getPackageManager().queryIntentActivities(
				intent, 0);
		return apps;
	}
	
	public void onClickButton(View view){
		switch (view.getId()) {
		case R.id.btn_cancel:
			setResult(RESULT_CANCELED);
			finish();
			break;
		case R.id.btn_ok:
			ResolveInfo info = (ResolveInfo) mAdapter.getSelectItem();
			String packageName = info.activityInfo.packageName;
			Intent intent = new Intent();
			intent.setPackage(packageName);
			setResult(RESULT_OK, intent);
			finish();
			break;

		default:
			break;
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_footview:
			Intent intent = new Intent();
			intent.setClass(this, AppLauncherActivity.class);
			
			Bundle bundle = new Bundle();
			bundle.putBoolean("selectMode", true);
			bundle.putString("title", "选择默认播放器");
			
			intent.putExtras(bundle);
			
			startActivityForResult(intent, 0);
			break;

		default:
			break;
		}
	}
	
	private class MusicSelectAdapter extends BaseAdapter{
		LayoutInflater inflater = null;
		int currentSelectPos = -1;
		int defaultPos = -1;
		
		public MusicSelectAdapter(Context context, String defPackage){
			inflater = LayoutInflater.from(context);
			
			String packagename = "";
			for (int i = 0; i < mApps.size(); i++) {
				packagename = mApps.get(i).activityInfo.packageName;
				if (defPackage.equals(packagename)) {
					currentSelectPos = i;
				}
				
				if ("com.zhaoyan.gesture".equals(packagename)) {
					defaultPos = i;
				}
			}
			
			currentSelectPos = (currentSelectPos == -1) ? defaultPos : currentSelectPos;
		}
		
		public void setSelect(int position){
			currentSelectPos = position;
			notifyDataSetChanged();
		}
		
		public Object getSelectItem(){
			return mApps.get(currentSelectPos);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mApps.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return mApps.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = null;
			ViewHodler hodler = null;
			if (convertView == null) {
				view = inflater.inflate(R.layout.dialog_default_player_select_item, null);
				hodler = new ViewHodler();
				hodler.labelView = (TextView) view.findViewById(R.id.tv_player_label);
				hodler.logoView = (ImageView) view.findViewById(R.id.iv_player_logo);
				view.setTag(hodler);
			} else {
				hodler = (ViewHodler) convertView.getTag();
				view = convertView;
			}
			
			ResolveInfo info = mApps.get(position);
			String label = (String) info.loadLabel(getPackageManager());
			Drawable logoDrawable = info.loadIcon(getPackageManager());
			
			if (currentSelectPos == position) {
				view.setBackgroundResource(R.color.holo_blue_dark);
			} else {
				view.setBackgroundColor(Color.TRANSPARENT);
			}
			
			hodler.labelView.setText(label);
			hodler.logoView.setImageDrawable(logoDrawable);
			
			return view;
		}
		
		class ViewHodler{
			TextView labelView;
			ImageView logoView;
		}
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		mAdapter.setSelect(position);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		setResult(resultCode, data);
		this.finish();
	}

}
