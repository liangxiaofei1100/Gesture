package com.zhaoyan.gesture.app;

import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import com.zhaoyan.common.actionmenu.ActionMenu;
import com.zhaoyan.common.actionmenu.ActionMenu.ActionMenuItem;
import com.zhaoyan.common.actionmenu.MenuBarInterface;
import com.zhaoyan.common.dialog.ZyDialogBuilder.onZyDialogClickListener;
import com.zhaoyan.common.utils.AppUtil;
import com.zhaoyan.common.utils.Log;
import com.zhaoyan.gesture.R;
import com.zhaoyan.gesture.app.AppLauncherActivity.AppListLoader;
import com.zhaoyan.gesture.fragment.BaseFragment;

public class AppGridFragment extends BaseFragment implements android.view.View.OnClickListener, OnItemLongClickListener, 
	OnItemClickListener, LoaderCallbacks<List<AppEntry>>, MenuBarInterface {
	private static final String TAG = AppGridFragment.class.getSimpleName();
	// This is the Adapter being used to display the list's data.
	AppListAdapter mAdapter;

	// If non-null, this is the current filter the user has provided.
	String mCurFilter;

	ProgressBar mLoadingBar;
	Button mCancelBtn, mOkBtn;

	View mBottomView;

	boolean mIsSelectMode = false;
	
	private GridView mGridView;
	private AppGridAdapter mGridAdapter;
	
	private List<AppEntry> mAppLists;
	
	private View mButtonLayoutView;
	private View mAcitonMenuView;
	
	private AppLauncherActivity mActivity;
	
	private AppDialog mAppDialog = null;
	private List<String> mUninstallList = null;
	
	private static final int REQUEST_CODE_UNINSTALL = 0x11;
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		mActivity = (AppLauncherActivity) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		Bundle bundle = getArguments();
		if (bundle != null) {
			mIsSelectMode = bundle.getBoolean("selectMode");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.layout_app_main, null);
		
		mGridView = (GridView) rootView.findViewById(R.id.app_gridview);

		mLoadingBar = (ProgressBar) rootView.findViewById(R.id.app_progressbar);
		mCancelBtn = (Button) rootView.findViewById(R.id.btn_cancel);
		mOkBtn = (Button) rootView.findViewById(R.id.btn_ok);
		mCancelBtn.setOnClickListener(this);
		mOkBtn.setOnClickListener(this);
		mOkBtn.setEnabled(false);

		mBottomView = rootView.findViewById(R.id.bottom);
		mBottomView.setVisibility(View.GONE);
		
		mAcitonMenuView = rootView.findViewById(R.id.layout_actionmenu);
		
		mButtonLayoutView = rootView.findViewById(R.id.button_layout);

		initMenuBar(mAcitonMenuView);
		return rootView;
	}
	
	

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_cancel:
			getActivity().finish();
			break;
		case R.id.btn_ok:
			AppEntry appEntry = mGridAdapter.getSelectEntry();
			Intent intent = new Intent();
			intent.setPackage(appEntry.getApplicationInfo().packageName);
			getActivity().setResult(Activity.RESULT_OK, intent);
			getActivity().finish();
			break;
		default:
			break;
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// We have a menu item to show in action bar.
		setHasOptionsMenu(true);

		if (mIsSelectMode) {
			mButtonLayoutView.setVisibility(View.VISIBLE);
		} else {
			mButtonLayoutView.setVisibility(View.GONE);
			mGridView.setOnItemLongClickListener(this);
		}

		// Create an empty adapter we will use to display the loaded data.
		mGridAdapter = new AppGridAdapter(getActivity());
		mGridView.setAdapter(mGridAdapter);

		// Start out with a progress indicator.
		// setListShown(false);
		mGridView.setOnItemClickListener(this);

		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Place an action bar item for searching.
		MenuItem item = menu.add("Search");
		item.setIcon(android.R.drawable.ic_menu_search);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
				| MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
		SearchView sv = new SearchView(getActivity());
//		sv.setOnQueryTextListener(this);
		item.setActionView(sv);
	}

//	@Override
//	public boolean onQueryTextChange(String newText) {
//		// Called when the action bar search text has changed. Since this
//		// is a simple array adapter, we can just have it do the filtering.
//		mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
//		mAdapter.getFilter().filter(mCurFilter);
//		return true;
//	}
//
//	@Override
//	public boolean onQueryTextSubmit(String query) {
//		// Don't care about this.
//		return true;
//	}


	@Override
	public Loader<List<AppEntry>> onCreateLoader(int id, Bundle args) {
		return new AppListLoader(getActivity());
	}

	@Override
	public void onLoadFinished(Loader<List<AppEntry>> loader,
			List<AppEntry> data) {
		// Set the new data in the adapter.
		Log.d(TAG, "onLoadFinished:" + data.size());
		count = data.size();
		mActivity.updateTitleNum(-1, count);
		
		mGridAdapter.setData(data);
		mLoadingBar.setVisibility(View.GONE);
	}

	@Override
	public void onLoaderReset(Loader<List<AppEntry>> loader) {
		// Clear the data in the adapter.
		mGridAdapter.setData(null);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (mIsSelectMode) {
			mGridAdapter.setSelect(position);
			mOkBtn.setEnabled(true);
			return;
		} 
			
		if (mGridAdapter.isMode(ActionMenu.MODE_EDIT)) {
			mGridAdapter.setChecked(position);
			mGridAdapter.notifyDataSetChanged();
			updateMenuBar();
		} else {
			AppEntry appEntry = (AppEntry) mGridAdapter.getItem(position);
			Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage(appEntry.getPackageName());
			if (null != intent) {
				startActivity(intent);
			} else {
				Toast.makeText(getActivity(), "Cannot open this app", Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		// TODO Auto-generated method stub
		if (mIsSelectMode) {
			return false;
		}
		
		if (mGridAdapter.isMode(ActionMenu.MODE_EDIT)) {
			//do nothing
			return false;
		} else {
			mGridAdapter.changeMode(ActionMenu.MODE_EDIT);
			mActivity.updateTitleNum(1, count);
		}
		mGridAdapter.setChecked(position, true);
		mGridAdapter.notifyDataSetChanged();
		
		mActionMenu = new ActionMenu(getActivity().getApplicationContext());
		getActionMenuInflater().inflate(R.menu.app_menu, mActionMenu);

		startMenuBar(mBottomView);
		return true;
	}

	public String getAppVersion(String packageName, PackageManager pm) {
		String version = "";
		try {
			version = pm.getPackageInfo(packageName, 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return version;
	}
	
	public void destroyMenuBar() {
		destroyMenuBar(mBottomView);
		
		mActivity.updateTitleNum(-1, count);
		
		mGridAdapter.changeMode(ActionMenu.MODE_NORMAL);
		mGridAdapter.checkedAll(false);
		mGridAdapter.notifyDataSetChanged();
	}
	
	public boolean onBackPressed(){
		if (null != mGridAdapter && mGridAdapter.isMode(ActionMenu.MODE_EDIT)) {
			destroyMenuBar();
			return false;
		}
		return true;
	}

	@Override
	public void updateMenuBar() {
		int selectCount = mGridAdapter.getCheckedCount();
		mActivity.updateTitleNum(selectCount, count);
		
		ActionMenuItem selectItem = mActionMenu.findItem(R.id.menu_select);
		if (mGridAdapter.getCount() == selectCount) {
			selectItem.setTitle(R.string.unselect_all);
			selectItem.setEnableIcon(R.drawable.ic_aciton_unselect);
		} else {
			selectItem.setTitle(R.string.select_all);
			selectItem.setEnableIcon(R.drawable.ic_aciton_select);
		}
		
		if (0==selectCount) {
        	mActionMenu.findItem(R.id.menu_backup).setEnable(false);
        	mActionMenu.findItem(R.id.menu_uninstall).setEnable(false);
        	mActionMenu.findItem(R.id.menu_app_info).setEnable(false);
		} else if (1 == selectCount) {
        	mActionMenu.findItem(R.id.menu_backup).setEnable(true);
        	mActionMenu.findItem(R.id.menu_uninstall).setEnable(true);
        	mActionMenu.findItem(R.id.menu_app_info).setEnable(true);
		} else {
        	mActionMenu.findItem(R.id.menu_backup).setEnable(true);
        	mActionMenu.findItem(R.id.menu_uninstall).setEnable(true);
        	mActionMenu.findItem(R.id.menu_app_info).setEnable(false);
		}
		mMenuBarManager.refreshMenus(mActionMenu);
	}

	@Override
	public void doCheckAll() {
		int selectedCount = mGridAdapter.getCheckedCount();
		if (mGridAdapter.getCount() != selectedCount) {
			mGridAdapter.checkedAll(true);
		} else {
			mGridAdapter.checkedAll(false);
		}
		updateMenuBar();
		mGridAdapter.notifyDataSetChanged();
	}
	
	@Override
	public void onMenuItemClick(ActionMenuItem item) {
		// TODO Auto-generated method stub
		super.onMenuItemClick(item);
		switch (item.getItemId()) {
		case R.id.menu_backup:
			//wait for relize
			break;
		case R.id.menu_uninstall:
			mUninstallList = mGridAdapter.getCheckedPkgList();
			showUninstallDialog();
			uninstallApp();
			destroyMenuBar();
			break;
		case R.id.menu_app_info:
			String packageName = mGridAdapter.getCheckedPkgList().get(0);
			showInstalledAppDetails(packageName);
			destroyMenuBar();
			break;
		case R.id.menu_select:
			doCheckAll();
			break;

		default:
			break;
		}
	}
	
	public void showInstalledAppDetails(String packageName){
		Intent intent = new Intent();
		final int apiLevel = Build.VERSION.SDK_INT;
		if (apiLevel >= Build.VERSION_CODES.GINGERBREAD) {
			intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
			Uri uri = Uri.fromParts("package", packageName, null);
			intent.setData(uri);
		}else {
			intent.setAction(Intent.ACTION_VIEW);
			intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
			intent.putExtra("pkg", packageName);
		}
		startActivity(intent);
	}
	
	protected void showUninstallDialog(){
    	mAppDialog = new AppDialog(getActivity(), mUninstallList.size());
		mAppDialog.setDialogTitle("卸载应用");
		mAppDialog.setNegativeButton(R.string.cancel, new onZyDialogClickListener() {
			
			@Override
			public void onClick(Dialog dialog) {
				if (null != mUninstallList) {
					mUninstallList.clear();
					mUninstallList = null;
				}
				mAppDialog.dismiss();
			}
		});
		mAppDialog.show();
    }
	
	protected void uninstallApp(){
		if (mUninstallList.size() <= 0) {
			mUninstallList = null;
			
			if (null != mAppDialog) {
				mAppDialog.cancel();
				mAppDialog = null;
			}
			return;
		}
		String uninstallPkg = mUninstallList.get(0);
		mAppDialog.updateUI(mAppDialog.getMax() - mUninstallList.size() + 1, 
				AppUtil.getAppLabel(uninstallPkg, getActivity().getPackageManager()));
		Uri packageUri = Uri.parse("package:" + uninstallPkg);
		Intent deleteIntent = new Intent();
		deleteIntent.setAction(Intent.ACTION_DELETE);
		deleteIntent.setData(packageUri);
		startActivityForResult(deleteIntent, REQUEST_CODE_UNINSTALL);
		mUninstallList.remove(0);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (REQUEST_CODE_UNINSTALL == requestCode) {
			uninstallApp();
		}
	}

}
