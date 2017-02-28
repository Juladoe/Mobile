package com.edusoho.kuozhi.v3.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.imserver.IMClient;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.entity.site.Site;
import com.edusoho.kuozhi.v3.factory.FactoryManager;
import com.edusoho.kuozhi.v3.factory.provider.AppSettingProvider;
import com.edusoho.kuozhi.v3.listener.PluginRunCallback;
import com.edusoho.kuozhi.v3.listener.ResponseCallbackListener;
import com.edusoho.kuozhi.v3.model.bal.SystemInfo;
import com.edusoho.kuozhi.v3.model.bal.site.SiteModel;
import com.edusoho.kuozhi.v3.model.result.SchoolResult;
import com.edusoho.kuozhi.v3.model.sys.Error;
import com.edusoho.kuozhi.v3.model.sys.ErrorResult;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.model.sys.School;
import com.edusoho.kuozhi.v3.model.sys.Token;
import com.edusoho.kuozhi.v3.ui.base.BaseActivity;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.SchoolUtil;
import com.edusoho.kuozhi.v3.view.EdusohoAutoCompleteTextView;
import com.edusoho.kuozhi.v3.view.photo.SchoolSplashActivity;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhang
 */
public class NetSchoolDialog extends Dialog implements Response.ErrorListener {
    private static final String SEARCH_HISTORY = "search_history";
    private static final int REQUEST_QR = 001;
    private static final int RESULT_QR = 002;
    private EdusohoAutoCompleteTextView mSearchEdt;
    protected LoadDialog mLoading;
    private ArrayList<String> mSchoolList;
    private ListView mListView;
    private List<Site> mList = new ArrayList<>();
    private MyAdapter mAdapter;
    private View mCancel;
    private View mUrlCancel;
    private BaseActivity mContext;
    public EdusohoApp app;

    public NetSchoolDialog(Context context) {
        super(context, R.style.DialogFullscreen);
        init(context);
    }

    public NetSchoolDialog(Context context, int theme) {
        super(context, theme);
        init(context);
    }

    protected NetSchoolDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init(context);
    }

    private void init(Context context) {
        mContext = (BaseActivity) context;
        getWindow().setWindowAnimations(R.style.DialogWindowAnimationAlpha);
        app = (EdusohoApp) mContext.getApplication();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_net_school);
        initView();
    }

    private Pattern mPattern;

    private void initView() {
        mCancel = findViewById(R.id.net_school_cancel_search_btn);
        mUrlCancel = findViewById(R.id.url_cancel_iv);
        mSearchEdt = (EdusohoAutoCompleteTextView) findViewById(R.id.school_url_edit);
        mListView = (ListView) findViewById(R.id.net_school_listview);
        mAdapter = new MyAdapter(mContext);
        mListView.setAdapter(mAdapter);
        mSearchEdt.setKeyDownCallback(new EdusohoAutoCompleteTextView.KeyDownCallback() {
            @Override
            public void invoke(int length) {
                if (length < 1) {
                    mUrlCancel.setVisibility(View.INVISIBLE);
                    return;
                }
                if (mUrlCancel.getVisibility() != View.VISIBLE) {
                    mUrlCancel.setVisibility(View.VISIBLE);
                }
                Editable text = mSearchEdt.getText();
                char input = text.charAt(length - 1);
                if (input == '.') {
                    if (text.toString().endsWith("www.")) {
                        return;
                    }
                    ArrayList<String> hisList = new ArrayList<String>();
                    hisList.add(mSearchEdt.getText() + "com");
                    hisList.add(mSearchEdt.getText() + "cn");
                    hisList.add(mSearchEdt.getText() + "net");
                    hisList.add(mSearchEdt.getText() + "org");
                    hisList.addAll(mSchoolList);
                    setSearchEdmContexttory(hisList);
                }
            }
        });
        mUrlCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchEdt.setText("");
            }
        });
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        mPattern = Pattern.compile("((http|ftp|https):\\/\\/)?[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?^=%&amp;:/~\\+#]*[\\w\\-\\@?^=%&amp;/~\\+#])?"
        );
        mSearchEdt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    return true;
                }
                String searchStr = mSearchEdt.getText().toString();
                if (searchStr.trim().length() > 0) {
                    Matcher matcher = mPattern.matcher(searchStr);
                    if (matcher.matches()) {
                        saveSearchHistory(searchStr);
                        searchSchool(searchStr);
                    } else {
                        mLoading = LoadDialog.create(mContext);
                        mLoading.show();
                        SiteModel.getSite(searchStr, new ResponseCallbackListener<List<Site>>() {
                            @Override
                            public void onSuccess(List<Site> data) {
                                mLoading.dismiss();
                                mList.clear();
                                if (data.size() == 0) {
                                    CommonUtil.shortToast(mContext, "没有搜索到相关网校");
                                } else {
                                    mList.addAll(data);
                                }
                                mAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onFailure(String code, String message) {
                                mLoading.dismiss();
                            }
                        });
                    }
                }
                return true;
            }
        });

        loadSchoolHistory();
    }

    private void loadSchoolHistory() {
        mSchoolList = new ArrayList<>();
        SharedPreferences sp = mContext.getSharedPreferences(SEARCH_HISTORY, Context.MODE_APPEND);
        Map<String, ?> mSet = sp.getAll();
        Map<String, ?> schools = sp.getAll();
        for (String key : schools.keySet()) {
            mSchoolList.add(key);
        }

        setSearchEdmContexttory(mSchoolList);
    }

    private void saveSearchHistory(String text) {
        SharedPreferences sp = mContext.getSharedPreferences(SEARCH_HISTORY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        if (sp.contains(text)) {
            return;
        }
        editor.putString(text, "");
        editor.commit();
    }

    protected void setSearchEdmContexttory(ArrayList<String> list) {
        ArrayAdapter adapter = new ArrayAdapter(
                mContext, R.layout.search_school_dropdown_item, list);

        mSearchEdt.setAdapter(adapter);
    }

    private void searchSchool(String url) {
        if (TextUtils.isEmpty(url)) {
            CommonUtil.longToast(mContext, "请输入网校url");
            return;
        }
        if (!url.contains("http")) {
            url = "http://" + url;
        }
        if (!url.contains(Const.VERIFYVERSION)) {
            url = url + Const.VERIFYVERSION;
        }
        mLoading = LoadDialog.create(mContext);
        mLoading.show();

        RequestUrl requestUrl = new RequestUrl(url);
        app.getUrl(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                SystemInfo systemInfo = mContext.parseJsonValue(response, new TypeToken<SystemInfo>() {
                });

                if (systemInfo == null || TextUtils.isEmpty(systemInfo.mobileApiUrl)) {
                    mLoading.dismiss();
                    PopupDialog.createNormal(mContext, "提示信息", "没有搜索到网校").show();
                    return;
                }

                app.schoolVersion = systemInfo.version;

                getSchoolApi(systemInfo);
            }
        }, this);
    }

    private void handlerError(String errorStr) {
        try {
            ErrorResult result = app.gson.fromJson(errorStr, new TypeToken<ErrorResult>() {
            }.getType());
            if (result != null) {
                Error error = result.error;
                PopupDialog.createNormal(mContext, "系统提示", error.message).show();
            }
        } catch (Exception e) {
            PopupDialog.createNormal(mContext, "提示信息", "没有搜索到网校").show();
        }
    }


    private void showSchSplash(String schoolName, String[] splashs) {
        if (splashs == null || splashs.length == 0) {
            app.mEngine.runNormalPlugin("DefaultPageActivity", mContext, new PluginRunCallback() {
                @Override
                public void setIntentDate(Intent startIntent) {
                    startIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                }
            });
        }
        SchoolSplashActivity.start(mContext, schoolName, splashs);

        mContext.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        dismiss();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        mLoading.dismiss();
        if (error.networkResponse != null) {
            if (error.networkResponse.statusCode == 302 || error.networkResponse.statusCode == 301) {
                String redirectUrl = error.networkResponse.headers.get("location");
                searchSchool(redirectUrl);
            } else {
                CommonUtil.longToast(mContext, mContext.getResources().getString(R.string.request_fail_text));
            }
        }
    }

    private void startSchoolActivity(School site) {
        mLoading.dismiss();
        showSchSplash(site.name, site.splashs);
    }

    protected void getSchoolApi(SystemInfo systemInfo) {
        final RequestUrl schoolApiUrl = new RequestUrl(systemInfo.mobileApiUrl + Const.VERIFYSCHOOL);
        app.getUrl(schoolApiUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.equals("网校客户端未开启")) {
                    CommonUtil.shortToast(mContext, "网校客户端未开启");
                    mLoading.dismiss();
                    return;
                }
                SchoolResult schoolResult = app.gson.fromJson(
                        response, new TypeToken<SchoolResult>() {
                        }.getType());

                if (schoolResult == null
                        || schoolResult.site == null) {
                    handlerError(response);
                    mLoading.dismiss();
                    return;
                }

                School site = schoolResult.site;
                if (!SchoolUtil.checkMobileVersion(mContext, site, site.apiVersionRange)) {
                    mLoading.dismiss();
                    return;
                }
                bindApiToken(site);
            }
        }, this);
    }

    private void saveSchoolHistory(School site) {
        SimpleDateFormat nowfmt = new SimpleDateFormat("登录时间：yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String loginTime = nowfmt.format(date);
        Uri uri = Uri.parse(site.url);
        String domain = uri.getPort() == -1 ?
                uri.getHost() :
                uri.getHost() + ":" + uri.getPort();
        SchoolUtil.saveEnterSchool(mContext, site.name, loginTime, "登录账号：未登录", domain);
        startSchoolActivity(site);
    }

    protected void bindApiToken(final School site) {
        StringBuffer sb = new StringBuffer(site.host);
        sb.append(Const.GET_API_TOKEN);
        RequestUrl requestUrl = new RequestUrl(sb.toString());
        app.getUrl(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                mLoading.dismiss();
                Token token = mContext.parseJsonValue(response, new TypeToken<Token>() {
                });
                if (token == null || TextUtils.isEmpty(token.token)) {
                    CommonUtil.longToast(mContext, "获取网校信息失败");
                    return;
                }
                app.setCurrentSchool(site);
                app.removeToken();
                app.registDevice(null);
                app.saveApiToken(token.token);
                getAppSettingProvider().setUser(null);
                IMClient.getClient().destory();
                saveSchoolHistory(site);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mLoading.dismiss();
                app.setCurrentSchool(site);
                app.removeToken();
                app.registDevice(null);
                getAppSettingProvider().setUser(null);
                IMClient.getClient().destory();
                saveSchoolHistory(site);
            }
        });
    }

    private class MyAdapter extends BaseAdapter {
        Context context;

        public MyAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(context)
                        .inflate(R.layout.item_net_school_dialog, null);
                holder.schoolTv = (TextView) convertView.findViewById(R.id.net_school_tv);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.schoolTv.setText(mList.get(position).getSiteName());
            convertView.setTag(R.id.net_school_tv, position);
            convertView.setOnClickListener(mOnClickListener);
            return convertView;
        }

        private ViewHolder holder;
        View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag(R.id.net_school_tv);
                String schoolhost = mList.get(position).getSiteUrl();
                searchSchool(schoolhost);
            }
        };
    }

    private final class ViewHolder {
        public TextView schoolTv;
    }

    protected AppSettingProvider getAppSettingProvider() {
        return FactoryManager.getInstance().create(AppSettingProvider.class);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        Bundle bundle = new Bundle();
        bundle.putString("class", "QrSchoolActivity");
        app.sendMessage(Const.DIALOG_DISMISS, bundle);
    }

    @Override
    public void show() {
        super.show();
    }
}

