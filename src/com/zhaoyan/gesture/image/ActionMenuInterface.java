package com.zhaoyan.gesture.image;

import com.zhaoyan.gesture.image.ActionMenu.ActionMenuItem;


public interface ActionMenuInterface {
	
	public interface OnMenuItemClickListener{
		public void onMenuItemClick(ActionMenuItem item);
	}
}
