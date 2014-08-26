package com.zhaoyan.gesture.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;

import com.zhaoyan.gesture.MediaInfo;

public class MusicPlayerService extends Service {
	private static final String TAG = MusicPlayerService.class.getSimpleName();
	private final IBinder mBinder = new LocalBinder();

	private MediaPlayer mMediaPlayer = null;

	// save audios
	private List<MediaInfo> mMusicList = new ArrayList<MediaInfo>();
	private int mCurrentPosition = -1;

	private QueryHandler mQueryHandler = null;

	private static final String[] PROJECTION = { MediaStore.Audio.Media._ID,
			MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DURATION,
			MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.IS_MUSIC,
			MediaStore.Audio.Media.DISPLAY_NAME };
	
	private boolean isLoadFinish = false;

	public static final String PLAYER_PREPARE_END = "com.zhaoyao.juyou.musicplayerservice.prepared";
	public static final String PLAY_COMPLETED = "com.zhaoyao.juyou.musicplayerservice.playcompleted";
	public static final String PLAYER_NOT_PREPARE = "com.zhaoyao.juyou.musicplayerservice.notprepare";

	MediaPlayer.OnCompletionListener mCompleteListener = new MediaPlayer.OnCompletionListener() {
		public void onCompletion(MediaPlayer mp) {
			if (mCurrentPosition < mMusicList.size()) {
				next();
			}
//			broadcastEvent(PLAY_COMPLETED);
		}
	};

	MediaPlayer.OnPreparedListener mPrepareListener = new MediaPlayer.OnPreparedListener() {
		public void onPrepared(MediaPlayer mp) {
			broadcastEvent(PLAYER_PREPARE_END);
		}
	};

	private void broadcastEvent(String what) {
		Intent i = new Intent(what);
		sendBroadcast(i);
	}

	public void onCreate() {
		super.onCreate();

		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setOnPreparedListener(mPrepareListener);
		mMediaPlayer.setOnCompletionListener(mCompleteListener);
		mMediaPlayer.setOnErrorListener(new OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				Log.e(TAG, "onError.what:" + what + ",extra:" + extra);
				if (what == -38) {
					broadcastEvent(PLAYER_NOT_PREPARE);
					return true;
				}
				return false;
			}
		});

		mQueryHandler = new QueryHandler(getContentResolver());
		queryMusic();
	}

	public class LocalBinder extends Binder {
		public MusicPlayerService getService() {
			return MusicPlayerService.this;
		}
	}

	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public void setDataSource(String path) {

		try {
			mMediaPlayer.reset();
			mMediaPlayer.setDataSource(path);
			mMediaPlayer.prepare();
		} catch (IOException e) {
			return;
		} catch (IllegalArgumentException e) {
			return;
		}
	}
	
	public void startMusic(){
		while (isLoadFinish) {
			mCurrentPosition = 0;
			doPlay(mCurrentPosition);
			return;
		}
	}
	
	public void next(){
		if (mCurrentPosition == -1) {
			return;
		}
		mCurrentPosition += 1;
		if (mCurrentPosition >= mMusicList.size()) {
			mCurrentPosition = 0;
		}
		doPlay(mCurrentPosition);
	}
	
	public void previous(){
		if (mCurrentPosition == -1) {
			return;
		}
		
		mCurrentPosition -= 1;
		if (mCurrentPosition < 0) {
			mCurrentPosition = mMusicList.size() - 1;
		}
		
		doPlay(mCurrentPosition);
	}
	
	private void doPlay(String path){
		Log.d(TAG, "doPlay:" + path);
		setDataSource(path);
		start();
	}
	
	private void doPlay(int position){
		String path = mMusicList.get(position).getUrl();
		doPlay(path);
	}

	public void start() {
		mMediaPlayer.start();
	}

	public void stop() {
		mMediaPlayer.stop();
	}

	public void pause() {
		mMediaPlayer.pause();
	}

	public boolean isPlaying() {
		return mMediaPlayer.isPlaying();
	}

	public int getDuration() {
		return mMediaPlayer.getDuration();
	}

	public int getPosition() {
		return mMediaPlayer.getCurrentPosition();
	}

	public long seek(long whereto) {
		mMediaPlayer.seekTo((int) whereto);
		return whereto;
	}

	/**
	 * Query Audio from Audio DB
	 */
	public void queryMusic() {
		// just show music files
		String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
		mQueryHandler.startQuery(0, null,
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, PROJECTION,
				selection, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
	}

	// query db
	private class QueryHandler extends AsyncQueryHandler {

		public QueryHandler(ContentResolver cr) {
			super(cr);
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			Log.d(TAG, "onQueryComplete");
			if (null != cursor) {
				if (cursor.moveToFirst()) {
					do {
						MediaInfo mediaInfo = new MediaInfo();
						long id = cursor.getLong(cursor
								.getColumnIndex(MediaColumns._ID));
						String title = cursor.getString(cursor
								.getColumnIndex(MediaColumns.TITLE));
						String url = cursor.getString(cursor
								.getColumnIndex(MediaStore.MediaColumns.DATA));
						String name = cursor.getString(cursor
								.getColumnIndex(MediaColumns.DISPLAY_NAME));

						mediaInfo.setId(id);
						mediaInfo.setTitle(title);
						mediaInfo.setDisplayName(name);
						mediaInfo.setUrl(url);

						mMusicList.add(mediaInfo);
					} while (cursor.moveToNext());
					cursor.close();
					Log.d(TAG, "Load Music finish");
					//test
//					for (MediaInfo mediaInfo : mMusicList) {
//						Log.d(TAG, "" + mediaInfo.getTitle());
//					}
					//test
					isLoadFinish = true;
				}
			}
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
		}
		super.onDestroy();
	}
}
