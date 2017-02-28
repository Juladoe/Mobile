package com.edusoho.test;

import android.content.Intent;
import android.test.UiThreadTest;
import android.webkit.WebView;

import com.edusoho.kuozhi.v3.ui.AboutSchool;
import com.edusoho.test.base.BaseActivityUnitTestCase;

/**
 * Created by JesseHuang on 15/8/24.
 */
public class AboutSchoolTest extends BaseActivityUnitTestCase<AboutSchool> {
    public AboutSchoolTest() {
        super(AboutSchool.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mLaunchIntent = new Intent(mInstrumentation.getTargetContext(),
                AboutSchool.class);
    }

    @UiThreadTest
    public void testAboutSchool() {
        AboutSchool mActivity = getActivity();
        assertNotNull(mActivity);
    }

    @UiThreadTest
    public void testAboutSchoolLayout() {
        AboutSchool mActivity = getActivity();
        WebView webView = (WebView) mActivity.findViewById(R.id.webView);
        assertNotNull(webView);
    }
}
