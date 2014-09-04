package com.zhaoyan.gesture.image;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.text.Collator;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.zhaoyan.gesture.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.SparseArray;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;


public class ZYUtils {
	private static final String TAG = "ZYUtils";
	
	/**
	 * 插入�?��数据到已经排好序的list�?
	 * @param list 已经排好序的list
	 * @param appEntry 要插入的数据
	 * @return 将要插入的位�?
	 */
	public static int getInsertIndex(List<AppInfo> list, AppInfo appEntry){
		Collator sCollator = Collator.getInstance();
		for (int i = 0; i < list.size(); i++) {
			int ret = sCollator.compare(appEntry.getLabel(), list.get(i).getLabel());
			if (ret <=0 ) {
				return i;
			}
		}
		return list.size();
	}
	
	/**
	 * byte convert
	 * @param size like 3232332
	 * @return like 3.23M
	 */
//	public static String getFormatSize(long size){
//		if (size >= 1024 * 1024 * 1024){
//			Double dsize = (double) (size / (1024 * 1024 * 1024));
//			return new DecimalFormat("#.00").format(dsize) + "G";
//		}else if (size >= 1024 * 1024) {
//			Double dsize = (double) (size / (1024 * 1024));
//			return new DecimalFormat("#.00").format(dsize) + "M";
//		}else if (size >= 1024) {
//			Double dsize = (double) (size / 1024);
//			return new DecimalFormat("#.00").format(dsize) + "K";
//		}else {
//			return String.valueOf((int)size) + "B";
//		}
//	}
	
	public static String getFormatSize(double size){
		if (size >= 1024 * 1024 * 1024){
			Double dsize = size / (1024 * 1024 * 1024);
			return new DecimalFormat("#.00").format(dsize) + "G";
		}else if (size >= 1024 * 1024) {
			Double dsize = size / (1024 * 1024);
			return new DecimalFormat("#.00").format(dsize) + "M";
		}else if (size >= 1024) {
			Double dsize = size / 1024;
			return new DecimalFormat("#.00").format(dsize) + "K";
		}else {
			return String.valueOf((int)size) + "B";
		}
	}
	
	/**get date format*/
	public static String getFormatDate(long date){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = format.format(new Date(date));
		return dateString;
	}
	
	/** 
     * 格式化时间，将毫秒转换为�?秒格�?
     * @param time audio/video time like 12323312
     * @return the format time string like 00:12:23
     */  
	public static String mediaTimeFormat(long duration) {
		long hour = duration / (60 * 60 * 1000);
		String min = duration % (60 * 60 * 1000) / (60 * 1000) + "";
		String sec = duration % (60 * 60 * 1000) % (60 * 1000) + "";

		if (min.length() < 2) {
			min = "0" + duration / (1000 * 60) + "";
		}

		if (sec.length() == 4) {
			sec = "0" + sec;
		} else if (sec.length() == 3) {
			sec = "00" + sec;
		} else if (sec.length() == 2) {
			sec = "000" + sec;
		} else if (sec.length() == 1) {
			sec = "0000" + sec;
		}

		if (hour == 0) {
			return min + ":" + sec.trim().substring(0, 2);
		} else {
			String hours = "";
			if (hour < 10) {
				hours = "0" + hour;
			} else {
				hours = hours + "";
			}
			return hours + ":" + min + ":" + sec.trim().substring(0, 2);
		}
	}
	
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
	
	/**set dialog dismiss or not*/
	public static void setDialogDismiss(DialogInterface dialog, boolean dismiss){
		try {
			Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
			field.setAccessible(true);
			field.set(dialog, dismiss);
			dialog.dismiss();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**Drawable convert to byte */
	public static synchronized byte[] drawableToByte(Drawable drawable) {
		if (drawable != null) {
			Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
					drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
			Canvas canvas = new Canvas(bitmap);
			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
			drawable.draw(canvas);
			int size = bitmap.getWidth() * bitmap.getHeight() * 4;
			// 创建�?��字节数组输出�?流的大小为size
			ByteArrayOutputStream baos = new ByteArrayOutputStream(size);
			// 设置位图的压缩格式，质量�?00%，并放入字节数组输出流中
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
			// 将字节数组输出流转化为字节数组byte[]
			byte[] imagedata = baos.toByteArray();
			return imagedata;
		}
		return null;
	}
	
	/**
	 * get parent path
	 * @param path current file path
	 * @return parent path
	 */
	public static String getParentPath(String path){
		File file = new File(path);
		return file.getParent();
	}
	
	/**
	 * bytes tp chars
	 * 
	 * @param bytes
	 * @return
	 */
	public static char[] getChars(byte[] bytes) {
		Charset cs = Charset.forName("UTF-8");
		ByteBuffer bb = ByteBuffer.allocate(bytes.length);
		bb.put(bytes);
		bb.flip();
		CharBuffer cb = cs.decode(bb);
		return cb.array();
	}
	
	/**
	 * show input method in view
	 * @param context
	 * @param view
	 */
	public static void showInputMethod(Context context, View view){
		InputMethodManager im = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		im.showSoftInput(view, 0);
	}
	
	/**
	 * show the input method mannual
	 * @param v the view that need show input method,like edittext
	 * @param hasFocus
	 */
	public static void onFocusChange(final View v, boolean hasFocus) {
		final boolean isFocus = hasFocus;
		(new Handler()).postDelayed(new Runnable() {
			public void run() {
				InputMethodManager imm = (InputMethodManager) v.getContext()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				if (isFocus) {
					imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
				} else {
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				}
			}
		}, 500);
	}
	
	/**
	 * force show virtual menu key </br>
	 * must call after setContentView() 
	 * @param window you can use getWindow()
	 */
	public static void forceShowMenuKey(Window window){
		try {
			window.addFlags(WindowManager.LayoutParams.class.getField("FLAG_NEEDS_MENU_KEY").getInt(null));
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
