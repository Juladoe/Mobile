package com.edusoho.test;

import android.support.v4.app.Fragment;
import android.test.UiThreadTest;
import android.test.ViewAsserts;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.edusoho.kuozhi.v3.ui.fragment.FindFragment;
import com.edusoho.kuozhi.v3.view.webview.ESWebView;
import com.edusoho.kuozhi.v3.view.webview.bridgeadapter.AbstractJsBridgeAdapterWebView;
import com.edusoho.test.base.BaseFragmentTestCase;

/**
 * Created by howzhi on 15/8/17.
 */

public class FindFragmentTest extends BaseFragmentTestCase<FindFragment> {

    public FindFragmentTest() {
        super(FindFragment.class);
    }

    @UiThreadTest
    public void testGetFragment() {
        Fragment mFragment = getFragment();
        assertNotNull(mFragment);
    }

    @UiThreadTest
    public void testInitWebView() {
        Fragment mFragment = getFragment();
        ESWebView webView = getWebView(mFragment);

        assertEquals("main", webView.getAppCode());
        assertNotNull(webView.getWebView());
    }


    private ESWebView getWebView(Fragment mFragment) {
        View parentView = mFragment.getView();
        assertNotNull(parentView);
        ESWebView webView = (ESWebView) mFragment.getView().findViewById(R.id.webView);

        return webView;
    }

    @UiThreadTest
    public void testWebViewPosition() {
        Fragment mFragment = getFragment();
        ESWebView webView = getWebView(mFragment);

        AbstractJsBridgeAdapterWebView cordovaWebView = webView.getWebView();
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) cordovaWebView.getLayoutParams();
        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, layoutParams.width);
        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, layoutParams.height);
    }

    @UiThreadTest
    public void testWebViewLoadingIsHide() {
        Fragment mFragment = getFragment();
        ESWebView webView = getWebView(mFragment);

        View pbLoading = webView.findViewById(R.id.pb_loading);

        View windowView = getActivity().getWindow().getDecorView();
        ViewAsserts.assertOnScreen(windowView, pbLoading);
        assertTrue(pbLoading.getVisibility() == View.VISIBLE);
    }
}
