package com.edusoho.test.base;

import android.content.Intent;
import com.edusoho.kuozhi.v3.ui.base.BaseFragment;
import com.edusoho.test.FragmentTestActivity;

/**
 * Created by howzhi on 15/8/17.
 */
public class BaseFragmentTestCase<K extends BaseFragment> extends BaseActivityNoActionBarUnitTestCase<FragmentTestActivity> {

    protected K mFragment;
    private Class<K> mFragmentClass;

    public BaseFragmentTestCase(Class<K> fragmentClass)
    {
        super(FragmentTestActivity.class);
        mFragmentClass = fragmentClass;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mLaunchIntent = new Intent(mInstrumentation.getTargetContext(),
                FragmentTestActivity.class);
    }

    public K getFragment() {
        if (mFragment == null) {
            FragmentTestActivity activity = super.getActivity();
            mFragment = (K) activity.loadFragment(mFragmentClass.getName(), mLaunchIntent.getExtras());

            mInstrumentation.callActivityOnStart(getActivity());
            mInstrumentation.callActivityOnResume(getActivity());
        }
        return mFragment;
    }
}
