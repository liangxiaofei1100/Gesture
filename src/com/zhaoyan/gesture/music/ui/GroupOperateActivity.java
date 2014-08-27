package com.zhaoyan.gesture.music.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.zhaoyan.gesture.R;
import com.zhaoyan.gesture.music.utils.MusicUtils;
import com.zhaoyan.gesture.music.utils.MusicUtils.Defs;

public class GroupOperateActivity extends ListActivity implements OnItemClickListener, MusicUtils.Defs {

    private static final int MSG_SHOW = 0;
    private static final int MSG_HIDE = 1;
    private static final int MSG_TOAST = 2;
    private static final int MSG_CHANGE = 3;

    private static final int GROUP_ADD = CHILD_MENU_BASE + 7;

    Menu mMenu;
//    private static final String EXTERNAL_SD_PATH = Environment.getSecondaryStorageDirectory().getPath();
  private static final String EXTERNAL_SD_PATH = Environment.getExternalStorageDirectory().getPath();

    String mToast;
    TextView mHintText;
    MusicListAdapter mAdapter;
    ContentResolver mResolver;
    MyContentObserver mObserver;
    ArrayList<ViewHolder> mMusicList = new ArrayList<ViewHolder>();
    ArrayList<ViewHolder> mTmpList = new ArrayList<ViewHolder>();

    boolean mDestroy = false;
    boolean mRegisteredReceiver = false;
    boolean mRegisteredObserver = false;
    boolean mProgressBarVisibile = false;
    int mOperateId = -1;
    String mPlaylistId = null;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!mDestroy) {
                GroupOperateActivity.this.finish();
            }
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mDestroy) return;
            //
            switch(msg.what) {
            case MSG_SHOW:
                showProgress(true);
                break;
            case MSG_HIDE:
                showProgress(false);
                break;
            case MSG_TOAST:
                showToast();
                break;
            case MSG_CHANGE:
                prepare();
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MusicUtils.addActivity(this);

        Intent intent = getIntent();
        mOperateId = intent.getIntExtra("operate_id", -1);
        mPlaylistId = intent.getStringExtra("playlist");
        String state = Environment.getExternalStorageState();
        if (mOperateId == -1 || mPlaylistId == null || !Environment.MEDIA_MOUNTED.equals(state)) {
            finish();
            return;
        }

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setTitle((mOperateId == GROUP_ADD) ? R.string.group_add : R.string.group_delete);
        setContentView(R.layout.operate_list);

        mHintText = (TextView)findViewById(android.R.id.empty);

        mAdapter = new MusicListAdapter(this);
        setListAdapter(mAdapter);
        getListView().setOnItemClickListener(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addDataScheme("file");
        registerReceiver(mReceiver, filter);
        mRegisteredReceiver = true;

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        mResolver = getContentResolver();
        mObserver = new MyContentObserver(null);
        mResolver.registerContentObserver(uri, false, mObserver);
        mRegisteredObserver = true;

        prepare();
    }

    private class MyContentObserver extends ContentObserver {
        public MyContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            mHandler.removeMessages(MSG_CHANGE);
            mHandler.sendEmptyMessageDelayed(MSG_CHANGE, 500);
        }
    }

    private void showProgress(boolean show) {
        mProgressBarVisibile = show;
        setProgressBarIndeterminateVisibility(show);
        if (!show) {
            mMusicList.clear();
            mMusicList.addAll(mTmpList);
            mTmpList.clear();
            mHintText.setText("");
            mAdapter.notifyDataSetChanged();
            updateHint();
        }
        updateMenu(mMenu);
    }

    private void updateMenu(Menu menu) {
        if (menu == null) return;

        if (mOperateId == GROUP_ADD) {
            menu.findItem(R.id.del_sel).setVisible(false);
            menu.findItem(R.id.del_all).setVisible(false);
        } else {
            menu.findItem(R.id.add_sel).setVisible(false);
            menu.findItem(R.id.add_all).setVisible(false);
        }

        if (mMusicList.size() == 0 || mProgressBarVisibile) {
            menu.findItem(R.id.desel_all).setEnabled(false);
            if (mOperateId == GROUP_ADD) {
                menu.findItem(R.id.add_sel).setEnabled(false);
                menu.findItem(R.id.add_all).setEnabled(false);
            } else {
                menu.findItem(R.id.del_sel).setEnabled(false);
                menu.findItem(R.id.del_all).setEnabled(false);
            }
        } else {
            boolean enable = isEnable();
            menu.findItem(R.id.desel_all).setEnabled(enable);
            if (mOperateId == GROUP_ADD) {
                menu.findItem(R.id.add_sel).setEnabled(enable);
                menu.findItem(R.id.add_all).setEnabled(true);
            } else {
                menu.findItem(R.id.del_sel).setEnabled(enable);
                menu.findItem(R.id.del_all).setEnabled(true);
            }
        }
    }

    private void prepare() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(MSG_SHOW);
                prepareList();
                mHandler.sendEmptyMessage(MSG_HIDE);
            }
        }).start();
    }

    private void prepareList() {
        mTmpList.clear();
        String[] cols = new String[] {
            MediaStore.Audio.Media._ID,//also used for playlist
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION
        };
        //
        Uri uri = null;
        StringBuilder where = new StringBuilder();
        if (mOperateId == GROUP_ADD) {
            uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            where.append(MediaStore.Audio.Media.IS_MUSIC + "=1");
        } else {
            uri = MediaStore.Audio.Playlists.Members.getContentUri("external",
                Long.valueOf(mPlaylistId));
        }
//        if (!Environment.MEDIA_MOUNTED.equals(Environment.getSecondaryStorageState())) {
        	if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            if (mOperateId == GROUP_ADD) {
                where.append(" AND " + MediaStore.Audio.Media.DATA + " NOT LIKE '" + EXTERNAL_SD_PATH + "/%'");
            } else {
                where.append(MediaStore.Audio.Media.DATA + " NOT LIKE '" + EXTERNAL_SD_PATH + "/%'");
            }
        }

        Cursor cursor = MusicUtils.query(GroupOperateActivity.this,
            uri, cols, where.toString(), null, null);
        if (cursor != null) {
            int len = cursor.getCount();
            cursor.moveToFirst();
            for (int i = 0; i < len && !mDestroy; i++) {
                ViewHolder holder = new ViewHolder();
                holder._id = cursor.getLong(0);
                holder._title = cursor.getString(1);
                holder._artist = cursor.getString(2);
                holder._duration = cursor.getInt(3)/1000;
                mTmpList.add(holder);
                //
                cursor.moveToNext();
            }
            cursor.close();
        }
    }

    private void updateHint() {
        if (mMusicList.size() == 0 && mOperateId == GROUP_ADD) {
            MusicUtils.displayHintMessage(this);
        }
    }

    private void showToast() {
        if (mToast != null) {
            Toast.makeText(this, mToast, Toast.LENGTH_SHORT).show();
        }
    }

    class MusicListAdapter extends BaseAdapter {

        private Context mContext;
        private LayoutInflater mInflater;

        MusicListAdapter(Context context) {
            mContext = context;
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mMusicList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (mDestroy) return null;

            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.operate_list_item, null);
                //
                holder = new ViewHolder();
                holder.cb = (CheckBox)convertView.findViewById(R.id.cb);
                holder.title = (TextView)convertView.findViewById(R.id.title);
                holder.artist = (TextView)convertView.findViewById(R.id.artist);
                holder.duration = (TextView)convertView.findViewById(R.id.duration);
                //
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            //
            ViewHolder vh = mMusicList.get(position);
            holder.cb.setChecked(vh.sel);
            holder.title.setText(vh._title);
            if (vh._artist == null || vh._artist.equals(MediaStore.UNKNOWN_STRING)) {
                vh._artist = mContext.getString(R.string.unknown_artist_name);
            }
            holder.artist.setText(vh._artist);
            if (vh._duration == 0) {
                holder.duration.setText("");
            } else {
                holder.duration.setText(MusicUtils.makeTimeString(mContext, vh._duration));
            }
            return convertView;
        }
    }

    class ViewHolder {
        boolean   sel;
        long      _id;
        String    _title;
        String    _artist;
        int       _duration;
        CheckBox  cb;
        TextView  title;
        TextView  artist;
        TextView  duration;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.operate_menu, menu);
        mMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    private void selAll(final boolean selAll) {
        int len = mMusicList.size();
        for (int i = 0; i < len && !mDestroy; i++) {
            mMusicList.get(i).sel = selAll;
        }
        mAdapter.notifyDataSetChanged();
    }

    private void operateConfirm(final boolean operateAll) {
        int msgId = (mOperateId == GROUP_ADD) ? R.string.add_warning : R.string.del_warning;
        new AlertDialog.Builder(this)
            .setMessage(getString(msgId))
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mHandler.sendEmptyMessage(MSG_SHOW);
                            if (mOperateId == GROUP_ADD) {
                                addAll(operateAll);
                            } else {
                                delAll(operateAll);
                            }
                            mHandler.sendEmptyMessage(MSG_HIDE);
                        }
                    }).start();
                }
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    private void addAll(boolean addAll) {
        List<Long> list = new ArrayList<Long>();
        mTmpList.addAll(mMusicList);
        int len = mTmpList.size();
        for (int i = 0; i < len && !mDestroy; i++) {
            if (mTmpList.get(i).sel || addAll) {
                list.add(mTmpList.get(i)._id);
            }
        }

        if (!mDestroy) {
            int count = list.size();
            if (count != 0) {
                long[] ids = new long[count];
                for (int j = 0; j < count; j++) {
                    ids[j] = list.get(j).longValue();
                }
                String toast = MusicUtils.addToPlaylist(GroupOperateActivity.this,
                        ids, Long.valueOf(mPlaylistId), false);
                ids = null;
                if (toast != null) {
                    mToast = toast;
                    mHandler.sendEmptyMessage(MSG_TOAST);
                }
            }
            //
            if (addAll) {
                mToast = getString(R.string.add_all_success);
                mHandler.sendEmptyMessage(MSG_TOAST);
                GroupOperateActivity.this.finish();
            }
        }
        list.clear();
    }

    private void delAll(boolean delAll) {
        mTmpList.addAll(mMusicList);
        int count = mTmpList.size();

        StringBuilder where = new StringBuilder();
        where.append(MediaStore.Audio.Media._ID + " IN (");
        for (int i = 0; i < mTmpList.size() && !mDestroy; i++) {
            if (mTmpList.get(i).sel || delAll) {
                where.append(mTmpList.get(i)._id);
                where.append(",");
                mTmpList.remove(i--);
            }
        }

        if (!mDestroy) {
            if (where.toString().endsWith(",")) {
                where.setCharAt(where.toString().lastIndexOf(','), ')');
                ContentResolver cr = getContentResolver();
                Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external",
                        Long.valueOf(mPlaylistId));
                cr.delete(uri, where.toString(), null);
            }

            if (!delAll) {
                mToast = (count - mTmpList.size()) + getString(R.string.del_success);
            } else if (mTmpList.size() == 0) {
                mToast = getString(R.string.del_all_success);
            }
            mHandler.sendEmptyMessage(MSG_TOAST);

            if (mTmpList.size() == 0) {
                GroupOperateActivity.this.finish();
            }
        }
        where = null;
    }

    private boolean isEnable() {
        boolean enable = false;
        int len = mMusicList.size();
        for (int i = 0; i < len; i++) {
            if (mMusicList.get(i).sel) {
                enable = true;
                break;
            }
        }
        return enable;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        updateMenu(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.sel_all:
                selAll(true);
                break;
            case R.id.desel_all:
                selAll(false);
                break;
            case R.id.add_sel:
            case R.id.del_sel:
                operateConfirm(false);
                break;
            case R.id.add_all:
            case R.id.del_all:
                operateConfirm(true);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void release() {
        mDestroy = true;
        MusicUtils.delActivity(this);
        if (mRegisteredReceiver) {
            unregisterReceiver(mReceiver);
            mRegisteredReceiver = false;
        }
        if (mRegisteredObserver) {
            mResolver.unregisterContentObserver(mObserver);
            mRegisteredObserver = false;
        }
        mHandler.removeMessages(MSG_CHANGE);
        mTmpList.clear();
        mMusicList.clear();
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        release();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mMusicList.get(position).sel = !mMusicList.get(position).sel;
        mAdapter.notifyDataSetChanged();
        updateMenu(mMenu);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        release();
    }
}
