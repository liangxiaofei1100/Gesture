package com.zhaoyan.gesture.app;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhaoyan.gesture.R;

public class AppListAdapter extends ArrayAdapter<AppEntry>{
	private final LayoutInflater mInflater;
	private int mCurrentSelectPosition = -1;
	private boolean mIsSelectMode = true;

	public AppListAdapter(Context context) {
		super(context, android.R.layout.simple_list_item_2);
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void setData(List<AppEntry> data) {
		clear();
		if (data != null) {
			addAll(data);
		}
	}
	
	public void setSelect(int position){
		mCurrentSelectPosition = position;
		notifyDataSetChanged();
	}
	
	public AppEntry getSelectEntry(){
		return getItem(mCurrentSelectPosition);
	}

	/**
	 * Populate new items in the list.
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;

		if (convertView == null) {
			view = mInflater.inflate(R.layout.app_list_item, parent, false);
		} else {
			view = convertView;
		}

		AppEntry item = getItem(position);
		((ImageView) view.findViewById(R.id.iv_app_logo)).setImageDrawable(item
				.getIcon());
		((TextView) view.findViewById(R.id.tv_app_label)).setText(item.getLabel());
		((TextView) view.findViewById(R.id.tv_app_info)).setText(item.getSizeStr());
		
//		if (mIsSelectMode) {
			if (mCurrentSelectPosition == position) {
				view.setBackgroundColor(Color.argb(0xff, 0x33, 0xb5, 0xe5));
			} else {
				view.setBackgroundColor(Color.TRANSPARENT);
			}
//		}

		return view;
	}
}
