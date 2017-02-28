package com.edusoho.kuozhi.v3.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.edusoho.kuozhi.v3.core.CoreEngine;
import com.edusoho.kuozhi.v3.listener.PluginRunCallback;
import com.edusoho.kuozhi.v3.ui.fragment.AboutFragment;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.view.dialog.PopupDialog;
import com.edusoho.kuozhi.v3.view.qr.CaptureActivity;
import com.google.zxing.Result;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.trinea.android.common.util.ToastUtils;

/**
 * Created by howzhi on 15/7/11.
 */
public class QrSearchActivity extends CaptureActivity {

    private List<URLMatchType> mURLMatchTypes;

    public static Pattern TYPE_PAT = Pattern.compile(
            "/([^/]+)/?(.+)",
            Pattern.DOTALL
    );

    @Override
    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        String msg = rawResult.getText();
        if (TextUtils.isEmpty(msg)) {
            ToastUtils.show(mContext, "没有扫描到任何东西!");
            return;
        }

        initURLMatchType(msg);
        if (parseResult(msg)) {
            finish();
        }
    }

    private void initURLMatchType(String result) {
        mURLMatchTypes = new ArrayList<>();
        mURLMatchTypes.add(new LoginURLMatchType(result));
        mURLMatchTypes.add(new SchoolURLMatchType(result));
    }

    protected boolean parseResult(final String result) {
        Log.d(TAG, "parseResult: " + result);
        if (!(result.startsWith("http://") || result.startsWith("https://"))) {
            showDataInWebView(result);
            return true;
        }
        URL resultUrl;
        try {
            resultUrl = new URL(result);
        } catch (Exception e) {
            e.printStackTrace();
            showDataInWebView(result);
            return true;
        }

        if (!resultUrl.getHost().equals(app.domain)) {
            PopupDialog popupDialog = PopupDialog.createNormal(
                    mContext,
                    "二维码提示",
                    "该二维码对应内容非当前登录网校内教学内容，请核对后重新扫描。"
            );
            popupDialog.setOkListener(new PopupDialog.PopupClickListener() {
                @Override
                public void onClick(int button) {
                    finish();
                }
            });
            popupDialog.show();
            return false;
        }

        Matcher typeMatcher = TYPE_PAT.matcher(resultUrl.getPath());
        if (typeMatcher.find() && urlcanMatch(typeMatcher)) {
            return false;
        }

        new RedirectUrl().execute(result);
        return false;
    }

    class RedirectUrl extends AsyncTask<String, Void, String> {

        private static final int CLASSROOM = 2;
        private static final int COURSE = 1;
        private static final int LESSON = 3;
        private int type = COURSE;

        @Override
        protected String doInBackground(String... params) {
            String url = params[0];
            String url302 = "";
            try {
                HttpURLConnection urlConnection = getConnection(url);
                urlConnection.connect();
                if (urlConnection.getResponseCode() == 302) {
                    url302 = urlConnection.getHeaderField("Location");
                    Log.d(TAG, "url302: " + url302);
                    if (TextUtils.isEmpty(url302)) {
                        url302 = urlConnection.getHeaderField("location");
                    }
                    if (url302 != null) {
                        if (url302.contains("course")) {
                            type = COURSE;
                        }
                        if (url302.contains("classroom")) {
                            type = CLASSROOM;
                        }
                        if (url302.contains("lesson")) {
                            type = LESSON;
                        }
                    }
                }
            } catch (Exception ex) {
                Log.d(TAG, "doInBackground: ex" + ex.getMessage());
            }
            return url302;
        }

        @Override
        protected void onPostExecute(String redirectUrl) {
            Log.d(TAG, "onPostExecute: " + redirectUrl);
            if (redirectUrl == null) {
                Toast.makeText(mContext, "二维码已失效", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            String[] urls = redirectUrl.split("/");
            final String targetId = urls[urls.length - 1];
            switch (type) {
                case COURSE:
                    CoreEngine.create(mContext).runNormalPlugin("CourseActivity", mContext, new PluginRunCallback() {
                        @Override
                        public void setIntentDate(Intent startIntent) {
                            startIntent.putExtra(Const.COURSE_ID, Integer.parseInt(targetId));
                        }
                    });
                    break;
                case CLASSROOM:
                    CoreEngine.create(mContext).runNormalPlugin("ClassroomActivity", mContext, new PluginRunCallback() {
                        @Override
                        public void setIntentDate(Intent startIntent) {
                            startIntent.putExtra(Const.CLASSROOM_ID, Integer.parseInt(targetId));
                        }
                    });
                    break;
                case LESSON:
                    Toast.makeText(mContext, "抱歉，当前版本不能识别此二维码", Toast.LENGTH_LONG).show();
                    break;
                default:
                    Toast.makeText(mContext, "暂不支持该类型的二维码", Toast.LENGTH_LONG).show();
                    break;
            }
            finish();
        }

        private HttpURLConnection getConnection(String strUrl) throws IOException {
            URL url = new URL(strUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setInstanceFollowRedirects(false);
            httpURLConnection.setConnectTimeout(10000);
            httpURLConnection.setReadTimeout(10000);
            return httpURLConnection;
        }
    }

    private void showDataInWebView(String data) {
        //webview打开
        Bundle bundle = new Bundle();
        bundle.putString(AboutFragment.CONTENT, data);
        bundle.putString(Const.ACTIONBAR_TITLE, "扫描结果");
        bundle.putInt(AboutFragment.TYPE, AboutFragment.FROM_STR);
        bundle.putString(FragmentPageActivity.FRAGMENT, "AboutFragment");
        app.mEngine.runNormalPluginWithBundle(
                "FragmentPageActivity", mContext, bundle);
    }

    private class LoginURLMatchType extends URLMatchType {

        public LoginURLMatchType(String url) {
            super(url);
            this.apiType = "mapi_v2";
            this.apiMethod = "User/loginWithToken";
        }

        @Override
        public void match() {
            new QrSchoolActivity.SchoolChangeHandler(mActivity).change(this.mUrl + "&version=2");
        }
    }

    private class SchoolURLMatchType extends URLMatchType {

        public SchoolURLMatchType(String url) {
            super(url);
            this.apiType = "mapi_v2";
            this.apiMethod = "School/loginSchoolWithSite";
        }

        @Override
        public void match() {
            new QrSchoolActivity.SchoolChangeHandler(mActivity).change(this.mUrl + "&version=2");
        }
    }

    private boolean urlcanMatch(Matcher typeMatcher) {
        String apiType = typeMatcher.group(1);
        String apiMethod = typeMatcher.group(2);
        Log.d(TAG, String.format("%s %s", apiType, apiMethod));
        for (URLMatchType matchType : mURLMatchTypes) {
            if (matchType.handle(apiType, apiMethod)) {
                return true;
            }
        }

        return false;
    }

    private abstract class URLMatchType {
        protected String apiType;
        protected String apiMethod;
        protected String mUrl;
        private String mHost;

        public URLMatchType(String url) {
            this.mUrl = url;

            try {
                this.mHost = new URL(url).getHost();
            } catch (Exception e) {
                this.mHost = "";
            }
        }

        public boolean handle(String apiType, String apiMethod) {
            if (apiType.equals(this.apiType) && apiMethod.equals(this.apiMethod)) {
                if (mHost.equals(app.domain)) {
                    match();
                    return true;
                }
                PopupDialog popupDialog = PopupDialog.createNormal(mActivity, "扫描提示", "请扫描当前网校二维码");
                popupDialog.setOkListener(new PopupDialog.PopupClickListener() {
                    @Override
                    public void onClick(int button) {
                        finish();
                    }
                });
                popupDialog.show();
                return true;
            }
            return false;
        }

        public abstract void match();
    }
}
