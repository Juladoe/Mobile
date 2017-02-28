package com.edusoho.kuozhi.v3.ui.fragment;

import android.os.Bundle;
import android.view.View;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.ui.base.BaseFragment;
import com.edusoho.kuozhi.v3.view.webview.ESWebView;

/**
 * Created by howzhi on 15/7/16.
 */
public class ESWebViewFragment extends BaseFragment {
    private static final String TAG = "ESWebViewFragment";
    protected ESWebView mWebView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContainerView(R.layout.fragment_webview);
    }

    @Override
    protected void initView(View view) {
        super.initView(view);
        mWebView = (ESWebView) view.findViewById(R.id.webView);
        mWebView.initPlugin(mActivity);
    }

    public ESWebView getWebView() {
        return mWebView;
    }
}
