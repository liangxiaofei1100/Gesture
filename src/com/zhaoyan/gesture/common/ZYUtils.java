package com.zhaoyan.gesture.common;

import android.app.AlertDialog;
import android.content.Context;
import android.util.SparseArray;
import android.view.View;

import com.zhaoyan.gesture.R;


public class ZYUtils {
	private static final String TAG = ZYUtils.class.getSimpleName();
	
	public static void showInfoDialog(Context context, String info){
		String title = context.getResources().getString(R.string.menu_info);
		showInfoDialog(context, title, info);
	}
	
	public static void showInfoDialog(Context context, String title, String info){
		new AlertDialog.Builder(context)
		.setTitle(title)
		.setMessage(info)
		.setPositiveButton(android.R.string.ok, null)
		.create().show();
	}
	
	
	/**
	 * verify the file name's format is correct
	 * @param context  for getResource
	 * @param name the name need to verify
	 * @return null:the filename's format is correct,else return the error message
	 */
	public static String FileNameFormatVerify(Context context, String name){
		if (name.equals(".")) {
			return context.getString(R.string.file_rename_error_2, ".");
		}else if (name.equals("..")) {
			return context.getString(R.string.file_rename_error_2, "..");
		}else {
			for (int i = 0; i < ZYConstant.ERROR_NAME_STRS.length; i++) {
				if (name.indexOf(ZYConstant.ERROR_NAME_STRS[i]) >= 0) {
					return context.getString(R.string.file_rename_error_1, ZYConstant.ERROR_NAME_STRS[i]);
				}
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static class ViewHolder{
	    public static <T extends View> T get(View view,int id){
			SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
	        if(viewHolder == null){
	            viewHolder = new SparseArray<View>();
	            view.setTag(viewHolder);
	        }
	        View childView = viewHolder.get(id);
	        if(childView == null){
	            childView = view.findViewById(id);
	            viewHolder.put(id,childView);   
	        }
	        return (T) childView;
	    }
	}
}
