package com.edusoho.test.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.edusoho.kuozhi.v3.model.bal.User;
import com.edusoho.test.TestEduSohoApp;
import com.google.gson.Gson;

/**
 * Created by JesseHuang on 15/8/22.
 * 初始化EduSohoApp中的信息
 */
public class TestUtils {
    public static void initApplication(TestEduSohoApp app, Context context) throws Exception {
        SharedPreferences sp = context.getSharedPreferences("config", Context.MODE_APPEND);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("showSplash", false);
        editor.putBoolean("registPublicDevice", false);
        editor.putBoolean("startWithSchool", true);
        editor.putInt("msgSound", 1);
        editor.putInt("msgVibrate", 2);
        editor.commit();
        editor.apply();

        sp = context.getSharedPreferences("defaultSchool", Context.MODE_APPEND);
        editor = sp.edit();
        editor.putString("name", "edusoho");
        editor.putString("url", "http://trymob.edusoho.cn/mapi_v2");
        editor.putString("host", "http://trymob.edusoho.cn");
        editor.putString("logo", "");
        editor.commit();
        editor.apply();

        String loginUserJson = "{\"consecutivePasswordErrorTimes\":\"0\",\"createdTime\":\"2015-07-11T15:34:24+08:00\"," +
                "\"email\":\"123324@163.com\",\"emailVerified\":\"0\",\"follower\":\"7\",\"following\":\"7\"," +
                "\"uri\":\"\",\"largeAvatar\":\"http://trymob.edusoho.cn/assets/img/default/avatar-large.png?6.4.3\"," +
                "\"lastPasswordFailTime\":\"0\",\"lockDeadline\":\"0\",\"locked\":\"0\"," +
                "\"mediumAvatar\":\"http://trymob.edusoho.cn/assets/img/default/avatar.png?6.4.3\",\"nickname\":\"jesse1\"," +
                "\"roles\":[\"ROLE_USER\"],\"setup\":\"1\",\"smallAvatar\":\"http://trymob.edusoho.cn/assets/img/default/avatar.png?6.4.3\"," +
                "\"title\":\"\",\"type\":\"default\",\"id\":268,\"dataType\":0}";
        app.gson = new Gson();
        app.loginUser = app.gson.fromJson(loginUserJson, User.class);
    }
}
