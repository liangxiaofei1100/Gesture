package com.zhaoyan.gesture.app;

import java.util.List;

import com.zhaoyan.gesture.R;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AppGridAdapter extends BaseAdapter {
	private static final String TAG = AppGridAdapter.class.getSimpleName();
	
	private List<AppEntry> mList;
	private LayoutInflater mInflater;
	
	private int mCurrentSelectPosition = -1;

	public AppGridAdapter(Context context){
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
		}
		
		return view;
	}

	private class ViewHolder{
		ImageView iconView;
		TextView labelView;
		TextView infoView;
	}
}
