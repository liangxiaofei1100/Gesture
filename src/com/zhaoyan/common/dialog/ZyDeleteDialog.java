package com.zhaoyan.common.dialog;

import com.zhaoyan.gesture.R;
import com.zhaoyan.gesture.common.ZYConstant;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ZyDeleteDialog extends ZyDialogBuilder {
	
	private String mDeleteMsg;
	private Context mContext;

	public ZyDeleteDialog(Context context) {
		super(context, R.style.dialog_untran);
		mContext = context;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		View customView = getLayoutInflater().inflate(R.layout.dialog_delete, null);
		TextView textView = (TextView) customView.findViewById(R.id.dialog_delete_tv_msg);
		textView.setText(mDeleteMsg);
		
		setCustomView(customView);
		setDuration(ZYConstant.DEFAULT_DIALOG_DURATION);
		super.onCreate(savedInstanceState);
	}
	
	public void setDeleteMsg(String msg){
		mDeleteMsg  = msg;
	}
	
	public void setDeleteMsg(int msgId){
		String msg = mContext.getString(msgId);
		setDeleteMsg(msg);
	}

}
