package com.edusoho.test.homework;

import android.content.Intent;
import android.test.UiThreadTest;

import com.edusoho.kuozhi.homework.HomeworkActivity;
import com.edusoho.test.base.BaseActivityUnitTestCase;

/**
 * Created by Melomelon on 2015/11/12.
 */
public class HomeworkActivityTest extends BaseActivityUnitTestCase<HomeworkActivity> {

    public HomeworkActivityTest() {
        super(HomeworkActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mLaunchIntent = new Intent(mInstrumentation.getTargetContext(),
                HomeworkActivity.class);
    }

    @UiThreadTest
    public void testGetActivity() {
        HomeworkActivity mActivity = getActivity();
        assertNotNull(mActivity);
    }

}
