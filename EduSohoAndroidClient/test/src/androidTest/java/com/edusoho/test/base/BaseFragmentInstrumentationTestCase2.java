package com.edusoho.test.base;

import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;
import android.test.ActivityInstrumentationTestCase2;

import com.edusoho.kuozhi.v3.ui.base.BaseFragment;
import com.edusoho.test.FragmentTestActivity;

/**
 * Created by howzhi on 15/8/24.
 */
public class BaseFragmentInstrumentationTestCase2<K extends BaseFragment> extends ActivityInstrumentationTestCase2<FragmentTestActivity> {

    protected Bundle mLaunchBundle;
    protected Instrumentation mInstrumentation;
    protected K mFragment;
    private Class<K> mFragmentClass;

    public BaseFragmentInstrumentationTestCase2(Class<K> fragmentClass)
    {
        super(FragmentTestActivity.class);
        mFragmentClass = fragmentClass;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(true);
        mInstrumentation = getInstrumentation();
    }

    public K getFragment() {
        if (mFragment == null) {
            FragmentTestActivity activity = super.getActivity();
            mFragment = (K) activity.loadFragment(mFragmentClass.getName(), mLaunchBundle);
        }
        return mFragment;
    }
}
