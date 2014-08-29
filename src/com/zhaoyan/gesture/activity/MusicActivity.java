package com.zhaoyan.gesture.activity;

import com.zhaoyan.gesture.R;
import com.zhaoyan.gesture.music.ui.MusicBrowserActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MusicActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.music_main);
		
		Button openBtn = getView(R.id.btn_open_music);
		openBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(MusicActivity.this, MusicBrowserActivity.class);
				startActivity(intent);
			}
		});
		
		initTitle(R.string.main_music);
	}
	

}
