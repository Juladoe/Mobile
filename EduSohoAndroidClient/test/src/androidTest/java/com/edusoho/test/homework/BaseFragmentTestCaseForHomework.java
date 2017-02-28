package com.edusoho.test.homework;

import android.content.Intent;

import com.edusoho.kuozhi.v3.ui.base.BaseFragment;
import com.edusoho.test.FragmentTestActivity;
import com.edusoho.test.base.BaseActivityNoActionBarUnitTestCase;
import com.edusoho.test.base.BaseFragmentTestCase;

/**
 * Created by Melomelon on 2015/11/17.
 */
public class BaseFragmentTestCaseForHomework<K extends BaseFragment> extends BaseActivityNoActionBarUnitTestCase<HomeworkQuestionFragmentTestActivity> {

    protected K mFragment;
    private Class<K> mFragmentClass;

    public BaseFragmentTestCaseForHomework(Class<K> activityClass) {
        super(HomeworkQuestionFragmentTestActivity.class);
        mFragmentClass = activityClass;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mLaunchIntent = new Intent(mInstrumentation.getTargetContext(),
                FragmentTestActivity.class);
    }

    public K getFragment() {
        if (mFragment == null) {
            HomeworkQuestionFragmentTestActivity activity = super.getActivity();
            mFragment = (K) activity.loadFragment(mFragmentClass.getName(), mLaunchIntent.getExtras());

            mInstrumentation.callActivityOnStart(getActivity());
            mInstrumentation.callActivityOnResume(getActivity());
        }
        return mFragment;
    }
}
