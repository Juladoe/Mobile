package com.edusoho.kuozhi.v3.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import com.android.volley.Response;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.shard.ThirdPartyLogin;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.model.bal.User;
import com.edusoho.kuozhi.v3.model.provider.IMServiceProvider;
import com.edusoho.kuozhi.v3.model.result.UserResult;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.ui.CompletePhoneActivity;
import com.edusoho.kuozhi.v3.ui.base.BaseActivity;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;

import static com.edusoho.kuozhi.v3.EdusohoApp.app;

/**
 * Created by howzhi on 15/7/7.
 */
public class OpenLoginUtil {

    private static final String EnterSchool = "enter_school";

    private NormalCallback<UserResult> mLoginhandler = new NormalCallback<UserResult>() {
        @Override
        public void success(UserResult obj) {
        }
    };

    private Context mContext;
    private String mAuthCancel;
    private Promise mPromise;
    private String thirdPartyType;

    private OpenLoginUtil(Context context) {
        this.mContext = context;
        mAuthCancel = mContext.getResources().getString(R.string.authorize_cancelled);
    }

    public static OpenLoginUtil getUtil(Context context) {
        return new OpenLoginUtil(context);
    }

    public void setLoginHandler(NormalCallback<UserResult> callback) {
        this.mLoginhandler = callback;
    }

    public void bindOpenUser(final Activity activity, String[] params) {
        if (params == null) {
            CommonUtil.longToast(mContext, "授权失败!");
            return;
        }
        final EdusohoApp app = (EdusohoApp) activity.getApplication();
        RequestUrl requestUrl = app.bindNewUrl(Const.BIND_LOGIN, false);
        requestUrl.setParams(new String[]{
                "type", params[3],
                "id", params[0],
                "name", params[1],
                "avatar", params[2],
        });
        thirdPartyType = params.length > 4 ? params[4] : "";
        Looper.prepare();

        app.postUrl(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                UserResult userResult = app.parseJsonValue(
                        response, new TypeToken<UserResult>() {
                        });
                if (!response.contains("verifiedMobile")) {
                    app.token = userResult.token;
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("user", userResult);
                    Intent intent = new Intent(mContext, CompletePhoneActivity.class);
                    mContext.startActivity(intent.putExtras(bundle));
                }else {
                    app.saveToken(userResult);
                    app.loginUser.thirdParty = thirdPartyType;
                    app.sendMessage(Const.THIRD_PARTY_LOGIN_SUCCESS, null);
                    Bundle bundle = new Bundle();
                    bundle.putString(Const.BIND_USER_ID, String.valueOf(app.loginUser.id));
                    User user = app.loginUser;
                    new IMServiceProvider(activity.getBaseContext()).bindServer(user.id, user.nickname);
                    mLoginhandler.success(userResult);
                    SimpleDateFormat nowfmt = new SimpleDateFormat("登录时间：yyyy/MM/dd HH:mm:ss");
                    Date date = new Date();
                    String entertime = nowfmt.format(date);
                    saveEnterSchool(app.defaultSchool.name, entertime, "登录账号：" + app.loginUser.nickname, app.domain);
                }
            }
        }, null);

        Looper.loop();
    }

    public void completeInfo(BaseActivity baseActivity, UserResult userResult){
        app.saveToken(userResult);
        app.loginUser.thirdParty = thirdPartyType;
        app.sendMessage(Const.THIRD_PARTY_LOGIN_SUCCESS, null);
        Bundle bundle = new Bundle();
        bundle.putString(Const.BIND_USER_ID, String.valueOf(app.loginUser.id));
        User user = app.loginUser;
        new IMServiceProvider(baseActivity.getBaseContext()).bindServer(user.id, user.nickname);
        SimpleDateFormat nowfmt = new SimpleDateFormat("登录时间：yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String entertime = nowfmt.format(date);
        saveEnterSchool(app.defaultSchool.name, entertime, "登录账号：" + app.loginUser.nickname, app.domain);
    }



    private String[] getWeixinLoginResult(HashMap<String, Object> res) {
        String id = res.get("unionid").toString();
        String name = res.get("nickname").toString();
        String avatar = res.get("headimgurl").toString();

        return new String[]{id, name, avatar, "weixinmob", "Wechat"};
    }

    private String[] getWeiboLoginResult(HashMap<String, Object> res) {
        String id = res.get("id").toString();
        String name = res.get("name").toString();
        String avatar = res.get("avatar_large").toString();

        return new String[]{id, name, avatar, "weibo", "SinaWeibo"};
    }

    private String[] getQQLoginResult(HashMap<String, Object> res) {
        String id = res.get("id").toString();
        String name = res.get("nickname").toString();
        String avatar = res.get("figureurl_qq_2").toString();

        return new String[]{id, name, avatar, "qq", "QQ"};
    }

    public String[] bindByPlatform(String type, HashMap<String, Object> res) {
        String[] params = null;
        if ("QQ".equals(type)) {
            params = getQQLoginResult(res);
        } else if ("Wechat".equals(type)) {
            params = getWeixinLoginResult(res);
        } else if ("SinaWeibo".equals(type)) {
            params = getWeiboLoginResult(res);
        }

        return params;
    }

    private void startOpenLogin(final String type) {
        ThirdPartyLogin.getInstance(mContext).login(new PlatformActionListener() {

            @Override
            public void onComplete(Platform platform, int action, HashMap<String, Object> res) {
                if (action == Platform.ACTION_USER_INFOR) {
                    try {
                        if (!res.containsKey("id")) {
                            res.put("id", platform.getDb().getToken());
                        }
                        String[] params = bindByPlatform(type, res);
                        mPromise.resolve(params);
                    } catch (Exception ex) {
                        Log.e("ThirdPartyLogin-->", ex.getMessage());
                    }
                }
            }

            @Override
            public void onError(Platform platform, int action, Throwable throwable) {
                platform.removeAccount();
            }

            @Override
            public void onCancel(Platform platform, int action) {
                CommonUtil.longToast(mContext, mAuthCancel);
            }
        }, type);
    }

    public Promise login(String type) {
        mPromise = new Promise();
        startOpenLogin(type);

        return mPromise;
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
        if (loadEnterSchool(EnterSchool) != null) {
            list = loadEnterSchool(EnterSchool);
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

        SharedPreferences sp = mContext.getSharedPreferences("EnterSchool", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(EnterSchool, mJsonArray.toString());
        editor.commit();
    }

    private List<Map<String, Object>> loadEnterSchool(String fileName) {
        List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
        SharedPreferences sp = mContext.getSharedPreferences("EnterSchool", Context.MODE_PRIVATE);
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
}
