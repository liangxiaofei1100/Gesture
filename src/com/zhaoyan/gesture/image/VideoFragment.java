package com.zhaoyan.gesture.image;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Video.VideoColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.ProgressBar;

import com.zhaoyan.common.actionmenu.ActionMenu;
import com.zhaoyan.common.actionmenu.ActionMenu.ActionMenuItem;
import com.zhaoyan.common.actionmenu.MenuBarInterface;
import com.zhaoyan.common.dialog.ZyDeleteDialog;
import com.zhaoyan.common.dialog.ZyDialogBuilder.onZyDialogClickListener;
import com.zhaoyan.common.file.FileDeleteHelper;
import com.zhaoyan.common.file.FileDeleteHelper.OnDeleteListener;
import com.zhaoyan.common.utils.FileManager;
import com.zhaoyan.common.utils.IntentBuilder;
import com.zhaoyan.common.utils.Utils;
import com.zhaoyan.gesture.R;
import com.zhaoyan.gesture.common.ZYConstant;
import com.zhaoyan.gesture.fragment.BaseV4Fragment;
import com.zhaoyan.gesture.image.ImageMainActivity.MediaType;


public class VideoFragment extends BaseV4Fragment implements OnItemClickListener, OnItemLongClickListener, 
			 OnScrollListener, MenuBarInterface {
	private static final String TAG = "VideoFragment";
	
	private GridView mFolderGridView;
	private GridView mItemGridView;
	
	
	private ProgressBar mLoadingFolderBar, mLoadingItemBar;
	
	private VideoCursorAdapter mItemAdapter = null;
	private QueryHandler mQueryHandler = null;
	
	private List<MediaFolderInfo> mFolderInfosList = new ArrayList<MediaFolderInfo>();
	private VideoFolderAdapter mFolderAdapter;
	
	private static final int QUERY_TOKEN_FOLDER = 0x11;
	private static final int QUERY_TOKEN_ITEM = 0x12;
	
	private Context mContext;
	
	private static final String[] PROJECTION = new String[] {MediaStore.Video.Media._ID, 
		MediaStore.Video.Media.DURATION, MediaStore.Video.Media.SIZE,
		MediaColumns.DATA, MediaStore.Video.Media.DISPLAY_NAME,
		MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
		MediaColumns.DATE_MODIFIED};
		
	private static final int MSG_UPDATE_UI = 0;
	private static final int MSG_DELETE_OVER = 1;
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_UPDATE_UI:
				int size = msg.arg1;
				count = size;
				mActivity.updateItemTitle(MediaType.Video, -1, count);
				break;
			case MSG_DELETE_OVER:
				count = mItemAdapter.getCount();
				mActivity.updateItemTitle(MediaType.Video, -1, count);
				break;
			default:
				break;
			}
		};
	};
	
	private ImageMainActivity mActivity;
	public void onAttach(android.app.Activity activity) {
		super.onAttach(activity);
		mActivity = (ImageMainActivity) activity;
	};
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mContext = getActivity();
		View rootView = inflater.inflate(R.layout.video_main, container, false);
		
		mFolderGridView = (GridView) rootView.findViewById(R.id.video_fragment_floder_gv);
		mFolderGridView.setOnItemClickListener(this);
		mFolderGridView.setOnScrollListener(this);
		
		mItemGridView = (GridView) rootView.findViewById(R.id.video_gridview);
		mItemGridView.setOnItemClickListener(this);
		mItemGridView.setOnItemLongClickListener(this);
		mItemGridView.setDrawSelectorOnTop(true);
		mItemGridView.setOnScrollListener(this);
		
		
		mLoadingFolderBar = (ProgressBar) rootView.findViewById(R.id.bar_loading_video_folder);
		mLoadingItemBar = (ProgressBar) rootView.findViewById(R.id.bar_loading_video_item);
		
		mItemAdapter = new VideoCursorAdapter(mContext);
		
		mItemGridView.setVisibility(View.VISIBLE);
		mItemGridView.setAdapter(mItemAdapter);
		
		mMenuBarView = rootView.findViewById(R.id.bottom);
		mMenuBarView.setVisibility(View.GONE);
		initMenuBar(mMenuBarView);
		
		return rootView;
	}
	
	@Override
	public void onDestroyView() {
		if (mItemAdapter != null && mItemAdapter.getCursor() != null) {
			mItemAdapter.getCursor().close();
			mItemAdapter.changeCursor(null);
		}
		super.onDestroyView();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mFolderAdapter = new VideoFolderAdapter(getActivity(), mFolderInfosList);
		
		mQueryHandler = new QueryHandler(getActivity().getApplicationContext()
				.getContentResolver());

		queryFolder();
	}
	
	/** query all */
	public void queryFolder() {
		Log.d(TAG, "queryFolder()");
		mFolderInfosList.clear();
		
		mLoadingFolderBar.setVisibility(View.VISIBLE);
		
		query(QUERY_TOKEN_FOLDER, null, null);
	}

	public void queryFolderItem(String bucketName) {
		mLoadingItemBar.setVisibility(View.VISIBLE);
		
		String selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + "=?";
		String selectionArgs[] = { bucketName };
		query(QUERY_TOKEN_ITEM, selection, selectionArgs);
	}

	public void query(int token, String selection, String[] selectionArgs) {
		String[] projection = PROJECTION;
		mQueryHandler.startQuery(token, null,
				ZYConstant.VIDEO_URI, projection, selection,
				selectionArgs, MediaStore.Video.Media.DEFAULT_SORT_ORDER);
	}
	
	public void query() {
		mQueryHandler.startQuery(0, null, ZYConstant.VIDEO_URI,
				PROJECTION, null, null, MediaStore.Video.Media.DEFAULT_SORT_ORDER);
	}
	
	// query db
	private class QueryHandler extends AsyncQueryHandler {

		public QueryHandler(ContentResolver cr) {
			super(cr);
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			Log.d(TAG, "onQueryComplete");
			mLoadingFolderBar.setVisibility(View.INVISIBLE);
			int num = 0;
			if (null != cursor) {
				Log.d(TAG, "onQueryComplete.count=" + cursor.getCount());
				switch (token) {
				case QUERY_TOKEN_FOLDER:
					mLoadingFolderBar.setVisibility(View.GONE);
					if (cursor.moveToFirst()) {
						MediaFolderInfo imageFolderInfo = null;
						do {
							long id = cursor.getLong(cursor
									.getColumnIndex(MediaColumns._ID));
							String bucketDisplayName = cursor
									.getString(cursor
											.getColumnIndex(VideoColumns.BUCKET_DISPLAY_NAME));
							String path = cursor.getString(cursor
									.getColumnIndex(MediaColumns.DATA));
							imageFolderInfo = MediaFolderInfo.getFolderInfo(bucketDisplayName, mFolderInfosList);
							if (null == imageFolderInfo) {
								imageFolderInfo = new MediaFolderInfo();
								imageFolderInfo
										.setBucketDisplayName(bucketDisplayName);
								imageFolderInfo.setImagePath(path);
//								System.out.println("path:" + path);
								imageFolderInfo.addIdToList(id);
								imageFolderInfo
										.setDisplayName(bucketDisplayName);
								mFolderInfosList.add(imageFolderInfo);
							} else {
								imageFolderInfo.addIdToList(id);
							}
						} while (cursor.moveToNext());
						cursor.close();
					}
					mFolderAdapter.setCheckedPosition(0);
					mFolderGridView.setAdapter(mFolderAdapter);

					queryFolderItem(mFolderInfosList.get(0).getBucketDisplayName());
					break;
				case QUERY_TOKEN_ITEM:
					mLoadingItemBar.setVisibility(View.GONE);
					mItemAdapter.changeCursor(cursor);
					mItemAdapter.checkedAll(false);
					num = cursor.getCount();
					break;

				default:
					break;
				}
			}
			updateUI(num);
		}

	}
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (view.getId() == R.id.video_fragment_floder_gv) {
			switch (scrollState) {
			case OnScrollListener.SCROLL_STATE_FLING:
				mFolderAdapter.setIdleFlag(false);
				break;
			case OnScrollListener.SCROLL_STATE_IDLE:
				mFolderAdapter.setIdleFlag(true);
				mFolderAdapter.notifyDataSetChanged();
				break;
			case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
				mFolderAdapter.setIdleFlag(false);
				break;

			default:
				break;
			}
		} else {
			switch (scrollState) {
			case OnScrollListener.SCROLL_STATE_FLING:
				mItemAdapter.setIdleFlag(false);
				break;
			case OnScrollListener.SCROLL_STATE_IDLE:
				mItemAdapter.setIdleFlag(true);
				mItemAdapter.notifyDataSetChanged();
				break;
			case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
				mItemAdapter.setIdleFlag(false);
				break;
			}
		}
		
	}
	
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		switch (parent.getId()) {
		case R.id.video_fragment_floder_gv:
			if (mItemAdapter.isMode(ActionMenu.MODE_EDIT)) {
				destroyMenuBar();
			}
			
			mFolderAdapter.setCheckedPosition(position);
			mFolderAdapter.notifyDataSetChanged();
			
			String folder = mFolderInfosList.get(position)
					.getBucketDisplayName();
			queryFolderItem(folder);
			
			break;
		default:
			if (mItemAdapter.isMode(ActionMenu.MODE_NORMAL)) {
				
				Cursor cursor = mItemAdapter.getCursor();
				cursor.moveToPosition(position);
				String url = cursor.getString(cursor
						.getColumnIndex(MediaStore.Video.Media.DATA)); // 文件路径
				IntentBuilder.viewFile(getActivity(), url);
			} else {
				mItemAdapter.setChecked(position);
				mItemAdapter.notifyDataSetChanged();
				
				int selectedCount = mItemAdapter.getCheckedCount();
				mActivity.updateItemTitle(MediaType.Video, selectedCount, count);
				updateMenuBar();
				mMenuBarManager.refreshMenus(mActionMenu);
			}
			break;
		}
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, final View view, final int position, long id) {
		if (mItemAdapter.isMode(ActionMenu.MODE_EDIT)) {
			//do nothing
			doCheckAll();
			return true;
		} else {
			mItemAdapter.changeMode(ActionMenu.MODE_EDIT);
			mActivity.updateItemTitle(MediaType.Video, 1, count);
		}
		
		boolean isChecked = mItemAdapter.isChecked(position);
		mItemAdapter.setChecked(position, !isChecked);
		mItemAdapter.notifyDataSetChanged();
		
		mActionMenu = new ActionMenu(getActivity().getApplicationContext());
		getActionMenuInflater().inflate(R.menu.video_menu, mActionMenu);
		
		startMenuBar(mMenuBarView);
		return true;
	}
	
	/**
     * show confrim dialog
     * @param path file path
     */
    public void showDeleteDialog() {
    	List<String> deleteNameList = mItemAdapter.getCheckedNameList();
    	
    	final ZyDeleteDialog deleteDialog = new ZyDeleteDialog(mContext);
		String msg = "";
		if (deleteNameList.size() == 1) {
			msg = mContext.getString(R.string.delete_file_confirm_msg, deleteNameList.get(0));
		}else {
			msg = mContext.getString(R.string.delete_file_confirm_msg_video, deleteNameList.size());
		}
		deleteDialog.setDeleteMsg(msg);
		deleteDialog.setNegativeButton(R.string.cancel, null);
		deleteDialog.setPositiveButton(R.string.menu_delete, new onZyDialogClickListener() {
			@Override
			public void onClick(Dialog dialog) {
				List<String> deleteList = mItemAdapter.getCheckedPathList();
				
				FileDeleteHelper mediaDeleteHelper = new FileDeleteHelper(mContext);
				mediaDeleteHelper.setDeletePathList(deleteList);
				mediaDeleteHelper.setOnDeleteListener(new OnDeleteListener() {
					@Override
					public void onDeleteFinished() {
						Log.d(TAG, "onFinished");
						mHandler.sendMessage(mHandler.obtainMessage(MSG_DELETE_OVER));
					}
				});
				mediaDeleteHelper.doDelete();
				
				destroyMenuBar();
				dialog.dismiss();
			}
		});
		deleteDialog.show();
    }
    
    public void updateUI(int num){
		Message message = mHandler.obtainMessage();
		message.arg1 = num;
		message.what = MSG_UPDATE_UI;
		message.sendToTarget();
	}
    
    public long getTotalSize(List<Integer> list){
		long totalSize = 0;
		Cursor cursor = mItemAdapter.getCursor();
		for(int pos : list){
			cursor.moveToPosition(pos);
			long size = cursor.getLong(cursor
					.getColumnIndex(MediaStore.Video.Media.SIZE)); // 文件大小
			totalSize += size;
		}
		
		return totalSize;
	}
	
	@Override
	public boolean onBackPressed() {
		if (mItemAdapter.isMode(ActionMenu.MODE_EDIT)) {
			destroyMenuBar();
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void onMenuItemClick(ActionMenuItem item) {
		switch (item.getItemId()) {
	/*	case R.id.menu_send:
			ArrayList<String> selectedList = (ArrayList<String>) mAdapter.getCheckedPathList();
			//send
			FileTransferUtil fileTransferUtil = new FileTransferUtil(getActivity());
			fileTransferUtil.sendFiles(selectedList, new TransportCallback() {
				@Override
				public void onTransportSuccess() {
					int first = mGridView.getFirstVisiblePosition();
					int last = mGridView.getLastVisiblePosition();
					List<Integer> checkedItems = mAdapter.getCheckedPosList();
					ArrayList<ImageView> icons = new ArrayList<ImageView>();
					for(int id : checkedItems) {
						if (id >= first && id <= last) {
							View view = mGridView.getChildAt(id - first);
							if (view != null) {
								VideoGridItem item = (VideoGridItem) view;
								icons.add(item.mIconView);
							}
						}
					}
					
					if (icons.size() > 0) {
						ImageView[] imageViews = new ImageView[0];
						showTransportAnimation(icons.toArray(imageViews));
					}
					destroyMenuBar();
				}
				
				@Override
				public void onTransportFail() {
				}
			});
			break;*/
		case R.id.menu_delete:
			showDeleteDialog();
			break;
		case R.id.menu_info:
			List<Integer> list = mItemAdapter.getCheckedPosList();
			InfoDialog dialog = new InfoDialog(getActivity());
			dialog.setDialogTitle(R.string.info_video_info);
			if (1 == list.size()) {
				dialog.setType(InfoDialog.SINGLE_FILE);
				Cursor cursor = mItemAdapter.getCursor();
				cursor.moveToPosition(list.get(0));
				
				long size = cursor.getLong(cursor
						.getColumnIndex(MediaStore.Video.Media.SIZE)); // 文件大小
				String url = cursor.getString(cursor
						.getColumnIndex(MediaStore.Video.Media.DATA)); // 文件路径
				String name = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
				long date = cursor.getLong(cursor
						.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED));
				date = date * 1000L;
				String videoType = FileManager.getExtFromFilename(name);
				if ("".equals(videoType)) {
					videoType = mContext.getResources().getString(R.string.unknow);
				}
				
				dialog.setFileType(InfoDialog.VIDEO, videoType);
				dialog.setFileName(name);
				dialog.setFilePath(Utils.getParentPath(url));
				dialog.setFileSize(size);
				dialog.setModifyDate(date);
				
			}else {
				dialog.setType(InfoDialog.MULTI);
				int fileNum = list.size();
				long size = getTotalSize(list);
				dialog.updateUI(size, fileNum, 0);
			}
			dialog.show();
			dialog.scanOver();
			//info
			break;
		case R.id.menu_select:
			doCheckAll();
			break;

		default:
			break;
		}
	}
	
	@Override
	public void doCheckAll(){
		int selectedCount1 = mItemAdapter.getCheckedCount();
		if (mItemAdapter.getCount() != selectedCount1) {
			mItemAdapter.checkedAll(true);
		} else {
			mItemAdapter.checkedAll(false);
		}
		updateMenuBar();
		mMenuBarManager.refreshMenus(mActionMenu);
		mItemAdapter.notifyDataSetChanged();
	}
	
	public void destroyMenuBar() {
		destroyMenuBar(mMenuBarView);
//		mActivity.updateTitleNum(-1,count);
		mActivity.updateItemTitle(MediaType.Video, -1, count);
		
		mItemAdapter.changeMode(ActionMenu.MODE_NORMAL);
		mItemAdapter .checkedAll(false);
		mItemAdapter .notifyDataSetChanged();
	}
	
	@Override
	public void updateMenuBar(){
		int selectCount = mItemAdapter .getCheckedCount();
		mActivity.updateItemTitle(MediaType.Video, selectCount, count);

		ActionMenuItem selectItem = mActionMenu.findItem(R.id.menu_select);
		if (mItemAdapter.getCount() == selectCount) {
			selectItem.setTitle(R.string.unselect_all);
			selectItem.setEnableIcon(R.drawable.ic_aciton_unselect);
		}else {
			selectItem.setTitle(R.string.select_all);
			selectItem.setEnableIcon(R.drawable.ic_aciton_select);
		}

		if (0==selectCount) {
//			mActionMenu.findItem(R.id.menu_send).setEnable(false);
			mActionMenu.findItem(R.id.menu_delete).setEnable(false);
			mActionMenu.findItem(R.id.menu_info).setEnable(false);
		}else {
//			mActionMenu.findItem(R.id.menu_send).setEnable(true);
			mActionMenu.findItem(R.id.menu_delete).setEnable(true);
			mActionMenu.findItem(R.id.menu_info).setEnable(true);
		}
	}
}
