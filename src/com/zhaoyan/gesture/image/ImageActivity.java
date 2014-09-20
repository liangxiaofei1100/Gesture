package com.zhaoyan.gesture.image;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
import android.provider.MediaStore.MediaColumns;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import com.zhaoyan.common.utils.Utils;
import com.zhaoyan.common.views.TableTitleView;
import com.zhaoyan.common.views.TableTitleView.OnTableSelectChangeListener;
import com.zhaoyan.gesture.R;
import com.zhaoyan.gesture.activity.BaseFragmentActivity;
import com.zhaoyan.gesture.common.ZYConstant;
import com.zhaoyan.gesture.common.ZYConstant.Extra;

public class ImageActivity extends BaseFragmentActivity implements OnScrollListener,
		OnItemClickListener, OnItemLongClickListener, MenuBarInterface {
	private static final String TAG = "ImageActivity";

	private View mImageLayout, mVideoLayout;
	private TableTitleView mTableTitleView;

	public static final String IMAGE_TYPE = "IMAGE_TYPE";
	public static final int TYPE_PHOTO = 0;
	public static final int TYPE_GALLERY = 1;
	private int mImageType = -1;

	private GridView mGridView;
	private ListView mListView;

	private ProgressBar mLoadingBar;
	private ViewGroup mViewGroup;

	// private ImageAdapter mAdapter;
	private ImageGridAdapter mAdapter;
	private List<ImageInfo> mPictureItemInfoList = new ArrayList<ImageInfo>();

	private static final int QUERY_TOKEN_FOLDER = 0x11;
	private static final int QUERY_TOKEN_ITEM = 0x12;

	private static final String[] PROJECTION = new String[] { MediaColumns._ID,
			MediaColumns.DATE_MODIFIED, MediaColumns.SIZE,
			MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
			MediaColumns.DATA, MediaColumns.DISPLAY_NAME };

	private static final String[] PROJECTION_ICS = new String[] {
			MediaColumns._ID, MediaColumns.DATE_MODIFIED, MediaColumns.SIZE,
			MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
			MediaColumns.DATA, MediaColumns.DISPLAY_NAME, "width", "height" };
	
	public static final int LIMIT_SIZE = 11 * 1024;//if a image size below than 10K,we do not show it

	/** order by date_modified DESC */
	public static final String SORT_ORDER_DATE = MediaColumns.DATE_MODIFIED
			+ " DESC";
	private static final String CAMERA = "Camera";
	private static final String GALLERY = "Gallery";
	private static final String MIMETYPE_PNG = "image/png";

	private static final int REQUEST_CODE_PAGER = 0x10;

	private static final int MSG_UPDATE_UI = 0;
	private static final int MSG_UPDATE_LIST = 1;
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_UPDATE_UI:
				int num = msg.arg1;
				updateTitleNum(-1, num);
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
						mPictureItemInfoList.remove(removePosition);
						mAdapter.notifyDataSetChanged();
					}

					updateTitleNum(-1, mPictureItemInfoList.size());
				} else {
					Log.e(TAG, "bundle is null");
				}
				break;

			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
			    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
		setContentView(R.layout.image_main);

		Bundle bundle = getIntent().getExtras();
		mImageType = TYPE_GALLERY;
		if (bundle != null) {
			mImageType = bundle.getInt(IMAGE_TYPE);
		}

		initView();

		if (TYPE_PHOTO == mImageType) {
			initTitle(R.string.camera);
			queryFolderItem(CAMERA);
		} else if (TYPE_GALLERY == mImageType) {
			initTitle(R.string.gallery);
			queryFolderItem(GALLERY);
		} else {
			Log.e(TAG, "onCreate.error.mImageType=" + mImageType);
		}
		mBaseIntroductionView.setVisibility(View.GONE);
		setTitleNumVisible(true);
	}

	private void initView() {
		mMenuBarView = findViewById(R.id.bottom);
		mMenuBarView.setVisibility(View.GONE);
		
		mViewGroup = (ViewGroup) findViewById(R.id.rl_picture_main);
//		mGridView = (GridView) findViewById(R.id.gv_picture_item);
		mGridView.setOnScrollListener(this);
		mGridView.setOnItemClickListener(this);
		mGridView.setOnItemLongClickListener(this);

//		mListView = (ListView) findViewById(R.id.lv_picture_item);
		mListView.setOnScrollListener(this);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);

//		mLoadingBar = (ProgressBar) findViewById(R.id.bar_loading_image);

		// mAdapter = new ImageAdapter(getApplicationContext(), mViewType,
		// mPictureItemInfoList);
//		mAdapter = new ImageGridAdapter(this, 0, mPictureItemInfoList);

		// if (false) {
		// mListView.setVisibility(View.VISIBLE);
		// mGridView.setVisibility(View.GONE);
		// mListView.setAdapter(mAdapter);
		// } else {
		mListView.setVisibility(View.GONE);
		mGridView.setVisibility(View.VISIBLE);
		mGridView.setAdapter(mAdapter);
		// }
		mImageLayout = findViewById(R.id.image_grid_layout);
//		mVideoLayout = findViewById(R.id.video_grid_layout);
		mTableTitleView = (TableTitleView) findViewById(R.id.ttv_sc_title);
		mTableTitleView.initTitles(new String[] { getString(R.string.gallery),
				getString(R.string.camera), getString(R.string.video) });
		mTableTitleView.setOnTableSelectChangeListener(myOnClickListener);
		
		initMenuBar(mMenuBarView);
	}

	public void query(int token, String selection, String[] selectionArgs,
			String orderBy) {
		String[] projection = PROJECTION;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			projection = PROJECTION_ICS;
		}

		new QueryHandler(getApplicationContext().getContentResolver())
				.startQuery(token, null, ZYConstant.IMAGE_URI, projection,
						selection, selectionArgs, orderBy);
	}

	/**
	 * accord bucketName to query images
	 * 
	 * @param bucketName
	 *            the bucketName of the image belong
	 */
	public void queryFolderItem(String bucketName) {
		String selection;
		if (GALLERY.equals(bucketName)) {
			selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + "!=?";
		} else {
			selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + "=?";
		}
		String selectionArgs[] = { CAMERA };
		
		mPictureItemInfoList.clear();
		query(QUERY_TOKEN_ITEM, selection, selectionArgs, SORT_ORDER_DATE);
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
				switch (token) {
				case QUERY_TOKEN_FOLDER:
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
							
							long size = cursor.getLong(cursor.getColumnIndex(MediaColumns.SIZE));
							if (size <= LIMIT_SIZE) {
								continue;
							}

							imageInfo.setImageId(id);
							imageInfo.setPath(url);
							imageInfo.setDisplayName(name);

							mPictureItemInfoList.add(imageInfo);
						} while (cursor.moveToNext());
						cursor.close();
					}
					num = mPictureItemInfoList.size();
					mAdapter.notifyDataSetChanged();
					mAdapter.checkedAll(false);
					updateUI(num);
					break;
				default:
					Log.e(TAG, "Error token:" + token);
					break;
				}
			}
		}
	}

	private void updateUI(int num) {
		Message message = mHandler.obtainMessage();
		message.arg1 = num;
		message.what = MSG_UPDATE_UI;
		message.sendToTarget();
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
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
	}

	private void startPagerActivityByPosition(int position) {
		List<String> urlList = new ArrayList<String>();
		int count = mPictureItemInfoList.size();
		for (int i = 0; i < count; i++) {
			String url = mPictureItemInfoList.get(i).getPath();
			urlList.add(url);
		}
		Intent intent = new Intent(this, ImagePagerActivity.class);
		// // Intent intent = new Intent(this, OtherActivithy.class);
		intent.putExtra(Extra.IMAGE_POSITION, position);
		intent.putStringArrayListExtra(Extra.IMAGE_INFO,
				(ArrayList<String>) urlList);
		startActivityForResult(intent, REQUEST_CODE_PAGER);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (mAdapter.isMode(ActionMenu.MODE_EDIT)) {
			mAdapter.setChecked(position);
			mAdapter.notifyDataSetChanged();

			int selectedCount = mAdapter.getCheckedCount();
			updateTitleNum(selectedCount, mAdapter.getCount());
			updateMenuBar();
			mMenuBarManager.refreshMenus(mActionMenu);
		} else {
			startPagerActivityByPosition(position);
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		if (mAdapter.isMode(ActionMenu.MODE_EDIT)) {
			// do nothing
//			doCheckAll();
			return true;
		} else {
			mAdapter.changeMode(ActionMenu.MODE_EDIT);
			updateTitleNum(1, mAdapter.getCount());
		}

		mAdapter.setChecked(position, true);
		mAdapter.notifyDataSetChanged();

		mActionMenu = new ActionMenu(getApplicationContext());
		getActionMenuInflater().inflate(R.menu.image_menu, mActionMenu);

		startMenuBar(mMenuBarView);
		return true;
	}

	@Override
	public boolean onBackKeyPressed() {
		if (mAdapter.isMode(ActionMenu.MODE_EDIT)) {
			destroyMenuBar();
			return false;
		} else {
			if(mVideoLayout.getVisibility()==View.VISIBLE){
				FragmentManager ft = getSupportFragmentManager();
				VideoFragment video = (VideoFragment) ft
						.findFragmentByTag(getString(R.string.video));
			return	video.onBackPressed();
			}
			return true;
		}
	}

	public void onMenuItemClick(ActionMenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_delete:
			showDeleteDialog();
			break;
		case R.id.menu_info:
			List<Integer> list = mAdapter.getCheckedPosList();
			InfoDialog dialog = new InfoDialog(this);
			dialog.setDialogTitle("信息");
			if (1 == list.size()) {
				dialog.setType(InfoDialog.SINGLE_FILE);
				int position = list.get(0);
				String url = mPictureItemInfoList.get(position).getPath();
				String displayName = mPictureItemInfoList.get(position)
						.getDisplayName();
				File file = new File(url);
				long size = file.length();
				long date = file.lastModified();

				String imageType = FileManager.getExtFromFilename(displayName);
				if ("".equals(imageType)) {
					imageType = getApplicationContext().getResources()
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
					size = new File(mPictureItemInfoList.get(pos).getPath())
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
		List<String> deleteNameList = mAdapter.getCheckedNameList();

		ZyDeleteDialog deleteDialog = new ZyDeleteDialog(this);
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
				final List<Integer> posList = mAdapter
						.getCheckedPosList();
				FileDeleteHelper deleteHelper = new FileDeleteHelper(
						ImageActivity.this);
				deleteHelper.setDeletePathList(mAdapter
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
		
		updateTitleNum(-1, mAdapter.getCount());

		mAdapter.changeMode(ActionMenu.MODE_NORMAL);
		mAdapter.checkedAll(false);
		mAdapter.notifyDataSetChanged();
	}

	public void updateMenuBar() {
		int selectCount = mAdapter.getCheckedCount();
		updateTitleNum(selectCount, mAdapter.getCount());

		ActionMenuItem selectItem = mActionMenu.findItem(R.id.menu_select);
		if (mAdapter.getCount() == selectCount) {
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_PAGER) {
			List<Integer> deleteList = data
					.getIntegerArrayListExtra(ImagePagerActivity.DELETE_POSITION);
			int removePosition;
			for (int i = 0; i < deleteList.size(); i++) {
				// remove from the last item to the first item
				removePosition = deleteList.get(deleteList.size() - (i + 1));
				mPictureItemInfoList.remove(removePosition);
				mAdapter.notifyDataSetChanged();

				updateTitleNum(-1, mPictureItemInfoList.size());
			}
		}
	}

	public static class PhotoActivity extends ImageActivity {

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			Intent intent = getIntent();
			intent.putExtra(IMAGE_TYPE, TYPE_PHOTO);
			super.onCreate(savedInstanceState);
		}
	}

	private OnTableSelectChangeListener myOnClickListener = new TableTitleView.OnTableSelectChangeListener() {

		@Override
		public void onTableSelect(int position) {
			// TODO Auto-generated method stub
			switch (position) {
			case 0:
				if (mImageLayout.getVisibility() == View.GONE) {
					mVideoLayout.setVisibility(View.GONE);
					mImageLayout.setVisibility(View.VISIBLE);
				}
				queryFolderItem(GALLERY);
				initTitle(R.string.gallery);
				break;
			case 1:
				if (mImageLayout.getVisibility() == View.GONE) {
					mVideoLayout.setVisibility(View.GONE);
					mImageLayout.setVisibility(View.VISIBLE);
				}
				queryFolderItem(CAMERA);
				initTitle(R.string.camera);
				break;
			case 2:
				if (mVideoLayout.getVisibility() == View.GONE) {
					mVideoLayout.setVisibility(View.VISIBLE);
					mImageLayout.setVisibility(View.GONE);
				}
				FragmentManager ft = getSupportFragmentManager();
				VideoFragment video = (VideoFragment) ft
						.findFragmentByTag(getString(R.string.video));
				if (video == null) {
					video = new VideoFragment();
					ft.beginTransaction()
							.add(R.id.content_layout, video,
									getString(R.string.video)).commit();
				} else {
					video.query();
				}
				initTitle(R.string.video);
				break;
			default:
				break;
			}
		}
	};

}
