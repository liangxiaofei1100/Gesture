package com.zhaoyan.gesture.image;

import java.util.ArrayList;
import java.util.List;

import com.zhaoyan.common.adapter.CheckableBaseAdapter;
import com.zhaoyan.common.view.CheckableImageView;
import com.zhaoyan.gesture.R;
import com.zhaoyan.gesture.common.ZYConstant.Extra;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;


public class ImageGridAdapter extends CheckableBaseAdapter{
	private static final String TAG = ImageGridAdapter.class.getSimpleName();
	private LayoutInflater mInflater = null;
	private List<ImageInfo> mDataList;
	
	private boolean mIdleFlag = true;
	
	/**save status of item selected*/
	private SparseBooleanArray mCheckArray;
	/**current menu mode,ActionMenu.MODE_NORMAL,ActionMenu.MODE_EDIT*/
	private int mMenuMode = 0;
	
	//List or Grid
	private int mViewType = Extra.VIEW_TYPE_DEFAULT;
	
	private int mWidth;
	private Context mContext;

	public ImageGridAdapter(Context context, int viewType, List<ImageInfo> itemList){
		super(context);
		mContext = context;
		
		mInflater = LayoutInflater.from(context);
		mDataList = itemList;
		mCheckArray = new SparseBooleanArray();
		
		mViewType = viewType;
		
		Display display = ((Activity)mContext).getWindowManager().getDefaultDisplay(); 
		mWidth = display.getWidth();  // deprecated
		Log.d(TAG, "mWidth=" + mWidth);
	}
	
	@Override
	public int getCount() {
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
		if (Extra.VIEW_TYPE_LIST == mViewType) {
			return getListView(convertView, position);
		} 
		
		return getGridView(convertView, position);
	}
	
	private View getGridView(View convertView, int position){
		View view = null;
		ViewHolder holder = null;
		if (convertView != null) {
			view = convertView;
			holder = (ViewHolder) view.getTag();
		} else {
			holder = new ViewHolder();
			view = mInflater.inflate(R.layout.image_item_grid, null);
			holder.imageView = (CheckableImageView) view.findViewById(R.id.iv_image_item);
			view.setTag(holder);
		}
		
		LayoutParams imageParams = (LayoutParams) holder.imageView.getLayoutParams();
		imageParams.width  = mWidth/3;
		imageParams.height = mWidth/3;
		holder.imageView.setLayoutParams(imageParams);

//		long id = mDataList.get(position).getImageId();
		String path = mDataList.get(position).getPath();
		
		ImageLoadAsync loadAsync = new ImageLoadAsync(mContext, holder.imageView, mWidth / 3);
		loadAsync.executeOnExecutor(MediaAsync.THREAD_POOL_EXECUTOR, path);
//		if (mIdleFlag) {
//			pictureLoader.loadBitmap(id, holder.imageView);
//			holder.imageView.setChecked(mCheckArray.get(position));
//		} else {
//			if (AsyncPictureLoader.bitmapCaches.size() > 0
//					&& AsyncPictureLoader.bitmapCaches.get(id) != null) {
//				holder.imageView.setImageBitmap(AsyncPictureLoader.bitmapCaches
//						.get(id).get());
//			} else {
//				holder.imageView.setImageResource(R.drawable.photo_l);
//			}
//			holder.imageView.setChecked(mCheckArray.get(position));
//		}
		holder.imageView.setChecked(mCheckArray.get(position));
		return view;
	}
	
	private View getListView(View convertView, int position){
		View view = null;
		ViewHolder holder = null;
		if (convertView != null) {
			view = convertView;
			holder = (ViewHolder) view.getTag();
		} else {
			holder = new ViewHolder();
			view = mInflater.inflate(R.layout.image_item_list, null);
			holder.imageView2 = (ImageView) view.findViewById(R.id.iv_image_item);
			holder.nameView = (TextView) view.findViewById(R.id.tv_image_name);
			holder.sizeView = (TextView) view.findViewById(R.id.tv_image_size);
			view.setTag(holder);
		}
		
		LayoutParams imageParams = (LayoutParams) holder.imageView2.getLayoutParams();
		imageParams.width  = mWidth / 4;
		imageParams.height = mWidth / 4;
		holder.imageView2.setLayoutParams(imageParams);
		
//		long id = mDataList.get(position).getImageId();
		String name = mDataList.get(position).getDisplayName();
		String size = mDataList.get(position).getFormatSize();
		String path = mDataList.get(position).getPath();
		
		holder.nameView.setText(name);
		holder.sizeView.setText(size);
		
		ImageLoadAsync loadAsync = new ImageLoadAsync(mContext, holder.imageView2, mWidth / 4);
		loadAsync.executeOnExecutor(MediaAsync.THREAD_POOL_EXECUTOR, path);
//		if (mIdleFlag) {
//			pictureLoader.loadBitmap(id, holder.imageView2);
//		} else {
//			if (AsyncPictureLoader.bitmapCaches.size() > 0
//					&& AsyncPictureLoader.bitmapCaches.get(id) != null) {
//				holder.imageView2.setImageBitmap(AsyncPictureLoader.bitmapCaches
//						.get(id).get());
//			} else {
//				holder.imageView2.setImageResource(R.drawable.photo_l);
//			}
//		}
		
		updateViewBackground(position, view);
		return view;
	}
	
	public void updateViewBackground(int position, View view){
		boolean selected = isChecked(position);
		if (selected) {
			view.setBackgroundResource(R.color.holo_blue_light);
		}else {
			view.setBackgroundResource(Color.TRANSPARENT);
		}
	}
	
	private class ViewHolder{
		CheckableImageView imageView;//folder icon
		
		ImageView imageView2;
		TextView nameView;
		TextView sizeView;
	}

	@Override
	public void changeMode(int mode) {
		mMenuMode = mode;
	}

	@Override
	public boolean isMode(int mode) {
		return mMenuMode == mode;
	}

	@Override
	public void checkedAll(boolean isChecked) {
		int count = this.getCount();
		for (int i = 0; i < count; i++) {
			setChecked(i, isChecked);
		}
	}

	@Override
	public void setChecked(int position, boolean isChecked) {
		mCheckArray.put(position, isChecked);
	}

	@Override
	public void setChecked(int position) {
		mCheckArray.put(position, !isChecked(position));
	}

	@Override
	public boolean isChecked(int position) {
		return mCheckArray.get(position);
	}

	@Override
	public int getCheckedCount() {
		int count = 0;
		for (int i = 0; i < mCheckArray.size(); i++) {
			if (mCheckArray.valueAt(i)) {
				count ++;
			}
		}
		return count;
	}

	@Override
	public List<Integer> getCheckedPosList() {
		List<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < mCheckArray.size(); i++) {
			if (mCheckArray.valueAt(i)) {
				list.add(i);
			}
		}
		return list;
	}

	@Override
	public List<String> getCheckedNameList() {
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < mCheckArray.size(); i++) {
			if (mCheckArray.valueAt(i)) {
				list.add(mDataList.get(i).getDisplayName());
			}
		}
		return list;
	}

	@Override
	public List<String> getCheckedPathList() {
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < mCheckArray.size(); i++) {
			if (mCheckArray.valueAt(i)) {
				list.add(mDataList.get(i).getPath());
			}
		}
		return list;
	}

	public void setIdleFlag(boolean flag) {
		this.mIdleFlag = flag;
	}
}
 

