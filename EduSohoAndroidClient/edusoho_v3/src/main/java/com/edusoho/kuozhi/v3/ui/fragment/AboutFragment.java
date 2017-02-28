package com.edusoho.kuozhi.v3.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.ui.base.BaseFragment;
import com.edusoho.kuozhi.v3.util.Const;

/**
 * Created by howzhi on 14-9-21.
 */
public class AboutFragment extends BaseFragment {

    public static final String URL = "url";
    public static final String CONTENT = "content";
    public static final String TYPE = "type";
    private static final int FROM_URL = 0010;
    public static final int FROM_STR = 0020;

    private WebView mAboutWebView;
    private String mUrl;
    private int mType;
    private String mContent;
    private String mTitle;

    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContainerView(R.layout.activity_about_school);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mUrl = bundle.getString(URL);
            mTitle = bundle.getString(Const.ACTIONBAR_TITLE);
            mContent = bundle.getString(AboutFragment.CONTENT);
            mType = bundle.getInt(AboutFragment.TYPE, FROM_URL);
        }
    }

    @Override
    protected void initView(View view) {
        super.initView(view);

        mAboutWebView = (WebView) view.findViewById(R.id.webView);
        mAboutWebView.getSettings().setJavaScriptEnabled(true);
        mAboutWebView.getSettings().setDefaultTextEncodingName("UTF-8");

        initWebViewConfig();
        loadContent();
    }

    private void initWebViewConfig() {
        mAboutWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress == 100) {
                    showProgress(false);
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                if (TextUtils.isEmpty(mTitle)) {
                    changeTitle(title);
                }
            }
        });

        mAboutWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        mAboutWebView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() != KeyEvent.ACTION_DOWN) {
                    return false;
                }
                if (keyCode == KeyEvent.KEYCODE_BACK && mAboutWebView.canGoBack()) {
                    mAboutWebView.goBack();
                    return true;
                }
                return false;
            }
        });
    }

    private void loadContent() {
        if (mType == FROM_URL) {
            mAboutWebView.loadUrl(mUrl);
        } else {
            mAboutWebView.loadDataWithBaseURL(null, mContent, "text/html", "utf-8", null);
        }
    }
}
