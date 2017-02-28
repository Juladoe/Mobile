package com.edusoho.kuozhi.v3.ui.base;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.factory.FactoryManager;
import com.edusoho.kuozhi.v3.factory.UtilFactory;
import com.edusoho.kuozhi.v3.factory.provider.AppSettingProvider;
import com.edusoho.kuozhi.v3.model.sys.ErrorResult;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.service.EdusohoMainService;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.VolleySingleton;
import com.edusoho.kuozhi.v3.view.dialog.LoadDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Map;

/**
 * Created by JesseHuang on 15/5/6.
 * 一般用于NoActionBar的theme
 */
public class BaseActivity extends ActionBarActivity {
    public static final String TAG = "ActionBarBaseActivity";
    public static final String BACK = "返回";
    protected BaseActivity mActivity;
    public Gson gson;
    protected Context mContext;
    public EdusohoApp app;
    public ActionBar mActionBar;
    protected FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        mContext = this;
        initActivity();
    }

    private void initActivity() {
        app = (EdusohoApp) getApplication();
        mActionBar = getSupportActionBar();
        mFragmentManager = getSupportFragmentManager();
        app.setDisplay(this);

        gson = app.gson;
        app.mActivity = mActivity;
        app.mContext = mContext;
    }

    @Override
    public void startActivity(Intent intent) {

        ComponentName componentName = intent.getComponent();
        if (componentName == null) {
            super.startActivity(intent);
            return;
        }

        String className = componentName.getClassName();
        if (getPackageName().equals(componentName.getPackageName()) && app.mEngine.isPlugin(className)) {
            intent.setComponent(new ComponentName(componentName.getPackageName(), app.mEngine.getPluginPkgName(className)));
        }
        super.startActivity(intent);
    }

    @Override
    public void startActivityForResult(final Intent intent, int requestCode, Bundle options) {

        ComponentName componentName = intent.getComponent();
        if (componentName == null) {
            super.startActivityForResult(intent, requestCode, options);
            return;
        }

        String className = componentName.getClassName();
        if (getPackageName().equals(componentName.getPackageName()) && app.mEngine.isPlugin(className)) {
            intent.setComponent(new ComponentName(componentName.getPackageName(), app.mEngine.getPluginPkgName(className)));
        }

        super.startActivityForResult(intent, requestCode, options);
    }

    public void hideActionBar() {
        if (mActionBar == null) {
            return;
        }
        mActionBar.hide();
    }

    public void showActionBar() {
        if (mActionBar == null) {
            return;
        }
        mActionBar.show();
    }

    public void ajaxPost(final RequestUrl requestUrl) {
        VolleySingleton.getInstance(this.getApplicationContext()).getRequestQueue();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, requestUrl.url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }

        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return requestUrl.getParams();
            }
        };
        stringRequest.setTag(requestUrl.url);

        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    public void ajaxPostWithLoading(final RequestUrl requestUrl, final Response.Listener<String> responseListener, final Response.ErrorListener errorListener, String loadingText) {
        final LoadDialog loadDialog = LoadDialog.create(mActivity);
        if (!TextUtils.isEmpty(loadingText)) {
            loadDialog.setMessage(loadingText);
        }
        loadDialog.show();
        app.postUrl(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                loadDialog.dismiss();
                responseListener.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loadDialog.dismiss();
                if (errorListener != null) {
                    errorListener.onErrorResponse(error);
                } else {
                    CommonUtil.longToast(mContext, getResources().getString(R.string.request_fail_text));
                }
            }
        });
    }

    public void ajaxPostMultiUrl(final RequestUrl requestUrl, final Response.Listener<String> responseListener, final Response.ErrorListener errorListener, int method) {
        app.postMultiUrl(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                responseListener.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (errorListener != null) {
                    errorListener.onErrorResponse(error);
                } else {
                    CommonUtil.longToast(mContext, getResources().getString(R.string.request_fail_text));
                }
            }
        }, method);
    }

    public void ajaxPost(final RequestUrl requestUrl, final Response.Listener<String> responseListener, final Response.ErrorListener errorListener) {
        app.postUrl(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                responseListener.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (errorListener != null) {
                    errorListener.onErrorResponse(error);
                } else if (error instanceof NoConnectionError) {
                    CommonUtil.longToast(mContext, getResources().getString(R.string.request_fail_text));
                }
            }
        });
    }

    public void ajaxGet(final String url, Response.Listener<String> responseListener, Response.ErrorListener errorListener) {
        app.getUrl(new RequestUrl(url), responseListener, errorListener);
    }

    public void ajaxGet(final RequestUrl requestUrl, final Response.Listener<String> responseListener, final Response.ErrorListener errorListener) {
        app.getUrl(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                responseListener.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (errorListener != null) {
                    errorListener.onErrorResponse(error);
                } else {
                    CommonUtil.longToast(mContext, getResources().getString(R.string.request_fail_text));
                }
            }
        });
    }


    public void runService(String serviceName) {
        app.mEngine.runService(serviceName, mActivity, null);
    }

    public <T> T parseJsonValue(String json, TypeToken<T> typeToken) {
        T value = null;
        try {
            value = mActivity.gson.fromJson(
                    json, typeToken.getType());
        } catch (Exception e) {
            e.printStackTrace();
            return value;
        }

        return value;
    }

    public <T> T handleJsonValue(String response, TypeToken<T> typeToken) {
        ErrorResult result = parseJsonValue(response, new TypeToken<ErrorResult>() {
        });
        if (result != null && result.error != null) {
            CommonUtil.longToast(mActivity, result.error.message);
            return null;
        } else {
            return parseJsonValue(response, typeToken);
        }
    }

    protected AppSettingProvider getAppSettingProvider() {
        return FactoryManager.getInstance().create(AppSettingProvider.class);
    }

    protected UtilFactory getUtilFactory() {
        return FactoryManager.getInstance().create(UtilFactory.class);
    }
}
