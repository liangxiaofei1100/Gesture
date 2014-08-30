package com.zhaoyan.gesture.activity;

import java.io.File;
import java.util.ArrayList;

import com.zhaoyan.gesture.R;
import com.zhaoyan.gesture.util.CopyFile;

import android.app.Activity;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.gesture.GestureOverlayView.OnGestureListener;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class GestureShowActivity extends Activity implements OnGestureListener {
	private Gesture mGesture;
	private static GestureLibrary sStore;
	private GestureOverlayView overlayView;
	private TextView mTv, mScoreTv;
	private ImageView mImageView;
	private Bitmap mBackgroundBitmap;
	private String name;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gesture_show_layout);
		File f = new File(getFilesDir(), "gestures");
		CopyFile.copyFile(this, f.getAbsolutePath(), "");
		sStore = GestureLibraries.fromFile(f);
		name = getIntent().getStringExtra("name");
		if (name != null) {
			getGesture(name);
		}
		mTv = (TextView) findViewById(R.id.show_gesture_introduction_tv);
		mScoreTv = (TextView) findViewById(R.id.show_gesture_introduction_tv_score);
		mImageView = (ImageView) findViewById(R.id.show_gesture);
		overlayView = (GestureOverlayView) findViewById(R.id.show_gestures_overlay);
		overlayView.addOnGestureListener(this);
		mImageView.getViewTreeObserver().addOnPreDrawListener(
				new OnPreDrawListener() {

					@Override
					public boolean onPreDraw() {
						// TODO Auto-generated method stub
						if (mGesture != null) {
							mBackgroundBitmap = mGesture.toBitmap(300, 400, 10,
									Color.GREEN);
							mImageView.setImageBitmap(mBackgroundBitmap);
						}
						return true;
					}
				});
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mTv.setText("参照轨迹练习手势");
	}

	private void getGesture(String name) {
		if (sStore != null) {
			sStore.load();
			for (Gesture gesture : sStore.getGestures(name)) {
				mGesture = gesture;
			}
		}

	}

	@Override
	public void onGesture(GestureOverlayView arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onGestureCancelled(GestureOverlayView overlay, MotionEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onGestureEnded(GestureOverlayView overlay, MotionEvent event) {
		// TODO Auto-generated method stub
		ArrayList<Prediction> predictions = sStore.recognize(overlay
				.getGesture());
		if (!predictions.isEmpty()) {
			for (Prediction p : predictions) {
				if (name != null && p.name.equals(name)) {
					if (p.score >= 10)
						mScoreTv.setText("匹配成功");
					else
						mScoreTv.setText("匹配失败");
				}
			}
		}
	}

	@Override
	public void onGestureStarted(GestureOverlayView overlay, MotionEvent event) {
		// TODO Auto-generated method stub
		mScoreTv.setText("");
	}
}
