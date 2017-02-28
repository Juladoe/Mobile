package com.edusoho.kuozhi.v3.ui.fragment;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.ui.LessonActivity;
import com.edusoho.kuozhi.v3.ui.fragment.lesson.TextLessonFragment;
import com.edusoho.kuozhi.v3.view.webview.InnerWebView;

/**
 * Created by howzhi on 15/2/2.
 */
public class DocumentLessonFragment extends TextLessonFragment {

    private boolean mIsFullScreen;
    public static final int NORMAL_SCREEN = 0001;
    public static final int FULL_SCREEN = 0002;

    @Override
    protected void initWorkHandler() {
        webViewHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case FULL_SCREEN:
                        fullScreen();
                        break;
                    case NORMAL_SCREEN:
                        normalScreen();
                        break;
                }
            }
        };
    }

    @Override
    protected void initView(View view) {
        mLessonWebview = (InnerWebView) view.findViewById(R.id.lesson_webview);
        initWebViewSetting(mLessonWebview);
        mLessonWebview.loadUrl(mContent);
    }

    @Override
    protected void initWebViewSetting(WebView webView) {
        super.initWebViewSetting(webView);
        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
    }

    private void normalScreen() {
        mIsFullScreen = false;
        app.sendMsgToTarget(LessonActivity.SHOW_TOOLS, null, LessonActivity.class);
        mActivity.showActionBar();
    }

    private void fullScreen() {
        mIsFullScreen = true;
        app.sendMsgToTarget(LessonActivity.HIDE_TOOLS, null, LessonActivity.class);
        mActivity.hideActionBar();
    }

    @Override
    protected Object getJsObj() {
        return new FullJavaScriptObj();
    }

    /**
     * js注入对象
     */
    public class FullJavaScriptObj {
        @JavascriptInterface
        public void toggleFullScreen() {
            if (mIsFullScreen) {
                webViewHandler.obtainMessage(NORMAL_SCREEN).sendToTarget();
                return;
            }
            webViewHandler.obtainMessage(FULL_SCREEN).sendToTarget();
        }
    }
}
