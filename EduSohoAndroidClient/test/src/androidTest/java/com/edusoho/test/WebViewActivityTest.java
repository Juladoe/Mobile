package com.edusoho.test;

import android.content.Intent;
import android.test.UiThreadTest;
import android.view.ContextThemeWrapper;

import com.edusoho.kuozhi.v3.ui.WebViewActivity;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.test.base.BaseActivityUnitTestCase;
import com.edusoho.test.utils.TestUtils;

/**
 * Created by JesseHuang on 15/8/24.
 */
public class WebViewActivityTest extends BaseActivityUnitTestCase<WebViewActivity> {

    private WebViewActivity mActivity;

    public WebViewActivityTest() {
        super(WebViewActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mInstrumentation = getInstrumentation();
        ContextThemeWrapper context = new ContextThemeWrapper(
                mInstrumentation.getTargetContext(), R.style.EdusohoAppTheme);
        setActivityContext(context);

        mApp = (TestEduSohoApp) mInstrumentation.newApplication(
                getClass().getClassLoader(), TestEduSohoApp.class.getName(), context);
        TestUtils.initApplication(mApp, mInstrumentation.getTargetContext());
        mInstrumentation.callApplicationOnCreate(mApp);
        setApplication(mApp);
        mLaunchIntent = new Intent(mInstrumentation.getTargetContext(),
                WebViewActivity.class);
    }

    public WebViewActivity getActivity(String url) {
        mLaunchIntent.putExtra(Const.WEB_URL, url);
        if (mActivity == null) {
            mActivity = startActivity(mLaunchIntent, null, null);
        }
        return mActivity;
    }

    @UiThreadTest
    public void testMyLearnWebView() {
        String url = String.format(Const.MOBILE_APP_URL, mApp.schoolHost, Const.MY_LEARN);
        mActivity = getActivity(url);
        assertNotNull(mActivity);
    }

    @UiThreadTest
    public void testVipListWebView() {
        String url = String.format(Const.MOBILE_APP_URL, mApp.schoolHost, Const.VIP_LIST);
        mActivity = getActivity(url);
        assertNotNull(mActivity);
    }

    @UiThreadTest
    public void testMyFavoriteWebView() {
        String url = String.format(Const.MOBILE_APP_URL, mApp.schoolHost, Const.MY_FAVORITE);
        mActivity = getActivity(url);
        assertNotNull(mActivity);
    }
}
