package com.edusoho.kuozhi.v3.ui.payCourse;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.ui.base.BaseFragment;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by howzhi on 14-10-11.
 */
public class AlipayFragment extends BaseFragment {
    private String mPayurl;
    private String mHost;

    private WebView webView;
    public static final int ALIPAY_REQUEST = 0001;
    public static final int ALIPAY_SUCCESS = 0002;
    public static final int ALIPAY_EXIT = 0003;

    private static Pattern urlPat = Pattern.compile("objc://([\\w\\W]+)\\?([\\w]+)", Pattern.DOTALL);

    //    @Override
    public String getTitle() {
        return "alipay";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContainerView(R.layout.alipay_layout);
    }

    @Override
    protected void initView(View view) {
        super.initView(view);

        Bundle bundle = getArguments();
        if (bundle == null) {
            CommonUtil.longToast(mContext, "错误的支付页面网址");
            return;
        }

        mPayurl = bundle.getString("payurl");
        try {
            URL hostURL = new URL(mPayurl);
            mHost = "http://" + hostURL.getHost();
        } catch (Exception e) {
            mHost = "";
        }

        webView = (WebView) view.findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.d(null, "url ->" + url);
                Matcher matcher = urlPat.matcher(url);
                if (matcher.find()) {
                    String callBack = matcher.group(1);
                    String param = matcher.group(2);
                    callMethod(callBack, param);
                    return;
                }
                if (url.startsWith(mHost) && !isEqualsURl(mPayurl, url)) {
                    app.sendMsgToTarget(PayCourseActivity.PAY_EXIT, null, PayCourseActivity.class);
                    mActivity.finish();
                }
            }
        });
        webView.loadUrl(mPayurl);
    }

    private void callMethod(String name, String params) {
        Log.d(null, "callMethod->" + name + "  " + params);
        try {
            Method method = getClass().getMethod(name, new Class[]{String.class});
            method.invoke(this, params);
        } catch (Exception e) {
            Log.d(null, e.toString());
        }
    }

    public void alipayCallback(String status) {
        if (Const.RESULT_SUCCESS.equals(status)) {
            app.sendMsgToTarget(PayCourseActivity.PAY_SUCCESS, null, PayCourseActivity.class);
            mActivity.finish();
        }
    }

    private boolean isEqualsURl(String firstUrl, String secUrl) {
        int tagIndex1 = firstUrl.indexOf("?");
        if (tagIndex1 != -1) {
            firstUrl = firstUrl.substring(0, tagIndex1);
            int tagIndex2 = secUrl.indexOf("?");
            if (tagIndex2 != -1) {
                secUrl = secUrl.substring(0, tagIndex2);
            }
        }

        if (firstUrl.equals(secUrl)) {
            return true;
        }

        return false;
    }
}
