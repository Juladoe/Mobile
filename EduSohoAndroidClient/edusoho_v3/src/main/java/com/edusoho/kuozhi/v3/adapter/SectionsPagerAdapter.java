package com.edusoho.kuozhi.v3.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.edusoho.kuozhi.v3.core.CoreEngine;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by suju on 17/2/7.
 */

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private String[] mTitleArray;
    private String[] mFragmentArray;
    private Context mContext;
    private Bundle mBundle;
    private List<Fragment> fragmentList;

    public SectionsPagerAdapter(FragmentManager fm, Context context, String[] titleArray, String[] fragmentArray, Bundle bundle) {
        super(fm);
        this.mBundle = bundle;
        this.mContext = context;
        this.mTitleArray = titleArray;
        this.mFragmentArray = fragmentArray;
        initData();
    }

    private void initData() {
        fragmentList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            fragmentList.add(CoreEngine.create(mContext).runPluginWithFragmentByBundle(mFragmentArray[i], mContext, mBundle));
        }
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        // Show 3 total pages.
        return mTitleArray.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitleArray[position];
    }
}
