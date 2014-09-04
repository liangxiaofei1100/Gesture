package com.zhaoyan.gesture.music.ui;

import java.text.Collator;
import java.util.ArrayList;

import android.app.ListActivity;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteException;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.zhaoyan.gesture.R;
import com.zhaoyan.gesture.music.MediaPlaybackService;
import com.zhaoyan.gesture.music.MusicConf;
import com.zhaoyan.gesture.music.utils.MusicUtils;
import com.zhaoyan.gesture.music.utils.MusicUtils.Defs;
import com.zhaoyan.gesture.music.utils.MusicUtils.ServiceToken;

public class PlaylistBrowserActivity extends ListActivity implements
		View.OnCreateContextMenuListener, MusicUtils.Defs {
	private static final String TAG = "PlaylistBrowserActivity";
	private static final int DELETE_PLAYLIST = CHILD_MENU_BASE + 1;
	private static final int EDIT_PLAYLIST = CHILD_MENU_BASE + 2;
	private static final int RENAME_PLAYLIST = CHILD_MENU_BASE + 3;
	private static final int CHANGE_WEEKS = CHILD_MENU_BASE + 4;
	private static final long RECENTLY_ADDED_PLAYLIST = -1;
	private static final long ALL_SONGS_PLAYLIST = -2;
	private static final long PODCASTS_PLAYLIST = -3;
	private PlaylistListAdapter mAdapter;
	boolean mAdapterSent;
	private static int mLastListPosCourse = -1;
	private static int mLastListPosFine = -1;

	private Menu mMenu;
	private boolean mCreateShortcut;
	private ServiceToken mToken;
	private long mTimeSpan = -1;

	public PlaylistBrowserActivity() {
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		final Intent intent = getIntent();
		final String action = intent.getAction();
		if (Intent.ACTION_CREATE_SHORTCUT.equals(action)) {
			mCreateShortcut = true;
		}

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		MusicUtils.addActivity(this);
		mToken = MusicUtils.bindToService(this, new ServiceConnection() {
			public void onServiceConnected(ComponentName classname, IBinder obj) {
				if (Intent.ACTION_VIEW.equals(action)) {
					Bundle b = intent.getExtras();
					if (b == null) {
						Log.w(TAG, "Unexpected:getExtras() returns null.");
					} else {
						try {
							long id = Long.parseLong(b.getString("playlist"));
							if (id == RECENTLY_ADDED_PLAYLIST) {
								playRecentlyAdded();
							} else if (id == PODCASTS_PLAYLIST) {
								playPodcasts();
							} else if (id == ALL_SONGS_PLAYLIST) {
								long[] list = MusicUtils
										.getAllSongs(PlaylistBrowserActivity.this);
								if (list != null) {
									MusicUtils.playAll(
											PlaylistBrowserActivity.this, list,
											0);
								}
							} else {
								MusicUtils.playPlaylist(
										PlaylistBrowserActivity.this, id);
							}
						} catch (NumberFormatException e) {
							Log.w(TAG, "Playlist id missing or broken");
						}
					}
					finish();
					return;
				}
				MusicUtils.updateNowPlaying(PlaylistBrowserActivity.this);
			}

			public void onServiceDisconnected(ComponentName classname) {
			}

		});
		IntentFilter f = new IntentFilter();
		f.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
		f.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		f.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		f.addDataScheme("file");
		registerReceiver(mScanListener, f);

		setContentView(R.layout.media_picker_activity);
		try {
			getWindow().addFlags(
					WindowManager.LayoutParams.class.getField(
							"FLAG_NEEDS_MENU_KEY").getInt(null));
		} catch (Exception e) {
		}
		MusicUtils.updateButtonBar(this, R.id.playlisttab);
		ListView lv = getListView();
		lv.setOnCreateContextMenuListener(this);
		lv.setTextFilterEnabled(true);

		mAdapter = (PlaylistListAdapter) getLastNonConfigurationInstance();
		if (mAdapter == null) {
			// Log.i("@@@", "starting query");
			mAdapter = new PlaylistListAdapter(getApplication(), this,
					R.layout.track_list_item, mPlaylistCursor,
					new String[] { MediaStore.Audio.Playlists.NAME },
					new int[] { android.R.id.text1 });
			setListAdapter(mAdapter);
			setTitle(R.string.working_playlists);
			getPlaylistCursor(mAdapter.getQueryHandler(), null);
		} else {
			mAdapter.setActivity(this);
			setListAdapter(mAdapter);
			mPlaylistCursor = mAdapter.getCursor();
			// If mPlaylistCursor is null, this can be because it doesn't have
			// a cursor yet (because the initial query that sets its cursor
			// is still in progress), or because the query failed.
			// In order to not flash the error dialog at the user for the
			// first case, simply retry the query when the cursor is null.
			// Worst case, we end up doing the same query twice.
			if (mPlaylistCursor != null) {
				init(mPlaylistCursor);
			} else {
				setTitle(R.string.working_playlists);
				getPlaylistCursor(mAdapter.getQueryHandler(), null);
			}
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		PlaylistListAdapter a = mAdapter;
		mAdapterSent = true;
		return a;
	}

	@Override
	public void onDestroy() {
		ListView lv = getListView();
		if (lv != null) {
			mLastListPosCourse = lv.getFirstVisiblePosition();
			View cv = lv.getChildAt(0);
			if (cv != null) {
				mLastListPosFine = cv.getTop();
			}
		}
		MusicUtils.delActivity(this);
		MusicUtils.unbindFromService(mToken);
		// If we have an adapter and didn't send it off to another activity yet,
		// we should
		// close its cursor, which we do by assigning a null cursor to it. Doing
		// this
		// instead of closing the cursor directly keeps the framework from
		// accessing
		// the closed cursor later.
		if (!mAdapterSent && mAdapter != null) {
			mAdapter.changeCursor(null);
		}
		// Because we pass the adapter to the next activity, we need to make
		// sure it doesn't keep a reference to this activity. We can do this
		// by clearing its DatasetObservers, which setListAdapter(null) does.
		setListAdapter(null);
		mAdapter = null;
		unregisterReceiver(mScanListener);
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
		IntentFilter f = new IntentFilter();
		f.addAction(MediaPlaybackService.META_CHANGED);
		f.addAction(MediaPlaybackService.QUEUE_CHANGED);
		registerReceiver(mTrackListListener, f);
		mTrackListListener.onReceive(null, null);

		MusicUtils.setSpinnerState(this);
	}

	@Override
	public void onPause() {
		unregisterReceiver(mTrackListListener);
		mReScanHandler.removeCallbacksAndMessages(null);
		super.onPause();
	}

	@Override
	public void onStop() {
		MusicUtils.dismissExitDialog();
		super.onStop();
	}

	private BroadcastReceiver mTrackListListener = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			getListView().invalidateViews();
//			MusicUtils.updateNowPlaying(PlaylistBrowserActivity.this);
		}
	};

	private BroadcastReceiver mScanListener = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			MusicUtils.setSpinnerState(PlaylistBrowserActivity.this);
			mReScanHandler.sendEmptyMessage(0);
		}
	};

	private Handler mReScanHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (mAdapter != null) {
				getPlaylistCursor(mAdapter.getQueryHandler(), null);
			}
		}
	};

	public void init(Cursor cursor) {

		if (mAdapter == null) {
			return;
		}
		mAdapter.changeCursor(cursor);

		if (mPlaylistCursor == null) {
			MusicUtils.displayDatabaseError(this);
			closeContextMenu();
			mReScanHandler.sendEmptyMessageDelayed(0, 1000);
			return;
		}

		// restore previous position
		if (mLastListPosCourse >= 0) {
			getListView().setSelectionFromTop(mLastListPosCourse,
					mLastListPosFine);
			mLastListPosCourse = -1;
		}

		MusicUtils.hideDatabaseError(this);
		if (mPlaylistCursor.getCount() == 0
				&& MusicUtils.canDisplayHintMessage(mTimeSpan)) {
			MusicUtils.displayHintMessage(this);
		}
		mTimeSpan = System.currentTimeMillis();
		MusicUtils.updateButtonBar(this, R.id.playlisttab);
		setTitle();
		updateMenu(mMenu);
	}

	private void setTitle() {
		setTitle(R.string.playlists_title);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mCreateShortcut) {
			menu.add(0, NEW_PLAYLIST, 0, R.string.new_playlist).setIcon(
					R.drawable.ic_menu_new_playlist);
			menu.add(0, DELETE_PLAYLIST, 0, R.string.delete_playlist).setIcon(
					R.drawable.ic_menu_delete_playlist);
			menu.add(0, PARTY_SHUFFLE, 0, R.string.party_shuffle); // icon will
																	// be set in
																	// onPrepareOptionsMenu()
			menu.add(0, EXIT_APP, 0, R.string.exit).setIcon(
					R.drawable.ic_menu_exit);
			mMenu = menu;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MusicUtils.setPartyShuffleMenuIcon(menu);
		updateMenu(menu);
		return super.onPrepareOptionsMenu(menu);
	}

	private void updateMenu(Menu menu) {
		if (menu == null)
			return;

		MenuItem item = menu.findItem(DELETE_PLAYLIST);
		if (item != null) {
			item.setVisible(mAdapter != null && mAdapter.getCount() > 1);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case NEW_PLAYLIST:
			intent = new Intent(this, CreatePlaylist.class);
			startActivityForResult(intent, NEW_PLAYLIST);
			break;
		case DELETE_PLAYLIST:
			intent = new Intent(this, DeletePlaylistActivity.class);
			startActivity(intent);
			break;
		case PARTY_SHUFFLE:
			MusicUtils.togglePartyShuffle();
			break;
		case EXIT_APP:
			MusicUtils.exitApp(this);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenuInfo menuInfoIn) {
		if (mCreateShortcut) {
			return;
		}

		AdapterContextMenuInfo mi = (AdapterContextMenuInfo) menuInfoIn;

		menu.add(0, PLAY_SELECTION, 0, R.string.play_selection);

		if (mi.id >= 0 /* || mi.id == PODCASTS_PLAYLIST */) {
			menu.add(0, DELETE_PLAYLIST, 0, R.string.delete_playlist_menu);
		}

		if (mi.id == RECENTLY_ADDED_PLAYLIST) {
			menu.add(0, EDIT_PLAYLIST, 0, R.string.edit_playlist_menu);
		}

		if (mi.id >= 0) {
			menu.add(0, RENAME_PLAYLIST, 0, R.string.rename_playlist_menu);
		}

		mPlaylistCursor.moveToPosition(mi.position);
		menu.setHeaderTitle(mPlaylistCursor.getString(mPlaylistCursor
				.getColumnIndexOrThrow(MediaStore.Audio.Playlists.NAME)));
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo mi = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case PLAY_SELECTION:
			if (mi.id == RECENTLY_ADDED_PLAYLIST) {
				playRecentlyAdded();
			} else if (mi.id == PODCASTS_PLAYLIST) {
				playPodcasts();
			} else {
				MusicUtils.playPlaylist(this, mi.id);
			}
			break;
		case DELETE_PLAYLIST:
			Uri uri = ContentUris.withAppendedId(
					MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, mi.id);
			getContentResolver().delete(uri, null, null);
			Toast.makeText(this, R.string.playlist_deleted_message,
					Toast.LENGTH_SHORT).show();
			if (mPlaylistCursor.getCount() == 0) {
				setTitle(R.string.no_playlists_title);
			}
			break;
		case EDIT_PLAYLIST:
			if (mi.id == RECENTLY_ADDED_PLAYLIST) {
				Intent intent = new Intent();
				intent.setClass(this, WeekSelector.class);
				startActivityForResult(intent, CHANGE_WEEKS);
				return true;
			} else {
				Log.e(TAG, "should not be here");
			}
			break;
		case RENAME_PLAYLIST:
			Intent intent = new Intent();
			intent.setClass(this, RenamePlaylist.class);
			intent.putExtra("rename", mi.id);
			startActivityForResult(intent, RENAME_PLAYLIST);
			break;
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		switch (requestCode) {
		case SCAN_DONE:
			if (resultCode == RESULT_CANCELED) {
				finish();
			} else if (mAdapter != null) {
				getPlaylistCursor(mAdapter.getQueryHandler(), null);
			}
			break;
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (mCreateShortcut) {
			final Intent shortcut = new Intent();
			shortcut.setAction(Intent.ACTION_VIEW);
			shortcut.setDataAndType(Uri.EMPTY,
					"vnd.android.cursor.dir/playlist");
			shortcut.putExtra("playlist", String.valueOf(id));

			final Intent intent = new Intent();
			intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcut);
			intent.putExtra(Intent.EXTRA_SHORTCUT_NAME,
					((TextView) v.findViewById(R.id.line1)).getText());
			intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
					Intent.ShortcutIconResource.fromContext(this,
							R.drawable.ic_launcher_shortcut_music_playlist));

			setResult(RESULT_OK, intent);
			finish();
			return;
		}
		if (id == RECENTLY_ADDED_PLAYLIST) {
			// Intent intent = new Intent(Intent.ACTION_PICK);
			Intent intent = new Intent(MusicConf.ZY_MUSIC_ACTION);
			intent.setDataAndType(Uri.EMPTY, "vnd.android.cursor.dir/track");
			intent.putExtra("playlist", "recentlyadded");
			startActivity(intent);
		} else if (id == PODCASTS_PLAYLIST) {
			// Intent intent = new Intent(Intent.ACTION_PICK);
			Intent intent = new Intent(MusicConf.ZY_MUSIC_ACTION);
			intent.setDataAndType(Uri.EMPTY, "vnd.android.cursor.dir/track");
			intent.putExtra("playlist", "podcasts");
			startActivity(intent);
		} else {
			Intent intent = new Intent(Intent.ACTION_EDIT);
			intent.setDataAndType(Uri.EMPTY, "vnd.android.cursor.dir/track");
			intent.putExtra("playlist", Long.valueOf(id).toString());
			startActivity(intent);
		}
	}

	private void playRecentlyAdded() {
		// do a query for all songs added in the last X weeks
		int X = MusicUtils.getIntPref(this, "numweeks", 2) * (3600 * 24 * 7);
		final String[] ccols = new String[] { MediaStore.Audio.Media._ID };
		String where = MediaStore.MediaColumns.DATE_ADDED + ">"
				+ (System.currentTimeMillis() / 1000 - X);
		Cursor cursor = MusicUtils.query(this,
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, ccols, where,
				null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

		if (cursor == null) {
			// Todo: show a message
			return;
		}
		try {
			int len = cursor.getCount();
			long[] list = new long[len];
			for (int i = 0; i < len; i++) {
				cursor.moveToNext();
				list[i] = cursor.getLong(0);
			}
			MusicUtils.playAll(this, list, 0);
		} catch (SQLiteException ex) {
		} finally {
			cursor.close();
		}
	}

	private void playPodcasts() {
		// do a query for all files that are podcasts
		final String[] ccols = new String[] { MediaStore.Audio.Media._ID };
		Cursor cursor = MusicUtils.query(this,
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, ccols,
				MediaStore.Audio.Media.IS_PODCAST + "=1", null,
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

		if (cursor == null) {
			// Todo: show a message
			return;
		}
		try {
			int len = cursor.getCount();
			long[] list = new long[len];
			for (int i = 0; i < len; i++) {
				cursor.moveToNext();
				list[i] = cursor.getLong(0);
			}
			MusicUtils.playAll(this, list, 0);
		} catch (SQLiteException ex) {
		} finally {
			cursor.close();
		}
	}

	String[] mCols = new String[] { MediaStore.Audio.Playlists._ID,
			MediaStore.Audio.Playlists.NAME };

	private Cursor getPlaylistCursor(AsyncQueryHandler async,
			String filterstring) {

		StringBuilder where = new StringBuilder();
		where.append(MediaStore.Audio.Playlists.NAME + " != ''");

		// Add in the filtering constraints
		String[] keywords = null;
		if (filterstring != null) {
			String[] searchWords = filterstring.split(" ");
			keywords = new String[searchWords.length];
			Collator col = Collator.getInstance();
			col.setStrength(Collator.PRIMARY);
			for (int i = 0; i < searchWords.length; i++) {
				keywords[i] = '%' + searchWords[i] + '%';
			}
			for (int i = 0; i < searchWords.length; i++) {
				where.append(" AND ");
				where.append(MediaStore.Audio.Playlists.NAME + " LIKE ?");
			}
		}

		String whereclause = where.toString();

		if (async != null) {
			async.startQuery(0, null,
					MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, mCols,
					whereclause, keywords, MediaStore.Audio.Playlists.NAME);
			return null;
		}
		Cursor c = null;
		c = MusicUtils.query(this,
				MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, mCols,
				whereclause, keywords, MediaStore.Audio.Playlists.NAME);

		return mergedCursor(c);
	}

	private Cursor mergedCursor(Cursor c) {
		if (c == null) {
			return null;
		}
		if (c instanceof MergeCursor) {
			// this shouldn't happen, but fail gracefully
			Log.d("PlaylistBrowserActivity", "Already wrapped");
			return c;
		}
		MatrixCursor autoplaylistscursor = new MatrixCursor(mCols);
		if (mCreateShortcut) {
			ArrayList<Object> all = new ArrayList<Object>(2);
			all.add(ALL_SONGS_PLAYLIST);
			all.add(getString(R.string.play_all));
			autoplaylistscursor.addRow(all);
		}
		ArrayList<Object> recent = new ArrayList<Object>(2);
		recent.add(RECENTLY_ADDED_PLAYLIST);
		recent.add(getString(R.string.recentlyadded));
		autoplaylistscursor.addRow(recent);

		// check if there are any podcasts
		Cursor counter = MusicUtils.query(this,
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				new String[] { "count(*)" }, "is_podcast=1", null, null);
		if (counter != null) {
			counter.moveToFirst();
			int numpodcasts = counter.getInt(0);
			counter.close();
			if (numpodcasts > 0) {
				ArrayList<Object> podcasts = new ArrayList<Object>(2);
				podcasts.add(PODCASTS_PLAYLIST);
				podcasts.add(getString(R.string.podcasts_listitem));
				autoplaylistscursor.addRow(podcasts);
			}
		}

		Cursor cc = new MergeCursor(new Cursor[] { autoplaylistscursor, c });
		return cc;
	}

	static class PlaylistListAdapter extends SimpleCursorAdapter {
		int mTitleIdx;
		int mIdIdx;
		private PlaylistBrowserActivity mActivity = null;
		private AsyncQueryHandler mQueryHandler;
		private String mConstraint = null;
		private boolean mConstraintIsValid = false;

		class QueryHandler extends AsyncQueryHandler {
			QueryHandler(ContentResolver res) {
				super(res);
			}

			@Override
			protected void onQueryComplete(int token, Object cookie,
					Cursor cursor) {
				// Log.i("@@@", "query complete: " + cursor.getCount() + "   " +
				// mActivity);
				if (cursor != null) {
					cursor = mActivity.mergedCursor(cursor);
				}
				mActivity.init(cursor);
			}
		}

		PlaylistListAdapter(Context context,
				PlaylistBrowserActivity currentactivity, int layout,
				Cursor cursor, String[] from, int[] to) {
			super(context, layout, cursor, from, to);
			mActivity = currentactivity;
			getColumnIndices(cursor);
			mQueryHandler = new QueryHandler(context.getContentResolver());
		}

		private void getColumnIndices(Cursor cursor) {
			if (cursor != null) {
				mTitleIdx = cursor
						.getColumnIndexOrThrow(MediaStore.Audio.Playlists.NAME);
				mIdIdx = cursor
						.getColumnIndexOrThrow(MediaStore.Audio.Playlists._ID);
			}
		}

		public void setActivity(PlaylistBrowserActivity newactivity) {
			mActivity = newactivity;
		}

		public AsyncQueryHandler getQueryHandler() {
			return mQueryHandler;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {

			long id = cursor.getLong(mIdIdx);

			TextView tv = (TextView) view.findViewById(R.id.line1);
			String name = cursor.getString(mTitleIdx);

			ImageView iv = (ImageView) view.findViewById(R.id.iv_app_logo);
			if (id == RECENTLY_ADDED_PLAYLIST) {
				tv.setText(R.string.recentlyadded);
				iv.setImageResource(R.drawable.ic_mp_playlist_recently_added_list);
			} else {
				tv.setText(name);
				iv.setImageResource(R.drawable.ic_mp_playlist_list);
			}
			ViewGroup.LayoutParams p = iv.getLayoutParams();
			p.width = ViewGroup.LayoutParams.WRAP_CONTENT;
			p.height = ViewGroup.LayoutParams.WRAP_CONTENT;

			iv = (ImageView) view.findViewById(R.id.play_indicator);
			iv.setVisibility(View.GONE);

			view.findViewById(R.id.line2).setVisibility(View.GONE);
		}

		@Override
		public void changeCursor(Cursor cursor) {
			if (mActivity.isFinishing() && cursor != null) {
				cursor.close();
				cursor = null;
			}
			if (cursor != mActivity.mPlaylistCursor) {
				if ((cursor != null) && cursor.isClosed()) {
					return;
				}
				mActivity.mPlaylistCursor = cursor;
				super.changeCursor(cursor);
				getColumnIndices(cursor);
			}
		}

		@Override
		public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
			String s = constraint.toString();
			if (mConstraintIsValid
					&& ((s == null && mConstraint == null) || (s != null && s
							.equals(mConstraint)))) {
				return getCursor();
			}
			Cursor c = mActivity.getPlaylistCursor(null, s);
			mConstraint = s;
			mConstraintIsValid = true;
			return c;
		}
	}

	private Cursor mPlaylistCursor;
}
