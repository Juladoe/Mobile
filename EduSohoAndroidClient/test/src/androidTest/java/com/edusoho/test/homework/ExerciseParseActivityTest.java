package com.edusoho.test.homework;

import android.content.Intent;
import android.test.UiThreadTest;

import com.edusoho.kuozhi.homework.ExerciseParseActivity;
import com.edusoho.test.base.BaseActivityUnitTestCase;

/**
 * Created by Melomelon on 2015/11/12.
 */
public class ExerciseParseActivityTest extends BaseActivityUnitTestCase<ExerciseParseActivity> {

    public ExerciseParseActivityTest() {
        super(ExerciseParseActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mLaunchIntent = new Intent(mInstrumentation.getTargetContext(),
                ExerciseParseActivity.class);
    }

    @UiThreadTest
    public void testGetActivity() {
        ExerciseParseActivity mActivity = getActivity();
        assertNotNull(mActivity);
    }
}
