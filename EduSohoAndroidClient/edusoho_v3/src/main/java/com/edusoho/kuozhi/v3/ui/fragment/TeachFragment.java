package com.edusoho.kuozhi.v3.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.view.webview.ESWebChromeClient;
import com.edusoho.kuozhi.v3.view.webview.ESWebView;

/**
 * Created by JesseHuang on 15/12/17.
 */
public class TeachFragment extends Fragment {

    private Context mContext;
    private ESWebView mWebView;
    private String url = "";

    protected int mViewId;
    protected View mContainerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContainerView(R.layout.webview_activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mContainerView == null) {
            mContainerView = inflater.inflate(mViewId, null);
            initView(mContainerView);
        }

        ViewGroup parent = (ViewGroup) mContainerView.getParent();
        if (parent != null) {
            parent.removeView(mContainerView);
        }
        return mContainerView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }

    protected void setContainerView(int viewId) {
        mViewId = viewId;
    }

    protected void initView(View view) {
        mWebView = (ESWebView) view.findViewById(R.id.webView);
    }

    private void initData() {
        Bundle bundle = getArguments();
        url = bundle.getString(Const.WEB_URL);

        if (TextUtils.isEmpty(url)) {
            CommonUtil.longToast(mContext, "访问的地址不存在");
            return;
        }
        mWebView.initPlugin(getActivity());
        mWebView.setWebChromeClient(new ESWebChromeClient(mWebView.getWebView()) {
            @Override
            public void onReceivedTitle(WebView view, String title) {
            }
        });
        mWebView.loadUrl(url);
    }
}
