package com.zhaoyan.gesture.activity;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhaoyan.gesture.R;
import com.zhaoyan.gesture.music.MusicConf;
import com.zhaoyan.gesture.music.ui.MusicBrowserActivity;

public class MusicActivity extends BaseActivity {
	private static final String TAG = MusicActivity.class.getSimpleName();
	
	private TextView mSetPlayerSummary, mSetPlayListSummary;
	private ImageView mLogoView;
	
	private String packageName = "";
	private PackageManager mPackageManager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.music_main);
		
		initTitle(R.string.main_music);
		mBaseIntroductionView.setIntentExtraName(getString(R.string.gesture_music));
		mBaseIntroductionView.setIntroductionText(getString(R.string.introduction_music));
		mSetPlayerSummary = getView(R.id.tv_player_label);
		mLogoView = getView(R.id.iv_player_logo);
		
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
		Intent intent = new Intent();
		intent.setClass(this, PlayerSelectActivity.class);
		startActivityForResult(intent, 0);
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
	

}
