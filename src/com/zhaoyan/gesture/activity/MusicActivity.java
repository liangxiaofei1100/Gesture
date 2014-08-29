package com.zhaoyan.gesture.activity;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhaoyan.gesture.R;
import com.zhaoyan.gesture.music.ui.MusicBrowserActivity;

public class MusicActivity extends BaseActivity {
	private static final String TAG = MusicActivity.class.getSimpleName();
	
	private TextView mSetPlayerSummary, mSetPlayListSummary;
	private ImageView mLogoView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.music_main);
		
		initTitle(R.string.main_music);
		
		mSetPlayerSummary = getView(R.id.tv_player_label);
		mLogoView = getView(R.id.iv_player_logo);
	}
	
	public void openMusicPlayer(View view){
		Intent intent = new Intent();
		intent.setClass(MusicActivity.this, MusicBrowserActivity.class);
//		intent.setAction("android.intent.action.MUSIC_PLAYER");
//		intent.addCategory(Intent.CATEGORY_APP_MUSIC);
		startActivity(intent);
	}
	
	public void setMusicPlayer(View view){
		Intent intent = new Intent();
		intent.setClass(this, PlayerSelectActivity.class);
		startActivityForResult(intent, 0);
	}
	

	public void setPlayList(View view){
	
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (RESULT_OK == resultCode) {
			String packagename = data.getPackage();
			try {
				ApplicationInfo info = getPackageManager().getApplicationInfo(packagename, 0);
				mSetPlayerSummary.setText(info.loadLabel(getPackageManager()));
				mLogoView.setImageDrawable(info.loadIcon(getPackageManager()));
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	

}
