package com.zhaoyan.gesture.activity;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.AsyncTaskLoader;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;
import android.widget.Toast;

import com.zhaoyan.gesture.R;
import com.zhaoyan.gesture.util.Utils;

/**
 * Demonstration of the implementation of a custom Loader.
 */
public class AppLauncherActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_main);
		
		
		Bundle bundle = getIntent().getExtras();
		if (bundle == null) {
			initTitle(R.string.main_app_manager);
		} else {
			initTitle(bundle.getString("title"));
		}

		FragmentManager fm = getFragmentManager();

		// Create the list fragment and add it as our sole content.
		if (fm.findFragmentById(R.id.fl_app_main) == null) {
			AppListFragment list = new AppListFragment();
			list.setArguments(bundle);
			fm.beginTransaction().add(R.id.fl_app_main, list).commit();
		}
	}

	/**
	 * This class holds the per-item data in our Loader.
	 */
	public static class AppEntry {
		private final AppListLoader mLoader;
		private final ApplicationInfo mInfo;
		private final File mApkFile;
		private String mLabel;
		private Drawable mIcon;
		private boolean mMounted;
		
		public AppEntry(AppListLoader loader, ApplicationInfo info) {
			mLoader = loader;
			mInfo = info;
			mApkFile = new File(info.sourceDir);
		}

		public ApplicationInfo getApplicationInfo() {
			return mInfo;
		}

		public String getLabel() {
			return mLabel;
		}
		
		public String getSizeStr(){
			return Utils.getFormatSize(mApkFile.length());
		}
		
//		public String getVersion(){
////			String version = "";
////			try {
////				version = pm.getPackageInfo(packagename, 0).versionName;
////			} catch (NameNotFoundException e) {
////				e.printStackTrace();
////			}
//		}

		public Drawable getIcon() {
			if (mIcon == null) {
				if (mApkFile.exists()) {
					mIcon = mInfo.loadIcon(mLoader.mPm);
					return mIcon;
				} else {
					mMounted = false;
				}
			} else if (!mMounted) {
				// If the app wasn't mounted but is now mounted, reload
				// its icon.
				if (mApkFile.exists()) {
					mMounted = true;
					mIcon = mInfo.loadIcon(mLoader.mPm);
					return mIcon;
				}
			} else {
				return mIcon;
			}

			return mLoader.getContext().getResources()
					.getDrawable(android.R.drawable.sym_def_app_icon);
		}

		@Override
		public String toString() {
			return mLabel;
		}

		void loadLabel(Context context) {
			if (mLabel == null || !mMounted) {
				if (!mApkFile.exists()) {
					mMounted = false;
					mLabel = mInfo.packageName;
				} else {
					mMounted = true;
					CharSequence label = mInfo.loadLabel(context
							.getPackageManager());
					mLabel = label != null ? label.toString()
							: mInfo.packageName;
				}
			}
		}
	}

	/**
	 * Perform alphabetical comparison of application entry objects.
	 */
	public static final Comparator<AppEntry> ALPHA_COMPARATOR = new Comparator<AppEntry>() {
		private final Collator sCollator = Collator.getInstance();

		@Override
		public int compare(AppEntry object1, AppEntry object2) {
			return sCollator.compare(object1.getLabel(), object2.getLabel());
		}
	};

	/**
	 * Helper for determining if the configuration has changed in an interesting
	 * way so we need to rebuild the app list.
	 */
	public static class InterestingConfigChanges {
		final Configuration mLastConfiguration = new Configuration();
		int mLastDensity;

		boolean applyNewConfig(Resources res) {
			int configChanges = mLastConfiguration.updateFrom(res
					.getConfiguration());
			boolean densityChanged = mLastDensity != res.getDisplayMetrics().densityDpi;
			if (densityChanged
					|| (configChanges & (ActivityInfo.CONFIG_LOCALE
							| ActivityInfo.CONFIG_UI_MODE | ActivityInfo.CONFIG_SCREEN_LAYOUT)) != 0) {
				mLastDensity = res.getDisplayMetrics().densityDpi;
				return true;
			}
			return false;
		}
	}

	/**
	 * Helper class to look for interesting changes to the installed apps so
	 * that the loader can be updated.
	 */
	public static class PackageIntentReceiver extends BroadcastReceiver {
		final AppListLoader mLoader;

		public PackageIntentReceiver(AppListLoader loader) {
			mLoader = loader;
			IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
			filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
			filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
			filter.addDataScheme("package");
			mLoader.getContext().registerReceiver(this, filter);
			// Register for events related to sdcard installation.
			IntentFilter sdFilter = new IntentFilter();
			sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
			sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
			mLoader.getContext().registerReceiver(this, sdFilter);
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			// Tell the loader about the change.
			mLoader.onContentChanged();
		}
	}

	/**
	 * A custom Loader that loads all of the installed applications.
	 */
	public static class AppListLoader extends AsyncTaskLoader<List<AppEntry>> {
		final InterestingConfigChanges mLastConfig = new InterestingConfigChanges();
		final PackageManager mPm;

		List<AppEntry> mApps;
		PackageIntentReceiver mPackageObserver;
		
		boolean mIsSelectMode = false;

		public AppListLoader(Context context) {
			super(context);

			// Retrieve the package manager for later use; note we don't
			// use 'context' directly but instead the save global application
			// context returned by getContext().
			mPm = getContext().getPackageManager();
		}

		/**
		 * This is where the bulk of our work is done. This function is called
		 * in a background thread and should generate a new set of data to be
		 * published by the loader.
		 */
		@Override
		public List<AppEntry> loadInBackground() {
			// Retrieve all known applications.
			List<ApplicationInfo> apps = mPm
					.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES
							| PackageManager.GET_DISABLED_COMPONENTS);
			if (apps == null) {
				apps = new ArrayList<ApplicationInfo>();
			}

			List<ApplicationInfo> threeApps = new ArrayList<ApplicationInfo>();
			for (ApplicationInfo applicationInfo : apps) {
				// not system apps
				if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
					threeApps.add(applicationInfo);
				}
				//was system apps.but user update it manual,the app to be user app
				else if ((applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
					threeApps.add(applicationInfo);
				}
			}

			final Context context = getContext();

			// Create corresponding array of entries and load their labels.
			List<AppEntry> entries = new ArrayList<AppEntry>(threeApps.size());
			for (int i = 0; i < threeApps.size(); i++) {
				AppEntry entry = new AppEntry(this, threeApps.get(i));
				entry.loadLabel(context);
				entries.add(entry);
			}

			// Sort the list.
			Collections.sort(entries, ALPHA_COMPARATOR);

			// Done!
			return entries;
		}

		/**
		 * Called when there is new data to deliver to the client. The super
		 * class will take care of delivering it; the implementation here just
		 * adds a little more logic.
		 */
		@Override
		public void deliverResult(List<AppEntry> apps) {
			if (isReset()) {
				// An async query came in while the loader is stopped. We
				// don't need the result.
				if (apps != null) {
					onReleaseResources(apps);
				}
			}
			List<AppEntry> oldApps = apps;
			mApps = apps;

			if (isStarted()) {
				// If the Loader is currently started, we can immediately
				// deliver its results.
				super.deliverResult(apps);
			}

			// At this point we can release the resources associated with
			// 'oldApps' if needed; now that the new result is delivered we
			// know that it is no longer in use.
			if (oldApps != null) {
				onReleaseResources(oldApps);
			}
		}

		/**
		 * Handles a request to start the Loader.
		 */
		@Override
		protected void onStartLoading() {
			if (mApps != null) {
				// If we currently have a result available, deliver it
				// immediately.
				deliverResult(mApps);
			}

			// Start watching for changes in the app data.
			if (mPackageObserver == null) {
				mPackageObserver = new PackageIntentReceiver(this);
			}

			// Has something interesting in the configuration changed since we
			// last built the app list?
			boolean configChange = mLastConfig.applyNewConfig(getContext()
					.getResources());

			if (takeContentChanged() || mApps == null || configChange) {
				// If the data has changed since the last time it was loaded
				// or is not currently available, start a load.
				forceLoad();
			}
		}

		/**
		 * Handles a request to stop the Loader.
		 */
		@Override
		protected void onStopLoading() {
			// Attempt to cancel the current load task if possible.
			cancelLoad();
		}

		/**
		 * Handles a request to cancel a load.
		 */
		@Override
		public void onCanceled(List<AppEntry> apps) {
			super.onCanceled(apps);

			// At this point we can release the resources associated with 'apps'
			// if needed.
			onReleaseResources(apps);
		}

		/**
		 * Handles a request to completely reset the Loader.
		 */
		@Override
		protected void onReset() {
			super.onReset();

			// Ensure the loader is stopped
			onStopLoading();

			// At this point we can release the resources associated with 'apps'
			// if needed.
			if (mApps != null) {
				onReleaseResources(mApps);
				mApps = null;
			}

			// Stop monitoring for changes.
			if (mPackageObserver != null) {
				getContext().unregisterReceiver(mPackageObserver);
				mPackageObserver = null;
			}
		}

		/**
		 * Helper function to take care of releasing resources associated with
		 * an actively loaded data set.
		 */
		protected void onReleaseResources(List<AppEntry> apps) {
			// For a simple List<> there is nothing to do. For something
			// like a Cursor, we would close it here.
		}
	}

	public static class AppListAdapter extends ArrayAdapter<AppEntry> {
		private final LayoutInflater mInflater;
		private int mCurrentSelectPosition = -1;
		private boolean mIsSelectMode = true;

		public AppListAdapter(Context context) {
			super(context, android.R.layout.simple_list_item_2);
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public void setData(List<AppEntry> data) {
			clear();
			if (data != null) {
				addAll(data);
			}
		}
		
		public void setSelect(int position){
			mCurrentSelectPosition = position;
			notifyDataSetChanged();
		}
		
		public AppEntry getSelectEntry(){
			return getItem(mCurrentSelectPosition);
		}

		/**
		 * Populate new items in the list.
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;

			if (convertView == null) {
				view = mInflater.inflate(R.layout.app_list_item, parent, false);
			} else {
				view = convertView;
			}

			AppEntry item = getItem(position);
			((ImageView) view.findViewById(R.id.iv_app_logo)).setImageDrawable(item
					.getIcon());
			((TextView) view.findViewById(R.id.tv_app_label)).setText(item.getLabel());
			((TextView) view.findViewById(R.id.tv_app_info)).setText(item.getSizeStr());
			
//			if (mIsSelectMode) {
				if (mCurrentSelectPosition == position) {
					view.setBackgroundColor(Color.argb(0xff, 0x33, 0xb5, 0xe5));
				} else {
					view.setBackgroundColor(Color.TRANSPARENT);
				}
//			}

			return view;
		}
	}

	public static class AppListFragment extends ListFragment implements
			OnQueryTextListener, LoaderManager.LoaderCallbacks<List<AppEntry>>,
			OnItemLongClickListener, android.view.View.OnClickListener, OnItemClickListener {

		// This is the Adapter being used to display the list's data.
		AppListAdapter mAdapter;

		// If non-null, this is the current filter the user has provided.
		String mCurFilter;
		
		ProgressBar mLoadingBar;
		Button mCancelBtn, mOkBtn;
		
		View mBottomView;
		
		boolean mIsSelectMode = false;
		
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
			// TODO Auto-generated method stub
			View rootView = inflater.inflate(R.layout.app_list_main, null);
			
			mLoadingBar = (ProgressBar) rootView.findViewById(R.id.bar_loading);
			mCancelBtn = (Button) rootView.findViewById(R.id.btn_cancel);
			mOkBtn = (Button) rootView.findViewById(R.id.btn_ok);
			mCancelBtn.setOnClickListener(this);
			mOkBtn.setOnClickListener(this);
			mOkBtn.setEnabled(false);
			
			mBottomView = rootView.findViewById(R.id.button_layout);
			
			return rootView;
		}
		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_cancel:
				getActivity().finish();
				break;
			case R.id.btn_ok:
				AppEntry appEntry = mAdapter.getSelectEntry();
				Intent intent = new Intent();
				intent.setPackage(appEntry.getApplicationInfo().packageName);
				getActivity().setResult(RESULT_OK, intent);
				getActivity().finish();
				break;
			default:
				break;
			}
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);

			// Give some text to display if there is no data. In a real
			// application this would come from a resource.
//			setEmptyText("No applications");

			// We have a menu item to show in action bar.
			setHasOptionsMenu(true);
			
			if (mIsSelectMode) {
				mBottomView.setVisibility(View.VISIBLE);
			} else {
				mBottomView.setVisibility(View.GONE);
				getListView().setOnItemLongClickListener(this);
			}

			// Create an empty adapter we will use to display the loaded data.
			mAdapter = new AppListAdapter(getActivity());
			setListAdapter(mAdapter);

			// Start out with a progress indicator.
//			setListShown(false);
			getListView().setOnItemClickListener(this);

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
			sv.setOnQueryTextListener(this);
			item.setActionView(sv);
		}

		@Override
		public boolean onQueryTextChange(String newText) {
			// Called when the action bar search text has changed. Since this
			// is a simple array adapter, we can just have it do the filtering.
			mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
			mAdapter.getFilter().filter(mCurFilter);
			return true;
		}

		@Override
		public boolean onQueryTextSubmit(String query) {
			// Don't care about this.
			return true;
		}

		@Override
		public void onListItemClick(ListView l, View v, int position, long id) {
			// Insert desired behavior here.
			Log.i("LoaderCustom", "Item clicked: " + id);
		}

		@Override
		public Loader<List<AppEntry>> onCreateLoader(int id, Bundle args) {
			// This is called when a new Loader needs to be created. This
			// sample only has one Loader with no arguments, so it is simple.
			return new AppListLoader(getActivity());
		}

		@Override
		public void onLoadFinished(Loader<List<AppEntry>> loader,
				List<AppEntry> data) {
			// Set the new data in the adapter.
			mAdapter.setData(data);

			mLoadingBar.setVisibility(View.GONE);
			// The list should now be shown.
//			if (isResumed()) {
//				setListShown(true);
//			} else {
//				setListShownNoAnimation(true);
//			}
		}

		@Override
		public void onLoaderReset(Loader<List<AppEntry>> loader) {
			// Clear the data in the adapter.
			mAdapter.setData(null);
		}
		
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			if (mIsSelectMode) {
				mAdapter.setSelect(position);
				mOkBtn.setEnabled(true);
			}
		}

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			// TODO Auto-generated method stub
			final AppEntry appEntry = mAdapter.getItem(position);
			CharSequence[] items = { "Open", "Backup" };
			new AlertDialog.Builder(getActivity()).setTitle("Menu")
					.setItems(items, new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							switch (which) {
							case 0:
								// open
								Intent intent = getActivity()
										.getPackageManager()
										.getLaunchIntentForPackage(
												appEntry.mInfo.packageName);
								if (intent != null) {
									startActivity(intent);
								} else {
									Toast.makeText(getActivity(),
											"Cannot open", Toast.LENGTH_SHORT)
											.show();
								}
								break;
							case 1:
								final String srcpath = appEntry.mApkFile
										.getAbsolutePath();
								// backup
								String defaultBackupPath = Environment
										.getExternalStorageDirectory()
										.getAbsolutePath()
										+ "/Urey";
								File dirFile = new File(defaultBackupPath);
								if (!dirFile.exists()) {
									dirFile.mkdirs();
								}
								final String despath = defaultBackupPath
										+ "/"
										+ appEntry.getLabel()
										+ "_"
										+ getAppVersion(
												appEntry.mInfo.packageName,
												getActivity()
														.getPackageManager())
										+ ".apk";
								new AsyncTask<Void, Void, Void>() {
									ProgressDialog dialog = null;

									protected void onPreExecute() {
										dialog = new ProgressDialog(
												getActivity());
										dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
										dialog.setMessage("Backuping...");
										dialog.setCancelable(false);
										dialog.show();
									};

									@Override
									protected Void doInBackground(
											Void... params) {
										// TODO Auto-generated method stub
										// try {
										// WardUtils.fileStreamCopy(srcpath,
										// despath);
										// } catch (IOException e) {
										// // TODO Auto-generated catch block
										// e.printStackTrace();
										// }
										return null;
									}

									protected void onPostExecute(Void result) {
										if (null != dialog) {
											dialog.cancel();
										}
									};

								}.execute();
								break;

							default:
								System.out.println("default:" + which);
								break;
							}
						}
					}).create().show();
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
	}

}
