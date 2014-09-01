package com.zhaoyan.common.dialog;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.zhaoyan.gesture.R;
import com.zhaoyan.gesture.music.PlayerAppInfo;

public class PlayerChoiceDialog extends ZyDialogBuilder implements OnItemClickListener{
	
	private ListView mListView;
	
	private Context mContext;
	private View mLoadMoreView;
	
	private SingleChoiceAdapter mAdapter;
	
	private List<PlayerAppInfo> mItemList = null;
	
	public PlayerChoiceDialog(Context context, List<PlayerAppInfo> list){
		super(context, R.style.dialog_untran);
		mItemList = list;
		
		mContext = context;
		View customView = LayoutInflater.from(context).inflate(R.layout.dialog_single_choice, null);
		mListView = (ListView) customView.findViewById(R.id.dlg_listview);
		
		View footView = LayoutInflater.from(context).inflate(R.layout.dialog_default_player_select_footview, null);
		mLoadMoreView = footView.findViewById(R.id.rl_footview);
		mListView.addFooterView(footView);
		
		mAdapter = new SingleChoiceAdapter(context);
		mListView.setAdapter(mAdapter);
		
		mListView.setOnItemClickListener(this);
		
		setDuration(0);
		setEffect(Effectstype.FadeIn);
		setMessage(null);
		setCustomView(customView, context);
	}
	
	public PlayerAppInfo getChoiceItem(){
		return mAdapter.getSelectItem();
	}
	
	public void setLoadMoreClick(android.view.View.OnClickListener click){
		mLoadMoreView.setOnClickListener(click);
	}
	
	private class SingleChoiceAdapter extends BaseAdapter{
		LayoutInflater inflater = null;
		
		public SingleChoiceAdapter(Context context){
			inflater = LayoutInflater.from(context);
		}
		
		public PlayerAppInfo getSelectItem(){
			for (PlayerAppInfo info : mItemList) {
				if (info.isChoice()) {
					return info;
				}
			}
			return null;
		}

		@Override
		public int getCount() {
			return mItemList == null ? 0 : mItemList.size();
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
			View view = null;
			ViewHodler hodler = null;
			if (convertView == null) {
				view = inflater.inflate(R.layout.dialog_single_choice_item, null);
				hodler = new ViewHodler();
				hodler.textView = (TextView) view.findViewById(R.id.dialog_tv_text);
				hodler.iconView = (ImageView) view.findViewById(R.id.dialog_iv_icon);
				hodler.radioButton = (RadioButton) view.findViewById(R.id.dialog_radiobutton);
				view.setTag(hodler);
			} else {
				hodler = (ViewHodler) convertView.getTag();
				view = convertView;
			}
			
			PlayerAppInfo info = mItemList.get(position);
			
			if (info.isChoice()) {
				hodler.radioButton.setChecked(true);
			} else {
				hodler.radioButton.setChecked(false);
			}
			
			hodler.textView.setText(info.getLabel());
			hodler.iconView.setImageDrawable(info.getLogo());
			
			return view;
		}
		
		class ViewHodler{
			TextView textView;
			ImageView iconView;
			RadioButton radioButton;
		}
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		PlayerAppInfo info = mItemList.get(position);
		info.setChoice(true);
		for (int i = 0; i < mItemList.size(); i++) {
			info = mItemList.get(i);
			if (i != position) {
				info.setChoice(false);
			}
		}
		mAdapter.notifyDataSetChanged();
	}
}
