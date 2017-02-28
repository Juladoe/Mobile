package com.edusoho.kuozhi.v3.view.webview;

import android.app.Activity;
import com.edusoho.kuozhi.v3.view.webview.bridgeadapter.AbstractJsBridgeAdapterWebView;
import com.edusoho.kuozhi.v3.view.webview.bridgeadapter.JsBridgeAdapter;
/**
 * Created by howzhi on 15/7/15.
 */
public class ESCordovaWebViewFactory {

    private static final String TAG = "ESCordovaWebViewFactory";
    private static ESCordovaWebViewFactory factory;

    private ESCordovaWebViewFactory() {
    }

    public static void init() {
        JsBridgeAdapter.getInstance().init();
        factory = new ESCordovaWebViewFactory();
    }

    public static ESCordovaWebViewFactory getFactory() {
        if (factory == null) {
            init();
        }
        return factory;
    }

    public void destory() {
        factory = null;
    }

    public AbstractJsBridgeAdapterWebView getWebView(Activity activity) {
        return new ESJsNativeWebView(activity);
    }
}
