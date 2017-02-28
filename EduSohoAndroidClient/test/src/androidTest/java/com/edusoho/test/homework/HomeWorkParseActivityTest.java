package com.edusoho.test.homework;

import android.content.Intent;
import android.test.UiThreadTest;

import com.edusoho.kuozhi.homework.HomeWorkParseActivity;
import com.edusoho.test.base.BaseActivityUnitTestCase;

/**
 * Created by Melomelon on 2015/11/12.
 */
public class HomeWorkParseActivityTest extends BaseActivityUnitTestCase<HomeWorkParseActivity> {

    public HomeWorkParseActivityTest() {
        super(HomeWorkParseActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mLaunchIntent = new Intent(mInstrumentation.getTargetContext(),
                HomeWorkParseActivity.class);
    }

    @UiThreadTest
    public void testGetActivity() {
        HomeWorkParseActivity mActivity = getActivity();
        assertNotNull(mActivity);
    }


}
