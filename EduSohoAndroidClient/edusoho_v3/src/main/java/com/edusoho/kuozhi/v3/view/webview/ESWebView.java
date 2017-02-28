package com.edusoho.kuozhi.v3.view.webview;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.cache.request.RequestCallback;
import com.edusoho.kuozhi.v3.cache.request.RequestManager;
import com.edusoho.kuozhi.v3.cache.request.model.Request;
import com.edusoho.kuozhi.v3.cache.request.model.Response;
import com.edusoho.kuozhi.v3.core.MessageEngine;
import com.edusoho.kuozhi.v3.factory.FactoryManager;
import com.edusoho.kuozhi.v3.factory.UtilFactory;
import com.edusoho.kuozhi.v3.model.htmlapp.AppMeta;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import com.edusoho.kuozhi.v3.view.webview.bridgeadapter.AbstractJsBridgeAdapterWebView;
import cn.trinea.android.common.util.FileUtils;

/**
 * Created by howzhi on 15/4/16.
 */
public class ESWebView extends RelativeLayout {

    public static final int LOAD_FROM_CACHE = 0001;
    public static final int LOAD_FROM_NET = 0002;
    public static final int LOAD_AUTO = 0003;
    public static final String MAIN_UPDATE = "html5_main_update";

    private int mLoadType;
    protected AbstractJsBridgeAdapterWebView mWebView;
    protected ProgressBar pbLoading;
    protected Context mContext;
    protected Activity mActivity;
    protected String mAppCode;
    private AttributeSet mAttrs;
    private String mUrl;
    private static final String TAG = "ESWebView";
    private static Pattern APPCODE_PAT = Pattern.compile(".+/mapi_v2/mobile/(\\w+)[#|/]*", Pattern.DOTALL);

    private RequestManager mRequestManager;
    private AppMeta mLocalAppMeta;

    public ESWebView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public ESWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        this.mAttrs = attrs;
        init();
    }

    public void setLoadType(int type) {
        this.mLoadType = type;
    }

    protected void init() {
        mLoadType = LOAD_AUTO;
    }

    public int getLoadType() {
        return mLoadType;
    }

    public String getUserAgent() {
        return mWebView.getSettings().getUserAgentString();
    }

    private AbstractJsBridgeAdapterWebView createWebView() {
        return ESCordovaWebViewFactory.getFactory().getWebView(mActivity);
    }

    private void setupWebView() {
        mWebView.setScrollBarSize(0);
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        mWebView.setWebViewClient(new ESWebViewClient());
        mWebView.setWebChromeClient(new ESWebChromeClient(mWebView));
    }

    public void setWebChromeClient(WebChromeClient webChromeClient) {
        mWebView.setWebChromeClient(webChromeClient);
    }

    private void initWebView() {
        mWebView = createWebView();
        setupWebView();

        RelativeLayout.LayoutParams webViewProgressBar = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        webViewProgressBar.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        addView(mWebView, webViewProgressBar);

        pbLoading = (ProgressBar) LayoutInflater.from(mContext).inflate(R.layout.progress_bar, null);
        RelativeLayout.LayoutParams paramProgressBar = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramProgressBar.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        paramProgressBar.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        addView(pbLoading, paramProgressBar);
    }

    public RequestManager getRequestManager() {
        return mRequestManager;
    }

    private void updateCode(String code) {
        this.mAppCode = code;
        mRequestManager = ESWebViewRequestManager.getRequestManager(mContext, this.mAppCode);
        mRequestManager.setWebView(this);
    }

    private AppMeta getLocalApp(String appCode) {
        EdusohoApp app = (EdusohoApp) mActivity.getApplication();
        File schoolStorage = AppUtil.getHtmlPluginStorage(mContext, app.domain);
        File appDir = new File(schoolStorage, appCode);

        if (appDir.exists()) {
            StringBuilder appVersionString = FileUtils.readFile(
                    new File(appDir, "version.json").getAbsolutePath(), "utf-8");
            if (appVersionString == null) {
                return null;
            }
            return getUtilFactory().getJsonParser().fromJson(appVersionString.toString(), AppMeta.class);
        }

        return null;
    }

    public void reload() {
        mWebView.reload();
    }

    public boolean canGoBack() {
        return mWebView.canGoBack();
    }

    public Activity getActivity() {
        return mActivity;
    }

    public void goBack() {
        mWebView.setGoBackStatus(true);
        mWebView.goBack();
    }

    public String getAppCode() {
        return mAppCode;
    }

    public void updateApp(String appCode) {
        String projectCode = mContext.getString(R.string.app_code);
        EdusohoApp app = (EdusohoApp) mActivity.getApplication();
        RequestUrl appVersionUrl = app.bindUrl(
                String.format(Const.MOBILE_APP_VERSION, appCode, projectCode), true);

        RequestCallback<Boolean> callback = new RequestCallback<Boolean>() {
            @Override
            public Boolean onResponse(Response response) {
                new Handler(mActivity.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, MAIN_UPDATE);
                        MessageEngine.getInstance().sendMsg(MAIN_UPDATE, null);
                    }
                });
                return false;
            }
        };
        mRequestManager.updateApp(appVersionUrl, callback);
    }

    public void loadUrl(String url) {
        mUrl = url;
        Matcher matcher = APPCODE_PAT.matcher(url);
        if (matcher.find()) {
            updateCode(matcher.group(1));
            mLocalAppMeta = getLocalApp(mAppCode);
        }

        if (TextUtils.isEmpty(mAppCode)) {
            mWebView.loadUrl(mUrl);
            return;
        }

        updateApp(mAppCode);
        if (checkResourceIsExists()) {
            mWebView.loadUrl(mUrl);
        }
    }

    private boolean checkResourceIsExists() {
        EdusohoApp app = (EdusohoApp) mActivity.getApplication();
        File schoolStorage = AppUtil.getHtmlPluginStorage(mContext, app.domain);
        File schoolAppFile = new File(schoolStorage, mAppCode);

        if (mLocalAppMeta == null) {
            if (AppUtil.unZipFile(schoolAppFile, getInnerHtmlPluginInputStream(mAppCode))) {
                mLocalAppMeta = getLocalApp(mAppCode);
            }
            return mLocalAppMeta != null;
        }

        AppMeta innerHtmlPluginAppMeta = getHtmlPluginAppMeta(mAppCode);
        if (innerHtmlPluginAppMeta == null) {
            return mLocalAppMeta != null;
        }

        int result = CommonUtil.compareVersion(mLocalAppMeta.version, innerHtmlPluginAppMeta.version);
        if (-1 == Const.LOW_VERSIO) {
            if (AppUtil.unZipFile(schoolAppFile, getInnerHtmlPluginInputStream(mAppCode))) {
                mLocalAppMeta = getLocalApp(mAppCode);
            }
        }
        return mLocalAppMeta != null;
    }

    private InputStream getInnerHtmlPluginInputStream(String code) {
        InputStream zinInputStream = null;
        try {
            String projectCode = mContext.getString(R.string.app_code);
            zinInputStream = mContext.getAssets().open(String.format("%s-html5-%s.Android.zip", projectCode, code));
        } catch (Exception e) {
        }

        return zinInputStream;
    }

    private AppMeta getHtmlPluginAppMeta(String code) {
        AppMeta localAppMeta = null;
        InputStream inputStream = null;
        String projectCode = mContext.getString(R.string.app_code);
        try {
            inputStream = mContext.getAssets().open(String.format("%s-html5-%s.Android.zip", projectCode, code));
            ZipInputStream zipInputStream = new ZipInputStream(inputStream);
            ZipEntry zipEntry = null;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if ("version.json".equals(zipEntry.getName())) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(zipInputStream));
                    String line = null;
                    StringBuilder stringBuilder = new StringBuilder();
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    localAppMeta = getUtilFactory().getJsonParser().fromJson(stringBuilder.toString(), AppMeta.class);
                    break;
                }
                zipInputStream.closeEntry();
            }
        } catch (IOException e) {
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {}
            }
        }

        return localAppMeta;
    }

    public void initPlugin(Activity activity) {
        this.mActivity = activity;
        initWebView();
    }

    public void destroy() {
        Log.d(TAG, "destroy");

        mWebView.stopLoading();
        mWebView.clearHistory();
        mWebView.handleDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN
                && keyCode == KeyEvent.KEYCODE_BACK
                && mWebView.canGoBack()) {
            goBack();
            return true;
        }
        return false;
    }

    private class ESWebViewClient extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            mWebView.setGoBackStatus(false);
            pbLoading.setVisibility(GONE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            EdusohoApp app = (EdusohoApp) mActivity.getApplication();
            if (url.startsWith(app.host)) {
                loadUrl(url);
                return true;
            }

            app.startUpdateWebView(url);
            return true;
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            if (mAppCode == null) {
                return super.shouldInterceptRequest(view, url);
            }

            WebResourceResponse resourceResponse = mRequestManager.blockGet(
                    new Request(url), new RequestCallback<WebResourceResponse>() {
                        @Override
                        public WebResourceResponse onResponse(Response<WebResourceResponse> response) {

                            if (response.isEmpty()) {
                                return null;
                            }
                            WebResourceResponse webResourceResponse = new WebResourceResponse(
                                    response.getMimeType(), response.getEncoding(), response.getContent()
                            );
                            return webResourceResponse;
                        }
                    });

            if (resourceResponse == null) {
                resourceResponse = super.shouldInterceptRequest(view, url);
            }

            return resourceResponse;
        }
    }

    public AbstractJsBridgeAdapterWebView getWebView() {
        return mWebView;
    }

    protected UtilFactory getUtilFactory() {
        return FactoryManager.getInstance().create(UtilFactory.class);
    }
}
