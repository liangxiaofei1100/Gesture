package com.zhaoyan.gesture.music.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhaoyan.gesture.R;
import com.zhaoyan.gesture.music.ui.MediaPlaybackActivity2;
import com.zhaoyan.gesture.music.utils.MusicUtils;
import com.zhaoyan.gesture.util.ZyLog;

public class BottomActionBar extends LinearLayout implements OnClickListener,
		OnLongClickListener {
	private static final String TAG = BottomActionBar.class.getSimpleName();

	public BottomActionBar(Context context) {
		super(context);
	}

	public BottomActionBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOnClickListener(this);
		setOnLongClickListener(this);
	}

	public BottomActionBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * Updates the bottom ActionBar's info
	 * 
	 * @param activity
	 * @throws RemoteException
	 */
	public void updateBottomActionBar(Activity activity) {
		ZyLog.d(TAG, "updateBottomActionBar:" + activity);
		View bottomActionBar = activity.findViewById(R.id.bottom_action_bar);
		if (bottomActionBar == null) {
			return;
		}
		try {
			if (MusicUtils.sService != null
					&& MusicUtils.getCurrentAudioId() != -1) {

				// Track name
				TextView mTrackName = (TextView) bottomActionBar
						.findViewById(R.id.bottom_action_bar_track_name);

				mTrackName.setText(MusicUtils.sService.getTrackName());

				// Artist name
				TextView mArtistName = (TextView) bottomActionBar
						.findViewById(R.id.bottom_action_bar_artist_name);
				mArtistName.setText(MusicUtils.sService.getArtistName());

				// // Album art
				// ImageView mAlbumArt = (ImageView)bottomActionBar
				// .findViewById(R.id.bottom_action_bar_album_art);

				// ImageInfo mInfo = new ImageInfo();
				// mInfo.type = TYPE_ALBUM;
				// mInfo.size = SIZE_THUMB;
				// mInfo.source = SRC_FIRST_AVAILABLE;
				// mInfo.data = new String[]{
				// String.valueOf(MusicUtils.getCurrentAlbumId()) ,
				// MusicUtils.sService.getArtistName(),
				// MusicUtils.sService.getAlbumName() };

				// ImageProvider.getInstance( activity ).loadImage( mAlbumArt ,
				// mInfo );

				// Divider
				ImageView mDivider = (ImageView) activity
						.findViewById(R.id.bottom_action_bar_info_divider);

			} else {
				ZyLog.e(TAG, "updateBottomActionBar service is null");
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bottom_action_bar:
			Intent intent = new Intent();
			intent.setClass(v.getContext(), MediaPlaybackActivity2.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_NEW_TASK);
			v.getContext().startActivity(intent);
			break;
		default:
			break;
		}

	}

	@Override
	public boolean onLongClick(View v) {
		// Context context = v.getContext();
		// context.startActivity(new Intent(context, QuickQueue.class));
		return true;
	}

}
