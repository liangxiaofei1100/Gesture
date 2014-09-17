package com.zhaoyan.gesture.image;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.MediaColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;

import com.zhaoyan.common.actionmenu.ActionMenu;
import com.zhaoyan.common.actionmenu.ActionMenu.ActionMenuItem;
import com.zhaoyan.common.dialog.ZyDeleteDialog;
import com.zhaoyan.common.dialog.ZyDialogBuilder.onZyDialogClickListener;
import com.zhaoyan.common.file.FileDeleteHelper;
import com.zhaoyan.common.file.FileDeleteHelper.OnDeleteListener;
import com.zhaoyan.common.utils.FileManager;
import com.zhaoyan.common.utils.Log;
import com.zhaoyan.common.utils.Utils;
import com.zhaoyan.gesture.R;
import com.zhaoyan.gesture.common.ZYConstant.Extra;
import com.zhaoyan.gesture.fragment.BaseV4Fragment;
import com.zhaoyan.gesture.image.ImageMainActivity.MediaType;

public class ImageFragment extends BaseV4Fragment implements
		OnItemClickListener, OnItemLongClickListener {
	private static final String TAG = ImageFragment.class.getSimpleName();

	private GridView mFolderGridView;
	private GridView mItemGridView;

	private static final String[] PROJECTION = new String[] { MediaColumns._ID,
			MediaColumns.DATE_MODIFIED, MediaColumns.SIZE,
			MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
			MediaColumns.DATA, MediaColumns.DISPLAY_NAME };

	private static final String[] PROJECTION_ICS = new String[] {
			MediaColumns._ID, MediaColumns.DATE_MODIFIED, MediaColumns.SIZE,
			MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
			MediaColumns.DATA, MediaColumns.DISPLAY_NAME, "width", "height" };

	private static final int QUERY_TOKEN_FOLDER = 0x11;
	private static final int QUERY_TOKEN_ITEM = 0x12;

	private static final int LIMIT_SIZE = 11 * 1024;// if a image size below
													// than 10K,we do not show
													// it

	/** order by date_modified DESC */
	public static final String SORT_ORDER_DATE = MediaColumns.DATE_MODIFIED
			+ " DESC";
	private static final String SORT_ORDER_BUCKET = MediaStore.Images.Media.BUCKET_DISPLAY_NAME
			+ " ASC";

	private static final String CAMERA = "Camera";

	private List<ImageFolderInfo> mFolderInfosList = new ArrayList<ImageFolderInfo>();
	private ImageFolderAdapter mFolderAdapter;

	private ImageGridAdapter mItemAdapter;
	private List<ImageInfo> mItemInfoList = new ArrayList<ImageInfo>();
	
	private static final int REQUEST_CODE_PAGER = 0x10;

	private QueryHandler mQueryHandler;

	private ImageMainActivity mActivity;

	public void onAttach(android.app.Activity activity) {
		super.onAttach(activity);
		mActivity = (ImageMainActivity) activity;
	};
	
	private static final int MSG_UPDATE_UI = 0;
	private static final int MSG_UPDATE_LIST = 1;
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_UPDATE_UI:
				int num = msg.arg1;
				mActivity.updateItemTitle(MediaType.Image, -1, num);
				break;
			case MSG_UPDATE_LIST:
				List<Integer> poslist = new ArrayList<Integer>();
				Bundle bundle = msg.getData();
				if (null != bundle) {
					poslist = bundle.getIntegerArrayList("position");
					// Log.d(TAG, "poslist.size=" + poslist);
					int removePosition;
					for (int i = 0; i < poslist.size(); i++) {
						// remove from the last item to the first item
						removePosition = poslist.get(poslist.size() - (i + 1));
						// Log.d(TAG, "removePosition:" + removePosition);
						mItemInfoList.remove(removePosition);
						mItemAdapter.notifyDataSetChanged();
					}
					mActivity.updateItemTitle(MediaType.Image, -1, mItemInfoList.size());
				} else {
					Log.e(TAG, "bundle is null");
				}
				break;

			default:
				break;
			}
		};
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.image_fragment_main, null);

		mFolderGridView = (GridView) rootView
				.findViewById(R.id.image_fragment_floder_gv);
		mFolderGridView.setOnItemClickListener(this);
		mItemGridView = (GridView) rootView
				.findViewById(R.id.image_fragment_item_gv);
		mItemGridView.setOnItemClickListener(this);
		mItemGridView.setOnItemLongClickListener(this);

		mMenuBarView = rootView.findViewById(R.id.bottom);
		mMenuBarView.setVisibility(View.GONE);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		Log.d(TAG, "onActivityCreated");
		initMenuBar(mMenuBarView);

		mFolderAdapter = new ImageFolderAdapter(getActivity(), mFolderInfosList);

		mItemAdapter = new ImageGridAdapter(getActivity(), 0, mItemInfoList);

		mQueryHandler = new QueryHandler(getActivity().getContentResolver());
		queryFolder();
	}

	/** query all */
	public void queryFolder() {
		Log.d(TAG, "queryFolder()");
		mFolderInfosList.clear();
		query(QUERY_TOKEN_FOLDER, null, null, SORT_ORDER_DATE);
	}

	public void queryFolderItem(String bucketName) {
		mItemInfoList.clear();

		String selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + "=?";
		String selectionArgs[] = { bucketName };
		query(QUERY_TOKEN_ITEM, selection, selectionArgs, SORT_ORDER_DATE);
	}

	public void query(int token, String selection, String[] selectionArgs,
			String orderBy) {
		String[] projection = PROJECTION;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			projection = PROJECTION_ICS;
		}
		mQueryHandler.startQuery(token, null,
				Images.Media.EXTERNAL_CONTENT_URI, projection, selection,
				selectionArgs, orderBy);
	}

	// query db
	private class QueryHandler extends AsyncQueryHandler {

		public QueryHandler(ContentResolver cr) {
			super(cr);
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			Log.d(TAG, "onQueryComplete");
			// mLoadingBar.setVisibility(View.INVISIBLE);
			int num = 0;
			if (null != cursor) {
				Log.d(TAG, "onQueryComplete.count=" + cursor.getCount());
				switch (token) {
				case QUERY_TOKEN_FOLDER:
					if (cursor.moveToFirst()) {
						ImageFolderInfo imageFolderInfo = null;
						do {
							long id = cursor.getLong(cursor
									.getColumnIndex(MediaColumns._ID));
							String bucketDisplayName = cursor
									.getString(cursor
											.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME));
							String path = cursor.getString(cursor
									.getColumnIndex(ImageColumns.DATA));
							imageFolderInfo = getFolderInfo(bucketDisplayName);
							if (null == imageFolderInfo) {
								imageFolderInfo = new ImageFolderInfo();
								imageFolderInfo
										.setBucketDisplayName(bucketDisplayName);
								imageFolderInfo.setImagePath(path);
								System.out.println("path:" + path);
								imageFolderInfo.addIdToList(id);
								if (CAMERA.equals(bucketDisplayName)) {
									imageFolderInfo.setDisplayName("相机");
									mFolderInfosList.add(0, imageFolderInfo);
								} else {
									imageFolderInfo
											.setDisplayName(bucketDisplayName);
									mFolderInfosList.add(imageFolderInfo);
								}
							} else {
								imageFolderInfo.addIdToList(id);
							}
						} while (cursor.moveToNext());
						cursor.close();
					}
					mFolderAdapter.setCheckedPosition(0);
					mFolderGridView.setAdapter(mFolderAdapter);
					// mFolderAdapter.notifyDataSetChanged();
					queryFolderItem(CAMERA);
					break;
				case QUERY_TOKEN_ITEM:
					if (cursor.moveToFirst()) {
						do {
							ImageInfo imageInfo = new ImageInfo();
							long id = cursor.getLong(cursor
									.getColumnIndex(MediaColumns._ID));
							String url = cursor
									.getString(cursor
											.getColumnIndex(MediaStore.MediaColumns.DATA));
							String name = cursor.getString(cursor
									.getColumnIndex(MediaColumns.DISPLAY_NAME));

							// long size = cursor.getLong(cursor
							// .getColumnIndex(MediaColumns.SIZE));
							// if (size <= LIMIT_SIZE) {
							// continue;
							// }

							imageInfo.setImageId(id);
							imageInfo.setPath(url);
							imageInfo.setDisplayName(name);

							mItemInfoList.add(imageInfo);
						} while (cursor.moveToNext());
						cursor.close();
					}
					num = mItemInfoList.size();
					mItemGridView.setAdapter(mItemAdapter);
					mItemAdapter.checkedAll(false);
					mActivity.updateItemTitle(MediaType.Image, -1, num);
					break;
				default:
					Log.e(TAG, "Error token:" + token);
					break;
				}
			}
		}
	}

	/***
	 * get {@link ImageFolderInfo} from {@link mFolderInfosList} accord to the
	 * speciy bucketDisplayName}}
	 * 
	 * @param bucketDisplayName
	 * @return {@link PictureFolderInfo}, null if not find
	 */
	public ImageFolderInfo getFolderInfo(String bucketDisplayName) {
		for (ImageFolderInfo folderInfo : mFolderInfosList) {
			if (bucketDisplayName.equals(folderInfo.getBucketDisplayName())) {
				return folderInfo;
			}
		}
		return null;
	}
	
	private void startPagerActivityByPosition(int position) {
		List<String> urlList = new ArrayList<String>();
		int count = mItemInfoList.size();
		for (int i = 0; i < count; i++) {
			String url = mItemInfoList.get(i).getPath();
			urlList.add(url);
		}
		Intent intent = new Intent(getActivity(), ImagePagerActivity.class);
		intent.putExtra(Extra.IMAGE_POSITION, position);
		intent.putStringArrayListExtra(Extra.IMAGE_INFO,
				(ArrayList<String>) urlList);
		startActivityForResult(intent, REQUEST_CODE_PAGER);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		switch (parent.getId()) {
		case R.id.image_fragment_floder_gv:
			mFolderAdapter.setCheckedPosition(position);
			mFolderAdapter.notifyDataSetChanged();
			String folder = mFolderInfosList.get(position)
					.getBucketDisplayName();
			queryFolderItem(folder);
			break;
		case R.id.image_fragment_item_gv:
			if (mItemAdapter.isMode(ActionMenu.MODE_EDIT)) {
				mItemAdapter.setChecked(position);
				mItemAdapter.notifyDataSetChanged();

				int selectedCount = mItemAdapter.getCheckedCount();
				mActivity.updateItemTitle(MediaType.Image, selectedCount, mItemAdapter.getCount());
				updateMenuBar();
				mMenuBarManager.refreshMenus(mActionMenu);
			} else {
				startPagerActivityByPosition(position);
			}
			break;

		default:
			break;
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		if (mItemAdapter.isMode(ActionMenu.MODE_EDIT)) {
			// do nothing
//			doCheckAll();
			return true;
		} else {
			mItemAdapter.changeMode(ActionMenu.MODE_EDIT);
			mActivity.updateItemTitle(MediaType.Image, 1, mItemAdapter.getCount());
		}

		mItemAdapter.setChecked(position, true);
		mItemAdapter.notifyDataSetChanged();

		mActionMenu = new ActionMenu(getActivity().getApplicationContext());
		getActionMenuInflater().inflate(R.menu.image_menu, mActionMenu);

		startMenuBar(mMenuBarView);
		return true;
	}
	
	@Override
	public boolean onBackPressed() {
		Log.d(TAG, "onBackPressed()");
		if (mItemAdapter.isMode(ActionMenu.MODE_EDIT)) {
			destroyMenuBar();
			return false;
		} else {
			return true;
		}
	}
	
	public void onMenuItemClick(ActionMenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_delete:
			showDeleteDialog();
			break;
		case R.id.menu_info:
			List<Integer> list = mItemAdapter.getCheckedPosList();
			InfoDialog dialog = new InfoDialog(getActivity());
			dialog.setDialogTitle("信息");
			if (1 == list.size()) {
				dialog.setType(InfoDialog.SINGLE_FILE);
				int position = list.get(0);
				String url = mItemInfoList.get(position).getPath();
				String displayName = mItemInfoList.get(position)
						.getDisplayName();
				File file = new File(url);
				long size = file.length();
				long date = file.lastModified();

				String imageType = FileManager.getExtFromFilename(displayName);
				if ("".equals(imageType)) {
					imageType = getActivity().getResources()
							.getString(R.string.unknow);
				}

				dialog.setFileType(InfoDialog.IMAGE, imageType);
				dialog.setFileName(displayName);
				dialog.setFilePath(Utils.getParentPath(url));
				dialog.setModifyDate(date);
				dialog.setFileSize(size);

			} else {
				dialog.setType(InfoDialog.MULTI);
				int fileNum = list.size();
				long totalSize = 0;
				long size = 0;
				for (int pos : list) {
					size = new File(mItemInfoList.get(pos).getPath())
							.length();
					totalSize += size;
				}
				dialog.updateUI(totalSize, fileNum, 0);
			}
			dialog.show();
			dialog.scanOver();
			// info
			break;
		case R.id.menu_select:
			doCheckAll();
			break;

		default:
			break;
		}
	}
	
	/**
	 * show confrim dialog
	 * @param path
	 *            file path
	 */
	public void showDeleteDialog() {
		List<String> deleteNameList = mItemAdapter.getCheckedNameList();

		ZyDeleteDialog deleteDialog = new ZyDeleteDialog(getActivity());
		String msg = "";
		if (deleteNameList.size() == 1) {
			msg = getString(R.string.delete_one_image_tip);
		} else {
			msg = getString(R.string.delete_some_images_tip, deleteNameList.size());
		}
		deleteDialog.setDeleteMsg(msg);
		deleteDialog.setNegativeButton(R.string.cancel, null);
		deleteDialog.setPositiveButton(R.string.menu_delete, new onZyDialogClickListener() {
			@Override
			public void onClick(Dialog dialog) {
				final List<Integer> posList = mItemAdapter
						.getCheckedPosList();
				FileDeleteHelper deleteHelper = new FileDeleteHelper(getActivity());
				deleteHelper.setDeletePathList(mItemAdapter
						.getCheckedPathList());
				deleteHelper
						.setOnDeleteListener(new OnDeleteListener() {
							@Override
							public void onDeleteFinished() {
								// when delete over,send message to
								// update ui
								Message message = mHandler
										.obtainMessage();
								Bundle bundle = new Bundle();
								bundle.putIntegerArrayList("position",
										(ArrayList<Integer>) posList);
								message.setData(bundle);
								message.what = MSG_UPDATE_LIST;
								message.sendToTarget();
							}
						});
				deleteHelper.doDelete();

				dialog.dismiss();
				destroyMenuBar();
			}
		});
		deleteDialog.show();
	}
	
	public void doCheckAll() {
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
		
		mActivity.updateItemTitle(MediaType.Image, -1, mItemAdapter.getCount());

		mItemAdapter.changeMode(ActionMenu.MODE_NORMAL);
		mItemAdapter.checkedAll(false);
		mItemAdapter.notifyDataSetChanged();
	}

	public void updateMenuBar() {
		int selectCount = mItemAdapter.getCheckedCount();
		mActivity.updateItemTitle(MediaType.Image, selectCount, mItemAdapter.getCount());

		ActionMenuItem selectItem = mActionMenu.findItem(R.id.menu_select);
		if (mItemAdapter.getCount() == selectCount) {
			selectItem.setTitle(R.string.unselect_all);
			selectItem.setEnableIcon(R.drawable.ic_aciton_unselect);
		} else {
			selectItem.setTitle(R.string.select_all);
			selectItem.setEnableIcon(R.drawable.ic_aciton_select);
		}

		if (0 == selectCount) {
			mActionMenu.findItem(R.id.menu_delete).setEnable(false);
			mActionMenu.findItem(R.id.menu_info).setEnable(false);
		} else {
			mActionMenu.findItem(R.id.menu_delete).setEnable(true);
			mActionMenu.findItem(R.id.menu_info).setEnable(true);
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_PAGER) {
			List<Integer> deleteList = data
					.getIntegerArrayListExtra(ImagePagerActivity.DELETE_POSITION);
			int removePosition;
			for (int i = 0; i < deleteList.size(); i++) {
				// remove from the last item to the first item
				removePosition = deleteList.get(deleteList.size() - (i + 1));
				mItemInfoList.remove(removePosition);
				mItemAdapter.notifyDataSetChanged();

				mActivity.updateItemTitle(MediaType.Image, -1, mItemInfoList.size());
			}
		}
	}

}
