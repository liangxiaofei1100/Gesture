package com.zhaoyan.gesture.image;

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
import android.widget.ListView;
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
	private GridView mGridView;
	private ListView mListView;
	private ProgressBar mLoadingBar;
	
	private VideoCursorAdapter mAdapter;
	private QueryHandler mQueryHandler = null;
	
	private Context mContext;
	
	private static final String[] PROJECTION = new String[] {MediaStore.Video.Media._ID, 
		MediaStore.Video.Media.DURATION, MediaStore.Video.Media.SIZE,
		MediaColumns.DATA, MediaStore.Video.Media.DISPLAY_NAME,
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
				count = mAdapter.getCount();
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
		mGridView = (GridView) rootView.findViewById(R.id.video_gridview);
		mGridView.setOnItemClickListener(this);
		mGridView.setOnItemLongClickListener(this);
		mGridView.setDrawSelectorOnTop(true);
		mGridView.setOnScrollListener(this);
		
		mListView = (ListView) rootView.findViewById(R.id.lv_video);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		mListView.setOnScrollListener(this);
		
		mLoadingBar = (ProgressBar) rootView.findViewById(R.id.bar_video_loading);
		mAdapter = new VideoCursorAdapter(mContext);
//		mAdapter.setCurrentViewType(mViewType);
		if (isListView()) {
			mListView.setVisibility(View.VISIBLE);
			mGridView.setVisibility(View.GONE);
			mListView.setAdapter(mAdapter);
		} else {
			mListView.setVisibility(View.GONE);
			mGridView.setVisibility(View.VISIBLE);
			mGridView.setAdapter(mAdapter);
		}
		
//		initTitle(rootView.findViewById(R.id.rl_video_main), R.string.video);
		mMenuBarView = rootView.findViewById(R.id.bottom);
		mMenuBarView.setVisibility(View.GONE);
		initMenuBar(mMenuBarView);
		
		return rootView;
	}
	
	@Override
	public void onDestroyView() {
		if (mAdapter != null && mAdapter.getCursor() != null) {
			mAdapter.getCursor().close();
			mAdapter.changeCursor(null);
		}
		super.onDestroyView();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mQueryHandler = new QueryHandler(getActivity().getApplicationContext()
				.getContentResolver());

		query();
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
			mLoadingBar.setVisibility(View.INVISIBLE);
			int num = 0;
			if (null != cursor) {
				Log.d(TAG, "onQueryComplete.count=" + cursor.getCount());
				mAdapter.changeCursor(cursor);
				mAdapter.checkedAll(false);
				num = cursor.getCount();
			}
			updateUI(num);
		}

	}
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		switch (scrollState) {
		case OnScrollListener.SCROLL_STATE_FLING:
			mAdapter.setIdleFlag(false);
			break;
		case OnScrollListener.SCROLL_STATE_IDLE:
			mAdapter.setIdleFlag(true);
			mAdapter.notifyDataSetChanged();
			break;
		case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
			mAdapter.setIdleFlag(false);
			break;

		default:
			break;
		}
	}
	
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (mAdapter.isMode(ActionMenu.MODE_NORMAL)) {
			
			Cursor cursor = mAdapter.getCursor();
			cursor.moveToPosition(position);
			String url = cursor.getString(cursor
					.getColumnIndex(MediaStore.Video.Media.DATA)); // 文件路径
			IntentBuilder.viewFile(getActivity(), url);
		} else {
			mAdapter.setChecked(position);
			mAdapter.notifyDataSetChanged();
			
			int selectedCount = mAdapter.getCheckedCount();
			mActivity.updateItemTitle(MediaType.Video, selectedCount, count);
			updateMenuBar();
			mMenuBarManager.refreshMenus(mActionMenu);
		}
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, final View view, final int position, long id) {
		if (mAdapter.isMode(ActionMenu.MODE_EDIT)) {
			//do nothing
			doCheckAll();
			return true;
		} else {
			mAdapter.changeMode(ActionMenu.MODE_EDIT);
			mActivity.updateItemTitle(MediaType.Video, 1, count);
		}
		
		boolean isChecked = mAdapter.isChecked(position);
		mAdapter.setChecked(position, !isChecked);
		mAdapter.notifyDataSetChanged();
		
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
    	List<String> deleteNameList = mAdapter.getCheckedNameList();
    	
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
				List<String> deleteList = mAdapter.getCheckedPathList();
				
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
		Cursor cursor = mAdapter.getCursor();
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
		if (mAdapter.isMode(ActionMenu.MODE_EDIT)) {
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
			List<Integer> list = mAdapter.getCheckedPosList();
			InfoDialog dialog = new InfoDialog(getActivity());
			dialog.setDialogTitle(R.string.info_video_info);
			if (1 == list.size()) {
				dialog.setType(InfoDialog.SINGLE_FILE);
				Cursor cursor = mAdapter.getCursor();
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
		int selectedCount1 = mAdapter.getCheckedCount();
		if (mAdapter.getCount() != selectedCount1) {
			mAdapter.checkedAll(true);
		} else {
			mAdapter.checkedAll(false);
		}
		updateMenuBar();
		mMenuBarManager.refreshMenus(mActionMenu);
		mAdapter.notifyDataSetChanged();
	}
	
	public void destroyMenuBar() {
		destroyMenuBar(mMenuBarView);
//		mActivity.updateTitleNum(-1,count);
		mActivity.updateItemTitle(MediaType.Video, -1, count);
		
		mAdapter.changeMode(ActionMenu.MODE_NORMAL);
		mAdapter.checkedAll(false);
		mAdapter.notifyDataSetChanged();
	}
	
	@Override
	public void updateMenuBar(){
		int selectCount = mAdapter.getCheckedCount();
		mActivity.updateItemTitle(MediaType.Video, selectCount, count);

		ActionMenuItem selectItem = mActionMenu.findItem(R.id.menu_select);
		if (mAdapter.getCount() == selectCount) {
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
