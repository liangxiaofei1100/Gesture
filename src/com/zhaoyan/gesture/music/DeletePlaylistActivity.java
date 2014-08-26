package com.zhaoyan.gesture.music;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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

public class DeletePlaylistActivity extends ListActivity implements OnItemClickListener {

    private static final int MSG_INIT = 0;

    Menu mMenu;
    TextView mHintText;
    PlaylistListAdapter mAdapter;
    ArrayList<ViewHolder> mPlaylist = new ArrayList<ViewHolder>();

    boolean mDestroy = false;
    boolean mRegisteredReceiver = false;
    boolean mProgressBarVisibile = false;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!mDestroy) {
                DeletePlaylistActivity.this.finish();
            }
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (!mDestroy) {
                prepare();
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.operate_list);
        MusicUtils.addActivity(this);

        mHintText = (TextView)findViewById(android.R.id.empty);

        mAdapter = new PlaylistListAdapter(this);
        setListAdapter(mAdapter);
        getListView().setOnItemClickListener(this);

        showProgress(true);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addDataScheme("file");
        registerReceiver(mReceiver, filter);
        mRegisteredReceiver = true;

        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            finish();
            return;
        }
        mHandler.sendEmptyMessageDelayed(MSG_INIT, 100);
    }

    private void showProgress(boolean show) {
        mProgressBarVisibile = show;
        setProgressBarIndeterminateVisibility(show);
        if (!show) {
            mHintText.setText("");
            mAdapter.notifyDataSetChanged();
        }
        updateMenu(mMenu);
    }

    private void updateMenu(Menu menu) {
        if (menu == null) return;

        if (mPlaylist.size() == 0 || mProgressBarVisibile) {
            menu.findItem(R.id.desel_all).setEnabled(false);
            menu.findItem(R.id.del_sel).setEnabled(false);
        } else {
            boolean enable = isEnable();
            menu.findItem(R.id.desel_all).setEnabled(enable);
            menu.findItem(R.id.del_sel).setEnabled(enable);
        }
    }

    private void prepare() {
        prepareList();
        showProgress(false);
    }

    private void prepareList() {
        mPlaylist.clear();
        Cursor cursor = MusicUtils.query(DeletePlaylistActivity.this,
                MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                new String[] {MediaStore.Audio.Playlists._ID, MediaStore.Audio.Playlists.NAME},
                null, null, MediaStore.Audio.Playlists.NAME);
        if (cursor != null) {
            int len = cursor.getCount();
            cursor.moveToFirst();
            for (int i = 0; i < len && !mDestroy; i++) {
                ViewHolder holder = new ViewHolder();
                holder.id = cursor.getInt(0);
                holder.playlistName = cursor.getString(1);
                mPlaylist.add(holder);
                //
                cursor.moveToNext();
            }
            cursor.close();
        }
    }

    class PlaylistListAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        PlaylistListAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mPlaylist.size();
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
                convertView = mInflater.inflate(R.layout.play_list_item, null);
                //
                holder = new ViewHolder();
                holder.cb = (CheckBox)convertView.findViewById(R.id.cb);
                holder.name = (TextView)convertView.findViewById(R.id.name);
                //
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            //
            holder.cb.setChecked(mPlaylist.get(position).sel);
            holder.name.setText(mPlaylist.get(position).playlistName);
            return convertView;
        }
    }

    class ViewHolder {
        boolean   sel;
        int       id;
        String    playlistName;
        CheckBox  cb;
        TextView  name;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        mMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    private void selAll(final boolean selAll) {
        int len = mPlaylist.size();
        for (int i = 0; i < len && !mDestroy; i++) {
            mPlaylist.get(i).sel = selAll;
        }
        mAdapter.notifyDataSetChanged();
    }

    private void delConfirm(final boolean delAll) {
        new AlertDialog.Builder(this)
            .setMessage(getString(R.string.del_warning))
            .setPositiveButton(R.string.delete_confirm_button_text, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    delAll(delAll);
                }
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    private void delAll(boolean delAll) {
        int delCount = mPlaylist.size();
        for (int i = 0; i < mPlaylist.size() && !mDestroy; i++) {
            if (mPlaylist.get(i).sel || delAll) {
                int id = mPlaylist.get(i).id;
                Uri uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, id);
                getContentResolver().delete(uri, null, null);
                mPlaylist.remove(i--);
            }
        }

        if (!mDestroy) {
            String toast = null;
            if (!delAll) {
                toast = (delCount - mPlaylist.size()) + getString(R.string.del_success);
            } else if (mPlaylist.size() == 0) {
                toast = getString(R.string.del_all_success);
            }
            mAdapter.notifyDataSetChanged();
            Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
            if (mPlaylist.size() == 0) {
                finish();
            }
        }
    }

    private boolean isEnable() {
        boolean enable = false;
        int len = mPlaylist.size();
        for (int i = 0; i < len; i++) {
            if (mPlaylist.get(i).sel) {
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
            case R.id.del_sel:
                delConfirm(false);
                break;
            case R.id.del_all:
                delConfirm(true);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mProgressBarVisibile) {
            super.onBackPressed();
        }
    }

    private void release() {
        mDestroy = true;
        MusicUtils.delActivity(this);
        if (mRegisteredReceiver) {
            unregisterReceiver(mReceiver);
            mRegisteredReceiver = false;
        }
        mHandler.removeMessages(MSG_INIT);
        mPlaylist.clear();
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
        mPlaylist.get(position).sel = !mPlaylist.get(position).sel;
        mAdapter.notifyDataSetChanged();
        updateMenu(mMenu);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        release();
    }
}
