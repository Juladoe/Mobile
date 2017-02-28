package com.edusoho.kuozhi.v3.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.ui.base.BaseNoTitleActivity;

/**
 * Created by JesseHuang on 15/6/17.
 */
public class WebViewDataActivity extends BaseNoTitleActivity {

    private final static String TAG = "WebViewDataActivity";
    public final static int CLOSE = 0x01;
    public final static String SEND_EVENT = "send_event";
    public static final int BACK = 0x02;
    public static final String TITLE = "title";
    public static final String DATA = "data";

    private WebView mWebView;
    private TextView mTitleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(this).inflate(R.layout.activity_web_data,null,false);
        setContentView(view);
        initView();
        initCordovaWebView();
    }

    @Override
    protected void initView() {
        super.initView();
        mTitleView = (TextView) findViewById(R.id.tv_title);
    }

    public void initCordovaWebView() {
        Intent intent = getIntent();
        String data = intent.getStringExtra(DATA);
        if (data == null && data.length() == 0) {
            finish();
        }
        String title = intent.getStringExtra(TITLE);
        if (title != null && mTitleView != null) {
            mTitleView.setText(title);
        }
        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.loadDataWithBaseURL(null, data, "text/html", "utf-8", null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebView.destroy();
        mWebView = null;
    }
}
