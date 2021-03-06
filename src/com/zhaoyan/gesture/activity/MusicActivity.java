package com.zhaoyan.gesture.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhaoyan.common.dialog.DefaultAppInfo;
import com.zhaoyan.common.dialog.DefaultAppChoiceDialog;
import com.zhaoyan.common.dialog.DefaultAppChoiceDialog.OnLoadMoreListener;
import com.zhaoyan.common.dialog.ZyDialogBuilder;
import com.zhaoyan.common.dialog.ZyDialogBuilder.onZyDialogClickListener;
import com.zhaoyan.gesture.R;
import com.zhaoyan.gesture.app.AppLauncherActivity;
import com.zhaoyan.gesture.music.MusicConf;
import com.zhaoyan.gesture.music.ui.MusicBrowserActivity;

public class MusicActivity extends BaseActivity {
	private static final String TAG = MusicActivity.class.getSimpleName();
	
	private TextView mSetPlayerSummary, mSetPlayListSummary;
	private ImageView mLogoView;
	
	private String packageName = "";
	private PackageManager mPackageManager;
	private View mChoose0,mChoose1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.music_main);
		
		initTitle(R.string.main_music);
		mBaseIntroductionView.setIntentExtraName(getString(R.string.main_music));
		mBaseIntroductionView.setIntroductionText(getString(R.string.introduction_music));
		mSetPlayerSummary = getView(R.id.tv_player_label);
		mLogoView = getView(R.id.iv_player_logo);
		mChoose0 = findViewById(R.id.gestrue_try_0);
		mChoose1 = findViewById(R.id.gestrue_try_1);
		mChoose0.setOnClickListener(myClickListener);
		mChoose1.setOnClickListener(myClickListener);
		
		packageName = MusicConf.getStringPref(getApplicationContext(), "package", "com.zhaoyan.gesture");
		
		mPackageManager = getPackageManager();
		updatePlayer();
	}
	
	public void openMusicPlayer(View view){
		Intent intent = null;
		if (packageName == null || packageName.equals("com.zhaoyan.gesture")) {
			intent = new Intent();
			intent.setClass(MusicActivity.this, MusicBrowserActivity.class);
		} else {
			intent = mPackageManager.getLaunchIntentForPackage(packageName);
		}
		startActivity(intent);
	}
	
	public void setMusicPlayer(View view){
		final List<DefaultAppInfo> apps = getMusicApps();

		final DefaultAppChoiceDialog choiceDialog = new DefaultAppChoiceDialog(this, apps);
		choiceDialog.setDialogTitle("设置默认音乐播放器");
		choiceDialog.setNegativeButton(R.string.cancel, null);
		choiceDialog.setPositiveButton(R.string.ok, new onZyDialogClickListener() {
			@Override
			public void onClick(Dialog dialog) {
				DefaultAppInfo info = choiceDialog.getChoiceItem();
				packageName = info.getPackageName();
				MusicConf.setStringPref(getApplicationContext(), "package", packageName);
				updatePlayer();
				
				dialog.cancel();
			}
		});
		choiceDialog.setLoadMoreClick(new OnLoadMoreListener() {
			@Override
			public void onLoadMore(ZyDialogBuilder dialogBuilder) {
				Intent intent = new Intent();
				intent.setClass(MusicActivity.this, AppLauncherActivity.class);
				
				Bundle bundle = new Bundle();
				bundle.putBoolean("selectMode", true);
				bundle.putString("title", "选择默认播放器");
				
				intent.putExtras(bundle);
				
				startActivityForResult(intent, 0);
				
				dialogBuilder.cancel();
			}
		});
		choiceDialog.show();
	}
	
	private boolean exist = false;
	private List<DefaultAppInfo> getMusicApps() {
		List<DefaultAppInfo> infoList = new ArrayList<DefaultAppInfo>();
		
		Intent intent = new Intent(Intent.ACTION_MAIN, null);
		intent.setAction("android.intent.action.MUSIC_PLAYER");
		intent.addCategory(Intent.CATEGORY_APP_MUSIC);
		List<ResolveInfo> apps = getPackageManager().queryIntentActivities(
				intent, 0);
		
		String packagename = "";
		String label = "";
		Drawable logo = null;
		DefaultAppInfo info = null;
		for (int i = 0; i < apps.size(); i++) {
			info = new DefaultAppInfo();
			packagename = apps.get(i).activityInfo.packageName;
			label = (String) apps.get(i).loadLabel(mPackageManager);
			logo = apps.get(i).loadIcon(mPackageManager);
			
			info.setPackageName(packagename);
			info.setLabel(label);
			info.setLogo(logo);
			
			infoList.add(info);
			if (packagename.equals(packageName)) {
				info.setChoice(true);
				exist = true;
			}
		}

		if (!exist) {
			String preLabel = "";
			Drawable preDrawable = null;
			try {
				ApplicationInfo applicationInfo = mPackageManager.getApplicationInfo(packageName, 0);
				preLabel = (String) applicationInfo.loadLabel(mPackageManager);
				preDrawable = applicationInfo.loadIcon(mPackageManager);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			
			info = new DefaultAppInfo();
			info.setPackageName(packageName);
			info.setLabel(preLabel);
			info.setLogo(preDrawable);
			info.setChoice(true);
			
			infoList.add(0, info);
		}
		exist = false;
		return infoList;
	}
	
	
	public int getDefaultChoiceItem(List<ResolveInfo> list){
		String string = "";
		for (int i = 0; i < list.size(); i++) {
			string = list.get(i).activityInfo.packageName;
			if (packageName.equals(string)) {
				return i;
			}
		}
		return -1;
	}
	

	public void setPlayList(View view){
	
	}
	
	private void updatePlayer(){
		try {
			ApplicationInfo info = mPackageManager.getApplicationInfo(packageName, 0);
			mSetPlayerSummary.setText(info.loadLabel(mPackageManager));
			mLogoView.setImageDrawable(info.loadIcon(mPackageManager));
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (RESULT_OK == resultCode) {
			packageName = data.getPackage();
			MusicConf.setStringPref(getApplicationContext(), "package", packageName);
			updatePlayer();
		}
	}
	
	private OnClickListener myClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.gestrue_try_0:
				Intent intent = new Intent();
				intent.setClass(MusicActivity.this, GestureShowActivity.class);
				intent.putExtra("name", "上一首");
				MusicActivity.this.startActivity(intent);
				break;
			case R.id.gestrue_try_1:
				 intent = new Intent();
				intent.setClass(MusicActivity.this, GestureShowActivity.class);
				intent.putExtra("name", "下一首");
				MusicActivity.this.startActivity(intent);
				break;

			default:
				break;
			}
		}
	};

}
