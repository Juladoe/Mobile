package com.edusoho.kuozhi.v3.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.imserver.IMClient;
import com.edusoho.kuozhi.v3.listener.PluginRunCallback;
import com.edusoho.kuozhi.v3.model.bal.SystemInfo;
import com.edusoho.kuozhi.v3.model.result.SchoolResult;
import com.edusoho.kuozhi.v3.model.sys.Error;
import com.edusoho.kuozhi.v3.model.sys.ErrorResult;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.model.sys.School;
import com.edusoho.kuozhi.v3.model.sys.Token;
import com.edusoho.kuozhi.v3.ui.base.ActionBarBaseActivity;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.SchoolUtil;
import com.edusoho.kuozhi.v3.view.EdusohoAutoCompleteTextView;
import com.edusoho.kuozhi.v3.view.dialog.LoadDialog;
import com.edusoho.kuozhi.v3.view.dialog.PopupDialog;
import com.edusoho.kuozhi.v3.view.photo.SchoolSplashActivity;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by JesseHuang on 15/5/28.
 */
public class NetSchoolActivity extends ActionBarBaseActivity implements Response.ErrorListener {
    private static final String SEARCH_HISTORY = "search_history";
    private static final String EnterSchool = "enter_school";
    private static final int REQUEST_QR = 001;
    private static final int RESULT_QR = 002;
    private EdusohoAutoCompleteTextView mSearchEdt;
    protected LoadDialog mLoading;
    private ArrayList<String> mSchoolList;
    private ListView mListView;
    private List<Map<String, Object>> mList;
    private MyAdapter adapter;
    private TextView mtv;
    private Button mCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_school);
        app.addTask("NetSchoolActivity", this);
        //getSupportActionBar().hide();
        initView();
    }

    private void initView() {
        mCancel = (Button) findViewById(R.id.net_school_cancel_search_btn);
        mtv = (TextView) findViewById(R.id.net_school_tv);
        mSearchEdt = (EdusohoAutoCompleteTextView) findViewById(R.id.school_url_edit);
        mListView = (ListView) this.findViewById(R.id.net_school_listview);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if (loadEnterSchool().size() != 0) {
            list = loadEnterSchool();
            Collections.reverse(list);
        } else {
            mtv.setVisibility(View.GONE);
        }
        mList = list;
        adapter = new MyAdapter(this);
        mListView.setAdapter(adapter);
        mSearchEdt.setKeyDownCallback(new EdusohoAutoCompleteTextView.KeyDownCallback() {
            @Override
            public void invoke(int length) {
                if (length < 1) {
                    return;
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
                    setSearchEdtHistory(hisList);
                }
            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mSearchEdt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    return true;
                }
                String searchStr = mSearchEdt.getText().toString();
                saveSearchHistory(searchStr);
                searchSchool(searchStr);
                return true;
            }
        });

        loadSchoolHistory();
    }

    private void loadSchoolHistory() {
        mSchoolList = new ArrayList<>();
        SharedPreferences sp = getSharedPreferences(SEARCH_HISTORY, Context.MODE_APPEND);
        Map<String, ?> mSet = sp.getAll();
        Map<String, ?> schools = sp.getAll();
        for (String key : schools.keySet()) {
            mSchoolList.add(key);
        }

        setSearchEdtHistory(mSchoolList);
    }

    private void saveSearchHistory(String text) {
        SharedPreferences sp = getSharedPreferences(SEARCH_HISTORY, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        if (sp.contains(text)) {
            return;
        }
        editor.putString(text, "");
        editor.commit();
    }

    public void saveEnterSchool(String schoolname, String entertime, String loginname, String schoolhost) {
        Map map = new HashMap();
        String lable = new String();
        lable = schoolname.substring(0, 2);
        map.put("lable", lable);
        map.put("schoolname", schoolname);
        map.put("entertime", entertime);
        map.put("loginname", loginname);
        map.put("schoolhost", schoolhost);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if (loadEnterSchool() != null) {
            list = loadEnterSchool();
        }
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).get("schoolhost").toString().equals(map.get("schoolhost"))) {
                list.remove(i);
                i--;
            }
        }
        list.add(map);
        if (list.size() > 4) {
            list.remove(0);
        }
        JSONArray mJsonArray;
        mJsonArray = new JSONArray();
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> itemMap = list.get(i);
            Iterator<Map.Entry<String, Object>> iterator = itemMap.entrySet().iterator();

            JSONObject object = new JSONObject();

            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                try {
                    object.put(entry.getKey(), entry.getValue());
                } catch (JSONException e) {

                }
            }
            mJsonArray.put(object);
        }

        SharedPreferences sp = getSharedPreferences("EnterSchool", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(EnterSchool, mJsonArray.toString());
        editor.apply();
    }

    private List<Map<String, Object>> loadEnterSchool() {
        List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
        SharedPreferences sp = getSharedPreferences("EnterSchool", Context.MODE_PRIVATE);
        String result = sp.getString(EnterSchool, "");
        try {
            JSONArray array = new JSONArray(result);
            for (int i = 0; i < array.length(); i++) {
                JSONObject itemObject = array.getJSONObject(i);
                Map<String, Object> itemMap = new HashMap<String, Object>();
                JSONArray names = itemObject.names();
                if (names != null) {
                    for (int j = 0; j < names.length(); j++) {
                        String name = names.getString(j);
                        String value = itemObject.getString(name);
                        itemMap.put(name, value);
                    }
                }
                datas.add(itemMap);
            }
        } catch (JSONException e) {
        }

        return datas;
    }

    protected void setSearchEdtHistory(ArrayList<String> list) {
        ArrayAdapter adapter = new ArrayAdapter(
                mContext, R.layout.search_school_dropdown_item, list);

        mSearchEdt.setAdapter(adapter);
    }

    private void searchSchool(String url) {
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
                SystemInfo systemInfo = parseJsonValue(response, new TypeToken<SystemInfo>() {
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

    public boolean checkMobileVersion(final School site, HashMap<String, String> versionRange) {
        String min = versionRange.get("min");
        String max = versionRange.get("max");

        int result = AppUtil.compareVersion(app.apiVersion, min);
        if (result == Const.LOW_VERSIO) {
            PopupDialog dlg = PopupDialog.createMuilt(
                    mContext,
                    "网校提示",
                    "您的客户端版本过低，无法登录该网校，请立即更新至最新版本。",
                    new PopupDialog.PopupClickListener() {
                        @Override
                        public void onClick(int button) {
                            if (button == PopupDialog.OK) {
                                String code = getResources().getString(R.string.app_code);
                                String updateUrl = String.format(
                                        "%s/%s?code=%s",
                                        site.url,
                                        Const.DOWNLOAD_URL,
                                        code
                                );
                                app.startUpdateWebView(updateUrl);
                            }
                        }
                    });

            dlg.setOkText("立即下载");
            dlg.show();
            return false;
        }

        result = AppUtil.compareVersion(app.apiVersion, max);
        if (result == Const.HEIGHT_VERSIO) {
            PopupDialog.createNormal(
                    mContext,
                    "网校提示",
                    "网校服务器版本过低，无法继续登录！请重新尝试。"
            ).show();
            return false;
        }

        return true;
    }

    private void showSchSplash(String schoolName, String[] splashs) {
        if (splashs == null || splashs.length == 0) {
            app.mEngine.runNormalPlugin("DefaultPageActivity", mActivity, new PluginRunCallback() {
                @Override
                public void setIntentDate(Intent startIntent) {
                    startIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                }
            });
        }
        SchoolSplashActivity.start(mContext, schoolName, splashs);

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void showQrResultDlg(String result) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        AlertDialog dlg = builder.setTitle("扫描结果")
                .setMessage("二维码信息:" + result)
                .setNegativeButton("查看", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create();
        dlg.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_QR && resultCode == RESULT_QR) {
            if (data != null) {
                Bundle bundle = data.getExtras();
                int status = bundle.getInt("status");
                String result = bundle.getString("result");
                showQrResultDlg(result);
            }
        }
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
                if (!checkMobileVersion(site, site.apiVersionRange)) {
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
        saveEnterSchool(site.name, loginTime, "登录账号：未登录", app.domain);
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
                Token token = parseJsonValue(response, new TypeToken<Token>() {
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
            ViewHolder holder = null;
            Map map = (Map) getItem(position);
            if (convertView == null) {

                holder = new ViewHolder();


                convertView = LayoutInflater.from(context).inflate(R.layout.activity_net_school_listviewitem, null);
                holder.enterbtn = (Button) convertView.findViewById(R.id.enter_btn);
                holder.entertimetv = (TextView) convertView.findViewById(R.id.login_time);
                holder.loginnametv = (TextView) convertView.findViewById(R.id.login_name);
                holder.labletv = (TextView) convertView.findViewById(R.id.net_school_lable);
                holder.schoolnametv = (TextView) convertView.findViewById(R.id.net_school_name);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }


            holder.entertimetv.setText((String) mList.get(position).get("entertime"));
            holder.labletv.setText((String) mList.get(position).get("lable"));
            if (position == 0) {
                holder.labletv.setBackgroundResource(R.drawable.round_blue_bg);
            }
            if (position == 1) {
                holder.labletv.setBackgroundResource(R.drawable.round_green2_bg);
            }
            if (position == 2) {
                holder.labletv.setBackgroundResource(R.drawable.round_orange_bg);
            }
            if (position == 3) {
                holder.labletv.setBackgroundResource(R.drawable.round_blue2_bg);
            }

            holder.loginnametv.setText((String) mList.get(position).get("loginname"));
            holder.schoolnametv.setText((String) mList.get(position).get("schoolname"));
            holder.enterbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HashMap map = (HashMap) mList.get(position);
                    String schoolhost = map.get("schoolhost").toString();
                    searchSchool(schoolhost);
                }
            });
            return convertView;
        }
    }

    private final class ViewHolder {
        public TextView labletv;
        public TextView schoolnametv;
        public TextView entertimetv;
        public TextView loginnametv;
        public Button enterbtn;
    }


}

