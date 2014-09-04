package com.zhaoyan.common.dialog;

import com.zhaoyan.common.dialog.ActionMenu.ActionMenuItem;


public interface ActionMenuInterface {
	
	public interface OnMenuItemClickListener{
		public void onMenuItemClick(ActionMenuItem item);
	}
}
