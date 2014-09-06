package com.zhaoyan.gesture.music.ui;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.zhaoyan.common.utils.Log;
import com.zhaoyan.gesture.R;
import com.zhaoyan.gesture.music.MediaPlaybackService;
import com.zhaoyan.gesture.music.utils.MusicUtils;
import com.zhaoyan.gesture.music.views.BottomActionBar;


/**The Button Fragment ,use to do the music play \next or more**/
public class BottomActionBarFragment extends Fragment {
	private static final String TAG = BottomActionBarFragment.class.getSimpleName();
	
	private ImageButton mPrev,mNext;
    private BottomActionBar mBottomActionBar;
    public static ImageButton mPlay;
    private ImageView mAlbumView;
    
    private AlbumArtWorker mAlbumArtWorker = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	Log.d(TAG, "onCreatView");
    	View root = inflater.inflate(R.layout.bottom_action_bar, container);
        mBottomActionBar = new BottomActionBar(getActivity());
        mBottomActionBar.updateBottomActionBar(getActivity());
        
        mPrev = (ImageButton)root.findViewById(R.id.bottom_action_bar_previous);
        mPrev.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MusicUtils.sService == null)
                    return;
                try {//Here the number has been changed (<2000 changed to >0)
                	 //so can save the problem that can not PREV
                    if (MusicUtils.sService.position() > 0) {
                        MusicUtils.sService.prev();
                    } else {
                        MusicUtils.sService.seek(0);
                        MusicUtils.sService.play();
                    }
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }
        });

        mPlay = (ImageButton)root.findViewById(R.id.bottom_action_bar_play);
        mPlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                doPauseResume();
            }
        });

        mNext = (ImageButton)root.findViewById(R.id.bottom_action_bar_next);
        mNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MusicUtils.sService == null)
                    return;
                try {
                    MusicUtils.sService.next();
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }
        });

        mAlbumView = (ImageView) root.findViewById(R.id.bottom_action_bar_album_art);
        return root;
    }

    /**
     * Update the list as needed
     */
    private final BroadcastReceiver mMediaStatusReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mBottomActionBar != null) {
                mBottomActionBar.updateBottomActionBar(getActivity());
            }
            setPauseButtonImage();
            //remove bottom action bar albumart get
//            setAlbumArt();
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MediaPlaybackService.PLAYSTATE_CHANGED);
        filter.addAction(MediaPlaybackService.META_CHANGED);
        getActivity().registerReceiver(mMediaStatusReceiver, filter);

    }

    @Override
    public void onStop() {
        getActivity().unregisterReceiver(mMediaStatusReceiver);
//        if (mAlbumArtWorker != null) {
//			mAlbumArtWorker.cancel(true);
//			mAlbumArtWorker = null;
//		}
        super.onStop();
    }

    /**
     * Play and pause music
     */
    private void doPauseResume() {
        try {
            if (MusicUtils.sService != null) {
                if (MusicUtils.sService.isPlaying()) {
                    MusicUtils.sService.pause();
                } else {
                    MusicUtils.sService.play();
                  //remove bottom action bar albumart get
//                  setAlbumArt();
                }
            }
            setPauseButtonImage();
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Set the play and pause image
     */
    private void setPauseButtonImage() {
        try {
            if (MusicUtils.sService != null && MusicUtils.sService.isPlaying()) {
                mPlay.setImageResource(R.drawable.holo_light_pause);
            } else {
                mPlay.setImageResource(R.drawable.holo_light_play);
            }
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }
    
    private void setAlbumArt(){
    	long currentAlbumId = MusicUtils.getCurrentAlbumId();
    	
//    	if (mAlbumArtWorker == null) {
			mAlbumArtWorker = new AlbumArtWorker();
//		}
    	
//    	if (mAlbumArtWorker.getStatus() != AsyncTask.Status.RUNNING) {
    		mAlbumArtWorker.execute(currentAlbumId);
//		}
    }
    
    /**
     * the class use to get the album art for notification
     * @author Yuri
     *
     */
    private class AlbumArtWorker extends AsyncTask<Long, Void, Bitmap>{

		@Override
		protected Bitmap doInBackground(Long... albumId) {
			Bitmap bitmap = null;
			try {
				long id = albumId[0].longValue();
				bitmap = MusicUtils.getArtwork(getActivity(), -1, id, true);
				//for special file whose decode data is null
				if (bitmap == null) {
					bitmap = MusicUtils.getDefaultArtwork(getActivity());
				}
			} catch (IllegalArgumentException e) {
				Log.e(TAG, "AlbumArtWOrker called with rong parameters");
				return null;
			}
			return bitmap;
		}
		
		/**
		 * update the bottom action bar if got the bitmap
		 */
		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			Log.d(TAG, "AlbumArtWorker.onPostExecute");
			mAlbumView.setImageBitmap(result);
		}
    	
    }
}
