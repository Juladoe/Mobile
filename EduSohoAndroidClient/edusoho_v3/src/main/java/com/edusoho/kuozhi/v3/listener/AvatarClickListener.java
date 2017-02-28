package com.edusoho.kuozhi.v3.listener;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.util.Const;

/**
 * Created by JesseHuang on 16/1/28.
 */
public class AvatarClickListener implements View.OnClickListener {
    private Context mContext;
    private int mUserId;

    public AvatarClickListener(Context context, int userId) {
        this.mContext = context;
        this.mUserId = userId;
    }

    @Override
    public void onClick(View v) {
        EdusohoApp.app.mEngine.runNormalPlugin("WebViewActivity", mContext, new PluginRunCallback() {
            @Override
            public void setIntentDate(Intent startIntent) {
                String url = String.format(Const.MOBILE_APP_URL, EdusohoApp.app.schoolHost, String.format(Const.USER_PROFILE, mUserId));
                startIntent.putExtra(Const.WEB_URL, url);
            }
        });
    }
}
