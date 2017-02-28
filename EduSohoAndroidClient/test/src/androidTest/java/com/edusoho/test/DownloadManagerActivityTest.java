package com.edusoho.test;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.test.UiThreadTest;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.edusoho.kuozhi.v3.ui.DownloadManagerActivity;
import com.edusoho.kuozhi.v3.ui.fragment.DownloadedFragment;
import com.edusoho.kuozhi.v3.ui.fragment.DownloadingFragment;
import com.edusoho.test.base.BaseActivityUnitTestCase;

import extensions.PagerSlidingTabStrip;

/**
 * Created by JesseHuang on 15/8/24.
 */
public class DownloadManagerActivityTest extends BaseActivityUnitTestCase<DownloadManagerActivity> {
    public DownloadManagerActivityTest() {
        super(DownloadManagerActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mLaunchIntent = new Intent(mInstrumentation.getTargetContext(),
                DownloadManagerActivity.class);
    }

    @UiThreadTest
    public void testDownloadManagerActivity() {
        DownloadManagerActivity mActivity = getActivity();
        assertNotNull(mActivity);
    }

    public Fragment getFragment(String fragmentName) {
        Fragment fragment = null;
        try {
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragment = Fragment.instantiate(getActivity(), fragmentName);
            fragment.setArguments(mLaunchIntent.getExtras());
            fragmentTransaction.replace(android.R.id.content, fragment);
            fragmentTransaction.commit();
            mInstrumentation.callActivityOnStart(getActivity());
            mInstrumentation.callActivityOnResume(getActivity());
        } catch (Exception ex) {
            Log.d("getFragment", ex.toString());
        }
        return fragment;
    }


    @UiThreadTest
    public void testDownloadManagerActivityLayout() {
        DownloadManagerActivity mActivity = getActivity();
        PagerSlidingTabStrip mPagerTab = (PagerSlidingTabStrip) mActivity.findViewById(R.id.tab_download);
        assertNotNull(mPagerTab);

        ViewPager mViewPagers = (ViewPager) mActivity.findViewById(R.id.viewpager_download);
        assertNotNull(mViewPagers);
        assertEquals(2, mViewPagers.getAdapter().getCount());

        int initCurrentItem = mViewPagers.getCurrentItem();
        DownloadManagerActivity.MyPagerAdapter viewPagerAdapter = (DownloadManagerActivity.MyPagerAdapter) mViewPagers.getAdapter();
        assertEquals(0, initCurrentItem);
        assertEquals(DownloadManagerActivity.DOWNLOAD_TITLES[initCurrentItem], viewPagerAdapter.getPageTitle(initCurrentItem));

        mViewPagers.setCurrentItem(1);

        int currentItem = mViewPagers.getCurrentItem();
        assertEquals(1, currentItem);
        assertEquals(DownloadManagerActivity.DOWNLOAD_TITLES[currentItem], viewPagerAdapter.getPageTitle(currentItem));

        TextView mDeviceSpaceInfo = (TextView) mActivity.findViewById(R.id.download_device_info);
        assertNotNull(mDeviceSpaceInfo);
        ProgressBar pbDownloadDeviceInfo = (ProgressBar) mActivity.findViewById(R.id.pb_download_device_info);
        assertNotNull(pbDownloadDeviceInfo);

        ActionBar actionBar = mActivity.getSupportActionBar();
        assertNotNull(actionBar);
    }

    @UiThreadTest
    public void testDownloadedFragment() {
        DownloadedFragment mFragment = (DownloadedFragment) getFragment(DownloadedFragment.class.getName());
        assertNotNull(mFragment);
    }

    @UiThreadTest
    public void testDownloadingFragment() {
        DownloadingFragment mFragment = (DownloadingFragment) getFragment(DownloadingFragment.class.getName());
        assertNotNull(mFragment);
    }

    @UiThreadTest
    public void testDownloadedFragmentLayout() {
        DownloadManagerActivity mActivity = getActivity();
        DownloadedFragment mFragment = (DownloadedFragment) getFragment(DownloadedFragment.class.getName());
        ActionBar actionBar = mActivity.getSupportActionBar();
        assertNotNull(actionBar);

        View view = mFragment.getView();

        View mToolsLayout = view.findViewById(R.id.download_tools_layout);
        assertNotNull(mToolsLayout);
        assertEquals(0, mToolsLayout.getHeight());
        TextView mSelectAllBtn = (TextView) view.findViewById(R.id.tv_select_all);
        assertNotNull(mSelectAllBtn);
        TextView mDelBtn = (TextView) view.findViewById(R.id.tv_delete);
        assertNotNull(mDelBtn);
        ExpandableListView mListView = (ExpandableListView) view.findViewById(R.id.el_downloaded);
        assertNotNull(mListView);
    }

}
