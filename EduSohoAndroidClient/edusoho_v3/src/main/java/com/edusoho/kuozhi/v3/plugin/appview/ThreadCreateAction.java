package com.edusoho.kuozhi.v3.plugin.appview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.edusoho.kuozhi.v3.core.CoreEngine;
import com.edusoho.kuozhi.v3.listener.PluginRunCallback;
import com.edusoho.kuozhi.v3.ui.ThreadCreateActivity;
import com.edusoho.kuozhi.v3.util.AppUtil;

/**
 * Created by 菊 on 2016/4/11.
 */
public class ThreadCreateAction {

    private Activity mActivity;

    public ThreadCreateAction(Activity activity)
    {
        this.mActivity = activity;
    }

    public void invoke(final Bundle bundle) {
        CoreEngine.create(mActivity).runNormalPlugin("ThreadCreateActivity", mActivity.getBaseContext(), new PluginRunCallback() {
            @Override
            public void setIntentDate(Intent startIntent) {
                startIntent.putExtra(ThreadCreateActivity.TARGET_ID, AppUtil.parseInt(bundle.getString("targetId")));
                startIntent.putExtra(ThreadCreateActivity.TARGET_TYPE, bundle.getString("targetType"));
                startIntent.putExtra(ThreadCreateActivity.THREAD_TYPE, bundle.getString("threadType"));
                startIntent.putExtra(ThreadCreateActivity.TYPE, bundle.getString("type"));
            }
        });
    }
}
