package com.edusoho.kuozhi.v3.ui.fragment.lesson;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.NestedScrollView;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.ui.LessonActivity;
import com.edusoho.kuozhi.v3.ui.base.BaseFragment;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.view.webview.InnerWebView;

import java.io.IOException;
import java.io.InputStream;

import cn.trinea.android.common.util.FileUtils;

/**
 * Created by howzhi on 14-9-15.
 */
public class TextLessonFragment extends BaseFragment implements NestedScrollView.OnScrollChangeListener {

    private static final String TEXT_CONFIG = "text_config";
    private static final String TEXT_POSITION = "%d_%d_text_position";

    protected InnerWebView mLessonWebview;
    protected String mContent;

    private int mCurrentScrollPosition;
    private int mLessonId;
    private int mCourseId;
    protected Handler webViewHandler;
    private static final int SHOW_IMAGES = 0002;

    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContainerView(R.layout.text_fragment_layout);
        initWorkHandler();
        initConfig();
    }

    @Override
    public void onStop() {
        super.onStop();
        saveConfig();
    }

    private void initConfig() {
        SharedPreferences sp = getContext().getSharedPreferences(TEXT_CONFIG, Context.MODE_PRIVATE);
        mCurrentScrollPosition = sp.getInt(String.format(TEXT_POSITION, mCourseId, mLessonId), 0);
    }

    private void saveConfig() {
        SharedPreferences sp = getContext().getSharedPreferences(TEXT_CONFIG, Context.MODE_PRIVATE);
        sp.edit().putInt(String.format(TEXT_POSITION, mCourseId, mLessonId), mCurrentScrollPosition).commit();
    }

    protected void initWorkHandler() {
        webViewHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case SHOW_IMAGES:
                        Bundle bundle = new Bundle();
                        bundle.putInt("index", msg.arg1);
                        bundle.putStringArray("images", (String[]) msg.obj);
                        app.mEngine.runNormalPluginWithBundle("ViewPagerActivity", mActivity, bundle);
                        break;
                }
            }
        };
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle bundle = getArguments();
        mContent = bundle.getString(LessonActivity.CONTENT);
        mCourseId = bundle.getInt(Const.COURSE_ID);
        mLessonId = bundle.getInt(Const.LESSON_ID);
    }

    @Override
    protected void initView(View view) {
        super.initView(view);

        mLessonWebview = (InnerWebView) view.findViewById(R.id.lesson_webview);
        initWebViewSetting(mLessonWebview);
        mLessonWebview.loadDataWithBaseURL(app.host, getWrapContent(mContent), "text/html", "utf-8", null);
        mLessonWebview.scrollTo(0, mCurrentScrollPosition);
        mLessonWebview.setOnScrollListener(this);
    }

    private String getWrapContent(String content) {
        try {
            InputStream inputStream = getContext().getAssets().open("template.html");
            String wrapContent = FileUtils.readFile(inputStream);
            return wrapContent.replace("%content%", content);
        } catch (IOException e) {
        }
        return content;
    }

    protected void initWebViewSetting(WebView webView) {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setPluginState(WebSettings.PluginState.ON);
        webSettings.setAllowFileAccess(true);
        webSettings.setDefaultTextEncodingName("utf-8");

        webView.addJavascriptInterface(getJsObj(), "jsobj");
        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("imageindexnurls")) {
                    return true;
                }
                return true;
            }
        });
    }

    @Override
    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        mCurrentScrollPosition = scrollY;
    }

    protected Object getJsObj() {
        return new JavaScriptObj();
    }

    /**
     * js注入对象
     */
    public class JavaScriptObj {
        @JavascriptInterface
        public void showHtml(String src) {
        }

        @JavascriptInterface
        public void showImages(String index, String[] imageArray) {
            Message msg = webViewHandler.obtainMessage(SHOW_IMAGES);
            msg.obj = imageArray;
            msg.arg1 = Integer.parseInt(index);
            msg.sendToTarget();
        }
    }
}
