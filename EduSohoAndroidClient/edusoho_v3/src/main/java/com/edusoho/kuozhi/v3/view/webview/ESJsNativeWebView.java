package com.edusoho.kuozhi.v3.view.webview;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import com.edusoho.kuozhi.v3.ui.base.ActionBarBaseActivity;
import com.edusoho.kuozhi.v3.view.webview.bridgeadapter.AbstractJsBridgeAdapterWebView;

/**
 * Created by su on 2015/12/8.
 */
public class ESJsNativeWebView extends AbstractJsBridgeAdapterWebView<ActionBarBaseActivity> {

    public ESJsNativeWebView(Context context) {
        super(context);
    }

    public ESJsNativeWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ESJsNativeWebView(Context context, AttributeSet attrs, int defstyle) {
        super(context, attrs, defstyle);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
    }

    @Override
    protected void initWebSetting(WebSettings webSettings) {
        super.initWebSetting(webSettings);
        String userAgent = webSettings.getUserAgentString();
        webSettings.setUserAgentString(userAgent.replace("Android", "Android-kuozhi;Android"));
    }
}
