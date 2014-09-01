package com.zhaoyan.gesture.activity;

import com.zhaoyan.gesture.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class TestActivity extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
		
//		int bundle = getIntent().getIntExtra("test", 0);
		String bundle = getIntent().getStringExtra("test");
		System.out.println("bundler:"  + bundle);
		
		TextView textView = (TextView) findViewById(R.id.textview);
		
		textView.setText("You have click the "  + bundle + " button");
	}

}
