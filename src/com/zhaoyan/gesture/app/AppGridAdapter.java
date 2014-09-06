package com.zhaoyan.gesture.app;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhaoyan.common.adapter.CheckableBaseAdapter;
import com.zhaoyan.common.utils.Log;
import com.zhaoyan.gesture.R;

public class AppGridAdapter extends CheckableBaseAdapter {
	private static final String TAG = AppGridAdapter.class.getSimpleName();
	
	private List<AppEntry> mList;
	private LayoutInflater mInflater;
	
	private int mCurrentSelectPosition = -1;

	public AppGridAdapter(Context context){
		super(context);
		mInflater = LayoutInflater.from(context);
	}
	
	public void setData(List<AppEntry> list){
		mList = list;
		notifyDataSetChanged();
	}
	
	public void setSelect(int position){
		mCurrentSelectPosition = position;
		notifyDataSetChanged();
	}
	
	public AppEntry getSelectEntry(){
		return (AppEntry) getItem(mCurrentSelectPosition);
	}
	
	
	/**
	 * get Select item pakcageName list
	 * @return
	 */
	public List<String> getCheckedPkgList(){
		Log.d(TAG, "getSelectedPkgList");
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < mCheckArray.size(); i++) {
			if (mCheckArray.valueAt(i)) {
				String packagename = mList.get(i).getPackageName();
				list.add(packagename);
			}
		}
		return list;
	}
	
	@Override
	public int getCount() {
		return mList == null ? 0 : mList.size();
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		ViewHolder holder = null;
		if (convertView == null) {
			view = mInflater.inflate(R.layout.layout_app_item_grid, null);
			holder = new ViewHolder();
			holder.iconView = (ImageView) view.findViewById(R.id.app_icon_text_view);
			holder.labelView = (TextView) view.findViewById(R.id.app_name_textview);
			holder.infoView = (TextView) view.findViewById(R.id.app_size_textview);
			view.setTag(holder);
		} else {
			view = convertView;
			holder = (ViewHolder) convertView.getTag();
		}
		
		AppEntry appEntry = mList.get(position);
		holder.iconView.setImageDrawable(appEntry.getIcon());
		holder.labelView.setText(appEntry.getLabel());
		holder.infoView.setText(appEntry.getSizeStr());
		
		if (mCurrentSelectPosition == position) {
			view.setBackgroundResource(R.color.kk_theme_color);
		} else {
			view.setBackgroundColor(Color.TRANSPARENT);
			updateViewBackground(position, view);
		}
		
		return view;
	}

	private class ViewHolder{
		ImageView iconView;
		TextView labelView;
		TextView infoView;
	}
}
