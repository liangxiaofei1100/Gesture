package com.zhaoyan.gesture.image;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zhaoyan.common.imageloader.ImageLoadAsync;
import com.zhaoyan.common.imageloader.MediaAsync;
import com.zhaoyan.common.views.CheckableImageView;
import com.zhaoyan.gesture.R;
import com.zhaoyan.gesture.common.ZYUtils.ViewHolder;

public class ImageFolderAdapter extends BaseAdapter {
	private static final String TAG = ImageFolderAdapter.class.getSimpleName();

	private List<ImageFolderInfo> mDataList = new ArrayList<ImageFolderInfo>();
	private LayoutInflater mInflater = null;
	private Context mContext;
	
	private int mCheckedPosition = 0;
	
	public ImageFolderAdapter(Context context, List<ImageFolderInfo> list){
		mContext = context;
		mDataList = list;
		mInflater = LayoutInflater.from(context);
	}
	
	public void setCheckedPosition(int position){
		mCheckedPosition = position;
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
		
		CheckableImageView imageView = ViewHolder.get(view, R.id.image_folder_icon_iv);
		TextView textView = ViewHolder.get(view, R.id.image_folder_info_tv);
		
		ImageLoadAsync loadAsync = new ImageLoadAsync(mContext, imageView, 68);
		loadAsync.executeOnExecutor(MediaAsync.THREAD_POOL_EXECUTOR, mDataList.get(position).getImagePath());
		
		textView.setText(mDataList.get(position).getDisplayName() + 
				"(" + mDataList.get(position).getIdList().size() + ")");
		
		if (position == mCheckedPosition) {
			imageView.setChecked(true);
		} else {
			imageView.setChecked(false);
		}
		
		return view;
	}
	
	

}
