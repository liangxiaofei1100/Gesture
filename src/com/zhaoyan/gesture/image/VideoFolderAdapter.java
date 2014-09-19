package com.zhaoyan.gesture.image;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zhaoyan.common.imageloader.AsyncVideoLoader;
import com.zhaoyan.common.imageloader.AsyncVideoLoader.ILoadVideoCallback;
import com.zhaoyan.common.views.CheckableImageView;
import com.zhaoyan.gesture.R;
import com.zhaoyan.gesture.common.ZYUtils.ViewHolder;

public class VideoFolderAdapter extends BaseAdapter {
	private static final String TAG = VideoFolderAdapter.class.getSimpleName();

	private List<MediaFolderInfo> mDataList = new ArrayList<MediaFolderInfo>();
	private LayoutInflater mInflater = null;
	private Context mContext;
	
	private AsyncVideoLoader mAsyncVideoLoader;
	private boolean mIdleFlag = true;
	
	private int mCheckedPosition = 0;
	
	public VideoFolderAdapter(Context context, List<MediaFolderInfo> list){
		mContext = context;
		mDataList = list;
		mInflater = LayoutInflater.from(context);
		
		mAsyncVideoLoader = new AsyncVideoLoader(context);
	}
	
	public void setCheckedPosition(int position){
		mCheckedPosition = position;
	}
	
	public void setIdleFlag(boolean flag){
		this.mIdleFlag = flag;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mDataList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view = null;
		if (convertView == null) {
			view = mInflater.inflate(R.layout.image_fragment_folder_item, null);
		} else {
			view = convertView;
		}
		
		final CheckableImageView imageView = ViewHolder.get(view, R.id.image_folder_icon_iv);
		TextView nameView = ViewHolder.get(view, R.id.image_folder_name_tv);
		TextView numView = ViewHolder.get(view, R.id.image_folder_num_tv);
		
		long id = mDataList.get(position).getIdList().get(0);
		if (!mIdleFlag) {
			if (AsyncVideoLoader.bitmapCaches.size() > 0
					&& AsyncVideoLoader.bitmapCaches.get(id) != null) {
				imageView.setImageBitmap(AsyncVideoLoader.bitmapCaches.get(id)
						.get());
			} else {
				imageView.setImageResource(R.drawable.default_video_iv);
			}
		} else {
			Bitmap bitmap = mAsyncVideoLoader.loadBitmap(id, new ILoadVideoCallback() {
				@Override
				public void onObtainBitmap(Bitmap bitmap, long id) {
					imageView.setImageBitmap(bitmap);
				}
			});
			
			if (null == bitmap) {
				//在图片没有读取出来的情况下预先放一张图
				imageView.setImageResource(R.drawable.default_video_iv);
			}else {
				imageView.setImageBitmap(bitmap);
			}
		}
		
		
		nameView.setText(mDataList.get(position).getDisplayName());
		numView.setText(mDataList.get(position).getIdList().size() + "");
		
		if (position == mCheckedPosition) {
			imageView.setChecked(true);
		} else {
			imageView.setChecked(false);
		}
		
		return view;
	}
	

}
