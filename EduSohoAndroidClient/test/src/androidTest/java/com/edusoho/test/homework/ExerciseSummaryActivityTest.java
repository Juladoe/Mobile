package com.edusoho.test.homework;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.test.UiThreadTest;

import com.edusoho.kuozhi.homework.ExerciseSummaryActivity;
import com.edusoho.kuozhi.homework.ui.fragment.HomeWorkSummaryFragment;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.test.base.BaseActivityUnitTestCase;

/**
 * Created by Melomelon on 2015/11/12.
 */
public class ExerciseSummaryActivityTest extends BaseActivityUnitTestCase<ExerciseSummaryActivity> {

    public ExerciseSummaryActivityTest() {
        super(ExerciseSummaryActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mLaunchIntent = new Intent(mInstrumentation.getTargetContext(), ExerciseSummaryActivity.class);
    }

    @UiThreadTest
    public void testGetActivity() {
        ExerciseSummaryActivity mActivity = getActivity();
        assertNotNull(mActivity);
    }

    @UiThreadTest
    public void testGetFragment(){
        HomeWorkSummaryFragment fragment = (HomeWorkSummaryFragment) getFragment(HomeWorkSummaryFragment.class.getName());
        assertNotNull(fragment);
    }

    private Fragment getFragment(String fragmentName){
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        Fragment fragment = Fragment.instantiate(getActivity(), fragmentName);
        mLaunchIntent.putExtra(Const.LESSON_ID,0);
        fragment.setArguments(mLaunchIntent.getExtras());
        fragmentTransaction.replace(android.R.id.content, fragment);
        fragmentTransaction.commit();

        return fragment;
    }
}
