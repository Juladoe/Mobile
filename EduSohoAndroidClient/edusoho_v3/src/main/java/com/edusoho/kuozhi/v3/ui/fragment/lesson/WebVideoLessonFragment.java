package com.edusoho.kuozhi.v3.ui.fragment.lesson;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.listener.PluginRunCallback;
import com.edusoho.kuozhi.v3.entity.lesson.LessonItem;
import com.edusoho.kuozhi.v3.ui.FragmentPageActivity;
import com.edusoho.kuozhi.v3.ui.LessonActivity;
import com.edusoho.kuozhi.v3.ui.base.BaseFragment;
import com.edusoho.kuozhi.v3.ui.fragment.video.LessonVideoPlayerFragment;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.view.dialog.PopupDialog;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by howzhi on 14-9-19.
 */
public class WebVideoLessonFragment extends BaseFragment {

    private boolean isFullScreen;
    private boolean isAutoScreen;
    private NormalCallback mNormalCallback;
    private WebVideoWebChromClient mWebVideoWebChromClient;
    protected WebView mWebView;
    private static final String ADD_FULLSCREEN_CLICK = "javascript:var divs = document.getElementsByTagName('body');" +
            "for(var i=0; i < divs.length; i++){" +
            "if (divs[i].className == 'x-zoomin'){" +
            "window.obj.addFullScreenEvent();" +
            "divs[i].addEventListener('click', function(event){window.obj.toggleFullScreen(), false});}}";

    public static final int HIDE = 0001;
    public static final int FULL_SCREEN = 0002;
    public static final int CHECK_YOUKU_SCREEN = 0011;
    private static final String TAG = "WebVideoLessonFragment";

    private boolean isAddFullScreenEvent;
    private LessonItem.MediaSourceType mMediaSourceType;
    protected String mUri;
    private boolean mIsPlayVideo;
    private static String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.153 Safari/537.36";

    private Handler workHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CHECK_YOUKU_SCREEN:
                    mWebView.loadUrl(ADD_FULLSCREEN_CLICK);
                    break;
                case HIDE:
                    normalScreen();
                    break;
                case FULL_SCREEN:
                    fullScreen();
                    break;
            }
        }
    };

    @Override
    public String getTitle() {
        return "视频";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContainerView(R.layout.webvideo_fragment);
    }

    private void initWebViewSeting() {
        if (Build.VERSION.SDK_INT >= 11) {
            mWebView.setLayerType(View.LAYER_TYPE_NONE, null);
        }
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setPluginState(WebSettings.PluginState.ON);
        webSettings.setDefaultTextEncodingName("utf-8");
        webSettings.setAllowFileAccess(true);
        mWebView.addJavascriptInterface(new JavaScriptObj(), "obj");

        mWebVideoWebChromClient = new WebVideoWebChromClient();
        mWebView.setWebChromeClient(mWebVideoWebChromClient);
        mWebView.setWebViewClient(new WebVideoWebViewClient());
        if (isAutoScreen) {
            webSettings.setUseWideViewPort(true);
            webSettings.setLoadWithOverviewMode(true);
        }
    }

    private PopupDialog getInstallFlashDlg() {
        return PopupDialog.createMuilt(
                mActivity,
                "播放提示",
                "系统尚未安装播放器组件，是否下载安装？",
                new PopupDialog.PopupClickListener() {
                    @Override
                    public void onClick(int button) {
                        if (button == PopupDialog.OK) {
                            app.startUpdateWebView(String.format(
                                            "%s%s?version=%d",
                                            app.schoolHost,
                                            Const.FLASH_APK,
                                            Build.VERSION.SDK_INT)
                            );

                            mActivity.finish();
                        }
                    }
                }
        );
    }

    protected void loadContent() {
        //4.1以下
        if (Build.VERSION.SDK_INT < 16) {
            mWebView.getSettings().setUserAgentString(USER_AGENT);
            if (!checkInstallFlash()) {
                getInstallFlashDlg().show();
                return;
            }
        }

        mWebView.loadUrl(mUri);
        switch (mMediaSourceType) {
            case YOUKU:
                if (Build.VERSION.SDK_INT >= 19) {
                    checkYoukuPlayerClick();
                }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mWebView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mWebView.onResume();
        mIsPlayVideo = false;
    }

    @Override
    protected void initView(View view) {
        super.initView(view);
        mWebView = (WebView) view.findViewById(R.id.webvideo_webview);

        Bundle bundle = getArguments();
        mUri = bundle.getString(LessonVideoPlayerFragment.PLAY_URI);
        isAutoScreen = bundle.getBoolean("AutoScreen", false);
        mMediaSourceType = LessonItem.MediaSourceType.cover(
                bundle.getString(Const.MEDIA_SOURCE));
        if (mUri == null || "".equals(mUri)) {
            Toast.makeText(mActivity, "该课程无法播放!(无效播放网址)", Toast.LENGTH_SHORT).show();
            return;
        }

        initWebViewSeting();
        loadContent();
    }

    private boolean checkInstallFlash() {
        PackageManager pm = mContext.getPackageManager();
        List<PackageInfo> infoList = pm
                .getInstalledPackages(PackageManager.GET_SERVICES);
        for (PackageInfo info : infoList) {
            if ("com.adobe.flashplayer".equals(info.packageName)) {
                return true;
            }
        }
        return false;
    }


    private Timer workTimer;

    private void checkYoukuPlayerClick() {
        workTimer = new Timer();
        workTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (mContext) {
                    if (isAddFullScreenEvent) {
                        workTimer.cancel();
                        return;
                    }
                    workHandler.obtainMessage(CHECK_YOUKU_SCREEN).sendToTarget();
                }
            }
        }, 0, 1000);
    }

    /**
     * js注入对象
     */
    public class JavaScriptObj {
        @JavascriptInterface
        public void show(String html) {
            Log.i(null, "html->" + html);
        }

        @JavascriptInterface
        public void addFullScreenEvent() {
            isAddFullScreenEvent = true;
        }

        @JavascriptInterface
        public void toggleFullScreen() {
            if (isFullScreen) {
                workHandler.obtainMessage(HIDE).sendToTarget();
                return;
            }
            workHandler.obtainMessage(FULL_SCREEN).sendToTarget();
        }
    }

    private void fullScreen() {
        isFullScreen = true;
        app.sendMsgToTarget(LessonActivity.HIDE_TOOLS, null, LessonActivity.class);
        mActivity.hideActionBar();
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    private void normalScreen() {
        isFullScreen = false;
        app.sendMsgToTarget(LessonActivity.HIDE_TOOLS, null, LessonActivity.class);
        mActivity.showActionBar();
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (mView == null) {
            return;
        }

        ViewGroup viewGroup = (ViewGroup) mView.getParent();
        if (viewGroup != null) {
            viewGroup.removeView(mView);
            viewGroup.addView(mWebView);
        }

        if (mCustomViewCallback != null) {
            mCustomViewCallback.onCustomViewHidden();
            mCustomViewCallback = null;
        }

        mView = null;
    }

    private View mView;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;

    private class WebVideoWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }

        @Override
        public void onPageFinished(WebView view, final String url) {
            if (url.endsWith("javascript:;")) {
                if (mNormalCallback != null) {
                    mNormalCallback.success(null);
                }
                return;
            }

            if (url.matches(".+\\.mp4\\?.+")) {
                playVideo(url);
                webViewStop();
                return;
            }
            if (Build.VERSION.SDK_INT >= 16 && url.matches(".+\\.flv\\??.*")) {
                playVideo(url);
                webViewStop();
                return;
            }
            Log.d(TAG, "onPageFinished->" + url);
            super.onPageFinished(view, url);
        }
    }

    private synchronized void playVideo(final String url) {
        if (mIsPlayVideo) {
            return;
        }
        mIsPlayVideo = true;
        app.mEngine.runNormalPlugin("FragmentPageActivity", mActivity, new PluginRunCallback() {
            @Override
            public void setIntentDate(Intent startIntent) {
                startIntent.putExtra(Const.MEDIA_URL, url);
                startIntent.putExtra(FragmentPageActivity.FRAGMENT, "VideoFragment");
                startIntent.putExtra(Const.ACTIONBAR_TITLE, mWebView.getTitle());
            }
        });
    }

    private class WebVideoWebChromClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
            super.onShowCustomView(view, callback);
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            Log.d(TAG, "onShowCustomView");
            if (mCustomViewCallback != null) {
                mCustomViewCallback.onCustomViewHidden();
                mCustomViewCallback = null;
                return;
            }

            fullScreen();
            ViewGroup viewGroup = (ViewGroup) mWebView.getParent();
            viewGroup.removeView(mWebView);
            view.setBackgroundColor(Color.BLACK);
            view.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            viewGroup.addView(view);

            mView = view;
            mCustomViewCallback = callback;
        }

        @Override
        public void onHideCustomView() {
            normalScreen();
            Log.d(TAG, "onHideCustomView");
        }
    }

    private void webViewStop() {
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(
                new AudioManager.OnAudioFocusChangeListener() {
                    @Override
                    public void onAudioFocusChange(int i) {
                    }
                },
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
        );

        Log.i(null, "WebVideoActivity webview stop");
        try {
            Class.forName("android.webkit.WebView")
                    .getMethod("onPause", (Class[]) null)
                    .invoke(mWebView, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        webViewStop();
    }
}
