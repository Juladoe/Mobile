package com.edusoho.kuozhi.v3.plugin.appview;

import android.app.Activity;
import android.os.Bundle;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.core.CoreEngine;
import com.edusoho.kuozhi.v3.model.bal.User;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.ui.ImChatActivity;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by 菊 on 2016/4/11.
 */
public class CourseConsultAction {

    private Activity mActivity;

    public CourseConsultAction(Activity activity)
    {
        this.mActivity = activity;
    }

    public void invoke(final Bundle bundle) {
        final EdusohoApp app = (EdusohoApp) mActivity.getApplication();
        RequestUrl requestUrl = app.bindUrl(Const.USERINFO, false);
        Map<String, String> params = requestUrl.getParams();
        params.put("userId", bundle.getString("userId"));
        app.postUrl(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                User user = app.parseJsonValue(response, new TypeToken<User>() {
                });
                if (user != null) {
                    bundle.putString(ImChatActivity.FROM_NAME, user.nickname);
                    bundle.putInt(ImChatActivity.FROM_ID, user.id);
                    bundle.putString(ImChatActivity.HEAD_IMAGE_URL, user.mediumAvatar);
                    CoreEngine.create(mActivity.getBaseContext()).runNormalPluginWithBundle("ImChatActivity", mActivity, bundle);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CommonUtil.shortToast(mActivity.getBaseContext(), "无法获取教师信息");
            }
        });
    }
}
