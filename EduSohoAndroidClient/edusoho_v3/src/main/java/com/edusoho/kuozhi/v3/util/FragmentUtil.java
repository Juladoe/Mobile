package com.edusoho.kuozhi.v3.util;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.List;

public class FragmentUtil {

    private List<Fragment> mFragments = new ArrayList<>();
    private FragmentManager mFragmentManager;
    private int mViewId;

    public FragmentUtil(FragmentManager fragmentManager, int viewId) {
        this.mFragmentManager = fragmentManager;
        this.mViewId = viewId;
    }

    public void initFragments(List<Fragment> fragments) {
        clear();
        mFragments.clear();
        mFragments.addAll(fragments);
        init();
    }

    public void addFragment(Fragment fragment) {
        mFragments.add(fragment);
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        if (fragment != null) {
            transaction.add(mViewId, fragment);
            transaction.hide(fragment);
            transaction.commitAllowingStateLoss();
        }
    }

    public void show(int position) {
        if (mFragments.size() > position
                && mFragments.get(position) != null) {
            hideAll();
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            transaction.show(mFragments.get(position));
            transaction.commitAllowingStateLoss();
        }
    }

    public void hideAll() {
        for (Fragment fragment : mFragments) {
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            if (fragment != null) {
                transaction.hide(fragment);
                transaction.commitAllowingStateLoss();
            }
        }
    }

    private void clear() {
        for (Fragment fragment : mFragments) {
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            if (fragment != null) {
                transaction.remove(fragment);
                transaction.commitAllowingStateLoss();
            }
        }
    }

    private void init() {
        for (Fragment fragment : mFragments) {
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            if (fragment != null) {
                transaction.add(mViewId, fragment);
                transaction.hide(fragment);
                transaction.commitAllowingStateLoss();
            }
        }
        if (mFragments.size() > 0) {
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            transaction.show(mFragments.get(0));
        }
    }
}
