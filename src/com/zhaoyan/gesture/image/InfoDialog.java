package com.zhaoyan.gesture.image;

import com.zhaoyan.common.dialog.ZyDialogBuilder;
import com.zhaoyan.common.utils.Utils;
import com.zhaoyan.gesture.R;
import com.zhaoyan.gesture.common.ZYConstant;

import android.R.bool;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


public class InfoDialog extends ZyDialogBuilder {
	
	public static final int IMAGE = 0;
	public static final int MUSIC = 1;
	public static final int VIDEO = 2;
	public static final int FILE = 3;
	public static final int FOLDER = 4;
	private TextView mNameView;
	private TextView mNameExtView;
	private TextView mTypeView,mLoacationView,mSizeView,mIncludeView,mDateView;
	
	private LinearLayout mNameLayout;
	private LinearLayout mTypeLayout;
	private LinearLayout mSizeLayout;
	private LinearLayout mLocationLayout;
	private LinearLayout mIncludeLayout;
	private LinearLayout mDateLayout;
	
	private long mTotalSize;
	private int mFileNum;
	private int mFolderNum;
	
	private String mFileName;
	
	private String mFilePath;
	
	private long mDate;
	
	private String mFileFormatStr;
	private int mFileType;
	
	private String mTitle;
	
	private Context mContext;
	
	public static final int SINGLE_FILE = 0x01;
	public static final int SINGLE_FOLDER = 0x02;
	public static final int MULTI = 0x03;
	private int type;
	
	private static final int MSG_UPDATEUI_MULTI = 0x10;
	private static final int MSG_UPDATEUI_SINGLE = 0x11;
	private static final int MSG_UPDATE_TITLE = 0x12;
	private static final int MSG_SCAN_OVER = 0x13;
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_UPDATEUI_MULTI:
				String sizeInfo = Utils.getFormatSize(mTotalSize);
				mSizeView.setText(sizeInfo);
				int folderNum = mFolderNum;
//				if (0 != mFolderNum) {
//					//remove self folder
//					folderNum = mFolderNum - 1;
//				}
				mIncludeView.setText(mContext.getResources().getString(R.string.info_include_files, mFileNum, folderNum));
				break;
			case MSG_UPDATEUI_SINGLE:
//				mTitleView.setText(mTitle);
				
				if (IMAGE == mFileType) {
					mNameExtView.setText(R.string.info_imagename);
				}else if (MUSIC == mFileType) {
					mNameExtView.setText(R.string.info_musicname);
				}else if (VIDEO == mFileType) {
					mNameExtView.setText(R.string.info_videoname);
				}else if (FOLDER == mFileType) {
					mNameExtView.setText(R.string.info_foldername);
				}else {
					mNameExtView.setText(R.string.info_filename);
				}
				
				mTypeView.setText(mFileFormatStr);
				mNameView.setText(mFileName);
				mSizeView.setText(Utils.getFormatSize(mTotalSize));
				mLoacationView.setText(mFilePath);
				mDateView.setText(Utils.getFormatDate(mDate));
				break;
			case MSG_UPDATE_TITLE:
//				mTitleView.setText(mTitle);
				break;
			case MSG_SCAN_OVER:
//				mLoadingInfoBar.setVisibility(View.GONE);
				break;
			default:
				break;
			}
		};
	};
	
	public InfoDialog(Context context) {
		super(context, R.style.dialog_untran);
		mContext = context;
	}
	
	public void setType(int type){
		this.type = type;
	}

	private void initView(Context context, int type) {
		View customView = getLayoutInflater().inflate(R.layout.dialog_info, null);
		
		mNameView = (TextView) customView.findViewById(R.id.tv_info_name);
		mNameExtView = (TextView) customView.findViewById(R.id.tv_info_name_ext);
		mTypeView = (TextView) customView.findViewById(R.id.tv_info_type);
		mLoacationView = (TextView) customView.findViewById(R.id.tv_info_location);
		mSizeView = (TextView) customView.findViewById(R.id.tv_info_size);
		mIncludeView = (TextView) customView.findViewById(R.id.tv_info_include);
		mDateView = (TextView) customView.findViewById(R.id.tv_info_date);
		
		mNameLayout = (LinearLayout) customView.findViewById(R.id.ll_info_name);
		mTypeLayout = (LinearLayout) customView.findViewById(R.id.ll_info_type);
		mSizeLayout = (LinearLayout) customView.findViewById(R.id.ll_info_size);
		mLocationLayout = (LinearLayout) customView.findViewById(R.id.ll_info_location);
		mIncludeLayout = (LinearLayout) customView.findViewById(R.id.ll_info_include);
		mDateLayout = (LinearLayout) customView.findViewById(R.id.ll_info_date);
		
//		mLoadingInfoBar = (ProgressBar) customView.findViewById(R.id.bar_loading_info);
		
		if (MULTI == type) {
			mNameView.setVisibility(View.GONE);
			mTypeView.setVisibility(View.GONE);
			mLoacationView.setVisibility(View.GONE);
			mDateView.setVisibility(View.GONE);
			
			mNameLayout.setVisibility(View.GONE);
			mTypeLayout.setVisibility(View.GONE);
			mLocationLayout.setVisibility(View.GONE);
			mDateLayout.setVisibility(View.GONE);
			
//			mLoadingInfoBar.setVisibility(View.VISIBLE);
		}else if (SINGLE_FILE == type) {
			mIncludeView.setVisibility(View.GONE);
			mIncludeLayout.setVisibility(View.GONE);
//			mLoadingInfoBar.setVisibility(View.GONE);
			
			if (IMAGE == mFileType) {
				mNameExtView.setText(R.string.info_imagename);
			}else if (VIDEO == mFileType) {
				mNameExtView.setText(R.string.info_videoname);
			}else {
				mNameExtView.setText(R.string.info_filename);
			}
			
			mTypeView.setText(mFileFormatStr);
			mNameView.setText(mFileName);
			mSizeView.setText(Utils.getFormatSize(mTotalSize));
			mLoacationView.setText(mFilePath);
			mDateView.setText(Utils.getFormatDate(mDate));
		}
		
		setDuration(ZYConstant.DEFAULT_DIALOG_DURATION);
		setCustomView(customView);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		initView(mContext, type);
		super.onCreate(savedInstanceState);
	}
	
	public void setFileType(int type, String fileType){
		mFileType = type;
		mFileFormatStr = fileType;
	}
	
	public void setFileName(String fileName){
		mFileName = fileName;
	}
	
	public void setFilePath(String filePath){
		mFilePath = filePath;
	}
	
	public void setModifyDate(long date){
		mDate = date;
	}
	
	public void setFileSize(long size){
		mTotalSize = size;
	}
	
	public void updateUI(long size, int fileNum, int folderNum){
		this.mTotalSize = size;
		this.mFileNum = fileNum;
		this.mFolderNum = folderNum;
		mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATEUI_MULTI));
	}
	
	public void updateUI(String fileName, String filePath, long date){
		mFileName = fileName;
		mFilePath = filePath;
		mDate = date;
		mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATEUI_SINGLE));
	}
	
	public void updateSingleFileUI(){
		mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATEUI_SINGLE));
	}
	
	public void updateTitle(){
		mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_TITLE));
	}
	
	public void scanOver(){
		mHandler.sendMessage(mHandler.obtainMessage(MSG_SCAN_OVER));
	}

}
