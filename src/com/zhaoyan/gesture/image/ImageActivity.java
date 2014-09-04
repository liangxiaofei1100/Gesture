package com.zhaoyan.gesture.image;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.zhaoyan.gesture.R;
import com.zhaoyan.gesture.activity.BaseActivity;
import com.zhaoyan.gesture.image.ActionMenu.ActionMenuItem;
import com.zhaoyan.gesture.image.ActionMenuInterface.OnMenuItemClickListener;
import com.zhaoyan.gesture.image.FileDeleteHelper.OnDeleteListener;
import com.zhaoyan.gesture.image.ZYConstant.Extra;
import com.zhaoyan.gesture.image.ZyAlertDialog.OnZyAlertDlgClickListener;

import android.app.Dialog;
import android.app.FragmentManager;
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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

public class ImageActivity extends BaseActivity implements OnScrollListener,
		OnItemClickListener, OnItemLongClickListener, MenuBarInterface,
		OnMenuItemClickListener {
	private static final String TAG = "ImageActivity";

	private TextView mGalleryTv, mCameraTv, mVideoTv;
	private View mImageLayout, mVideoLayout;

	public static final String IMAGE_TYPE = "IMAGE_TYPE";
	public static final int TYPE_PHOTO = 0;
	public static final int TYPE_GALLERY = 1;
	private int mImageType = -1;

	private GridView mGridView;
	private ListView mListView;

	private ProgressBar mLoadingBar;
	private ViewGroup mViewGroup;
	// menubar
	protected View mMenuBarView;
	protected LinearLayout mMenuHolder;
	protected MenuBarManager mMenuBarManager;
	protected ActionMenu mActionMenu;

	private ActionMenuInflater mActionMenuInflater;

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
		mViewGroup = (ViewGroup) findViewById(R.id.rl_picture_main);
		mGridView = (GridView) findViewById(R.id.gv_picture_item);
		mGridView.setOnScrollListener(this);
		mGridView.setOnItemClickListener(this);
		mGridView.setOnItemLongClickListener(this);

		mListView = (ListView) findViewById(R.id.lv_picture_item);
		mListView.setOnScrollListener(this);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);

		mLoadingBar = (ProgressBar) findViewById(R.id.bar_loading_image);

		// mAdapter = new ImageAdapter(getApplicationContext(), mViewType,
		// mPictureItemInfoList);
		mAdapter = new ImageGridAdapter(this, 0, mPictureItemInfoList);

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
		mVideoLayout = findViewById(R.id.video_grid_layout);
		mGalleryTv = (TextView) findViewById(R.id.gallery_image);
		mCameraTv = (TextView) findViewById(R.id.camera_image);
		mVideoTv = (TextView) findViewById(R.id.video_image);
		mGalleryTv.setOnClickListener(myOnClickListener);
		mCameraTv.setOnClickListener(myOnClickListener);
		mVideoTv.setOnClickListener(myOnClickListener);

		initMenuBar();
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
		// do not load png image
		if (GALLERY.equals(bucketName)) {
			selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + "!=?"
					+ " and " + MediaStore.Images.Media.MIME_TYPE + "!=?";
		} else {
			selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + "=?"
					+ " and " + MediaStore.Images.Media.MIME_TYPE + "!=?";
		}
		String selectionArgs[] = { CAMERA, MIMETYPE_PNG };
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
			doCheckAll();
			return true;
		} else {
			mAdapter.changeMode(ActionMenu.MODE_EDIT);
			updateTitleNum(1, mAdapter.getCount());
		}

		mAdapter.setChecked(position, true);
		mAdapter.notifyDataSetChanged();

		mActionMenu = new ActionMenu(getApplicationContext());
		getActionMenuInflater().inflate(R.menu.image_menu, mActionMenu);

		startMenuBar();
		return true;
	}

	@Override
	public boolean onBackKeyPressed() {
		if (mAdapter.isMode(ActionMenu.MODE_EDIT)) {
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
			List<Integer> list = mAdapter.getCheckedPosList();
			InfoDialog dialog = null;
			if (1 == list.size()) {
				dialog = new InfoDialog(this, InfoDialog.SINGLE_FILE);
				dialog.setTitle("信息");
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
				dialog.setFilePath(ZYUtils.getParentPath(url));
				dialog.setModifyDate(date);
				dialog.setFileSize(size);

			} else {
				dialog = new InfoDialog(this, InfoDialog.MULTI);
				dialog.setTitle("信息");
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
	 * 
	 * @param path
	 *            file path
	 */
	public void showDeleteDialog() {
		List<String> deleteNameList = mAdapter.getCheckedNameList();

		ZyDeleteDialog deleteDialog = new ZyDeleteDialog(this);
		deleteDialog.setTitle(R.string.delete_image);
		String msg = "";
		if (deleteNameList.size() == 1) {
			msg = getString(R.string.delete_file_confirm_msg,
					deleteNameList.get(0));
		} else {
			msg = getString(R.string.delete_file_confirm_msg_image,
					deleteNameList.size());
		}
		deleteDialog.setMessage(msg);
		deleteDialog.setPositiveButton(R.string.menu_delete,
				new OnZyAlertDlgClickListener() {
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
		deleteDialog.setNegativeButton(R.string.cancel, null);
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
		mMenuBarView.setVisibility(View.GONE);
		mMenuBarView.clearAnimation();
		mMenuBarView.startAnimation(AnimationUtils.loadAnimation(this,
				R.anim.slide_down_out));
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

	protected ActionMenuInflater getActionMenuInflater() {
		if (null == mActionMenuInflater) {
			mActionMenuInflater = new ActionMenuInflater(
					getApplicationContext());
		}
		return mActionMenuInflater;
	}

	protected void initMenuBar() {
		mMenuBarView = findViewById(R.id.menubar_bottom);
		mMenuBarView.setVisibility(View.GONE);
		mMenuHolder = (LinearLayout) findViewById(R.id.ll_menutabs_holder);

		mMenuBarManager = new MenuBarManager(getApplicationContext(),
				mMenuHolder);
		mMenuBarManager.setOnMenuItemClickListener(this);
	}

	public void startMenuBar() {
		mMenuBarView.setVisibility(View.VISIBLE);
		mMenuBarView.clearAnimation();
		mMenuBarView.startAnimation(AnimationUtils.loadAnimation(this,
				R.anim.slide_up_in));
		mMenuBarManager.refreshMenus(mActionMenu);
	}

	private OnClickListener myOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.gallery_image:
				if (mImageLayout.getVisibility() == View.GONE) {
					mVideoLayout.setVisibility(View.GONE);
					mImageLayout.setVisibility(View.VISIBLE);
				}
				queryFolderItem(GALLERY);
				initTitle(R.string.gallery);
				break;
			case R.id.camera_image:
				if (mImageLayout.getVisibility() == View.GONE) {
					mVideoLayout.setVisibility(View.GONE);
					mImageLayout.setVisibility(View.VISIBLE);
				}
				queryFolderItem(CAMERA);
				initTitle(R.string.camera);
				break;
			case R.id.video_image:
				if (mVideoLayout.getVisibility() == View.GONE) {
					mVideoLayout.setVisibility(View.VISIBLE);
					mImageLayout.setVisibility(View.GONE);
				}
				FragmentManager ft = getFragmentManager();
				VideoFragment video = (VideoFragment) ft
						.findFragmentByTag(getString(R.string.video));
				if (video == null) {
					video = new VideoFragment();
					ft.beginTransaction()
							.add(R.id.video_grid_layout, video,
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
