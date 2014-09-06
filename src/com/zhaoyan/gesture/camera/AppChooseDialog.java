package com.zhaoyan.gesture.camera;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.zhaoyan.common.dialog.Effectstype;
import com.zhaoyan.common.dialog.ZyDialogBuilder;
import com.zhaoyan.gesture.R;

public class AppChooseDialog extends ZyDialogBuilder implements
		OnItemClickListener {
	private static final String TAG = AppChooseDialog.class.getSimpleName();

	private List<AppInfo> mAppList = null;
	private ListView mListView;
	private View mLoadMoreView;
	private SingleChoiceAdapter mAdapter;
	private int mCurrentChoicePostion;

	public AppChooseDialog(Context context, List<AppInfo> appList) {
		super(context, R.style.dialog_untran);
		mAppList = appList;

		View customView = LayoutInflater.from(context).inflate(
				R.layout.dialog_single_choice, null);
		mListView = (ListView) customView.findViewById(R.id.dlg_listview);

		View footView = LayoutInflater.from(context).inflate(
				R.layout.dialog_default_player_select_footview, null);
		mLoadMoreView = footView.findViewById(R.id.rl_footview);
		mListView.addFooterView(footView);

		mAdapter = new SingleChoiceAdapter(context);
		mListView.setAdapter(mAdapter);

		mListView.setOnItemClickListener(this);

		setDuration(0);
		setEffect(Effectstype.SlideBottom);
		setMessage(null);
		setCustomView(customView, context);
	}

	public void setLoadMoreClick(android.view.View.OnClickListener click) {
		mLoadMoreView.setOnClickListener(click);
	}

	public void setCurrentChoice(int position) {
		mCurrentChoicePostion = position;
	}

	public AppInfo getCurrentChoice() {
		if (mAppList != null) {
			return mAppList.get(mCurrentChoicePostion);
		}
		return null;
	}

	private class SingleChoiceAdapter extends BaseAdapter {
		LayoutInflater inflater = null;

		public SingleChoiceAdapter(Context context) {
			inflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return mAppList == null ? 0 : mAppList.size();
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
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = null;
			ViewHodler hodler = null;
			if (convertView == null) {
				view = inflater.inflate(R.layout.dialog_single_choice_item,
						null);
				hodler = new ViewHodler();
				hodler.textView = (TextView) view
						.findViewById(R.id.dialog_tv_text);
				hodler.iconView = (ImageView) view
						.findViewById(R.id.dialog_iv_icon);
				hodler.radioButton = (RadioButton) view
						.findViewById(R.id.dialog_radiobutton);
				view.setTag(hodler);
			} else {
				hodler = (ViewHodler) convertView.getTag();
				view = convertView;
			}

			AppInfo info = mAppList.get(position);

			if (mCurrentChoicePostion == position) {
				hodler.radioButton.setChecked(true);
			} else {
				hodler.radioButton.setChecked(false);
			}

			hodler.textView.setText(info.label);
			hodler.iconView.setImageDrawable(info.logo);

			return view;
		}

		class ViewHodler {
			TextView textView;
			ImageView iconView;
			RadioButton radioButton;
		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		mCurrentChoicePostion = position;
		mAdapter.notifyDataSetChanged();
	}

}
