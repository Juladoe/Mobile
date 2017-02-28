package com.edusoho.videoplayer;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;

import com.edusoho.kuozhi.v3.ui.FragmentPageActivity;

/**
 * Created by suju on 16/12/12.
 */

public class CourseCatalogFragmentTest extends ActivityInstrumentationTestCase2<FragmentPageActivity> {

    public CourseCatalogFragmentTest() {
        super(FragmentPageActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        Intent intent = new Intent();
        intent.putExtra(FragmentPageActivity.FRAGMENT, "CourseCatalogFragment");
        setActivityIntent(intent);
        super.setUp();
    }

    @UiThreadTest
    public void testLoadCourseCatalogFragment() throws Exception {

    }
}
