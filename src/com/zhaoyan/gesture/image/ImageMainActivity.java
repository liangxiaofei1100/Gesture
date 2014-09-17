package com.zhaoyan.gesture.image;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;

import com.zhaoyan.common.views.CustomViewPager;
import com.zhaoyan.common.views.TableTitleView;
import com.zhaoyan.common.views.TableTitleView.OnTableSelectChangeListener;
import com.zhaoyan.gesture.R;
import com.zhaoyan.gesture.activity.BaseFragmentActivity;

public class ImageMainActivity extends BaseFragmentActivity {
	private static final String TAG = ImageMainActivity.class.getSimpleName();

	private TableTitleView mTableTitleView;
	private CustomViewPager mViewPager;
	
	private List<Fragment> mFragmentList = new ArrayList<Fragment>();
	
	private int mCurrentPosition;
	
	public enum MediaType{
		Image, Video
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_main);
		
		initTitle("我的相册");
		
		mBaseIntroductionView.setVisibility(View.GONE);
		setTitleNumVisible(true);
		
		initView();
		
		//init fragments
		ImageFragment imageFragment = new ImageFragment();
		VideoFragment videoFragment = new VideoFragment();
		mFragmentList.add(imageFragment);
		mFragmentList.add(videoFragment);
		
		mViewPager = (CustomViewPager) findViewById(R.id.image_viewpager);
		mViewPager.setPagerSlideEnable(true);
		mViewPager.setAdapter(new ImagePagerAdapter(getSupportFragmentManager()));
		mViewPager.setCurrentItem(0);
		mViewPager.setOnPageChangeListener(new MyOnPageChangeListener());
		mTableTitleView.setSelectedPostion(0);
	}
	
	private class ImagePagerAdapter extends FragmentPagerAdapter{

		public ImagePagerAdapter(FragmentManager fm) {
			super(fm);
			// TODO Auto-generated constructor stub
		}

		@Override
		public Fragment getItem(int arg0) {
			// TODO Auto-generated method stub
			return mFragmentList.get(arg0);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mFragmentList.size();
		}
	}
	
	public class MyOnPageChangeListener implements OnPageChangeListener{  
          
        @Override  
        public void onPageScrolled(int arg0, float arg1, int arg2) {  
            // TODO Auto-generated method stub  
        }  
          
        @Override  
        public void onPageScrollStateChanged(int arg0) {  
            // TODO Auto-generated method stub  
        }  
          
        @Override  
        public void onPageSelected(int arg0) {  
            // TODO Auto-generated method stub  
        	mTableTitleView.setSelectedPostion(arg0);
        }  
    }  

	private void initView() {
		mTableTitleView = (TableTitleView) findViewById(R.id.ttv_sc_title);
		mTableTitleView.initTitles(new String[] { getString(R.string.gallery), getString(R.string.video) });
		mTableTitleView.setOnTableSelectChangeListener(myOnClickListener);
	}

	public void updateItemTitle(MediaType type, int selected, int count) {
		String title = "";
		int position = 0;
		switch (type) {
		case Image:
			title = getString(R.string.gallery);
			position = 0;
			break;
		case Video:
			title = getString(R.string.video);
			position = 1;
			break;
		default:
			break;
		}
		if (selected == -1) {
			title += getString(R.string.num_format, count);
		} else {
			title += getString(R.string.num_format2, selected, count);
		}
		mTableTitleView.setTableTitle(position, title);
	}

	private OnTableSelectChangeListener myOnClickListener = new TableTitleView.OnTableSelectChangeListener() {
		@Override
		public void onTableSelect(int position) {
			mViewPager.setCurrentItem(position);
		}
	};
	
	public boolean onBackKeyPressed() {
		switch (mViewPager.getCurrentItem()) {
		case 0:
			ImageFragment imageFragment = (ImageFragment) mFragmentList.get(0);
			return imageFragment.onBackPressed();
		case 1:
			VideoFragment videoFragment = (VideoFragment) mFragmentList.get(1);
			return videoFragment.onBackPressed();
		}
		return super.onBackKeyPressed();
	};

}
