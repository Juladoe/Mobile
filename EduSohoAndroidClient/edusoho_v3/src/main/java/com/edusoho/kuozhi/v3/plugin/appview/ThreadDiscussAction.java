package com.edusoho.kuozhi.v3.plugin.appview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.edusoho.kuozhi.v3.core.CoreEngine;
import com.edusoho.kuozhi.v3.listener.PluginRunCallback;
import com.edusoho.kuozhi.v3.ui.ThreadDiscussChatActivity;
import com.edusoho.kuozhi.v3.util.AppUtil;

/**
 * Created by 菊 on 2016/4/11.
 */
public class ThreadDiscussAction {

    private Activity mActivity;

    public ThreadDiscussAction(Activity activity)
    {
        this.mActivity = activity;
    }

    public void invoke(final Bundle bundle) {
        CoreEngine.create(mActivity).runNormalPlugin("ThreadDiscussActivity", mActivity.getBaseContext(), new PluginRunCallback() {
            @Override
            public void setIntentDate(Intent startIntent) {
                startIntent.putExtra(ThreadDiscussChatActivity.THREAD_TARGET_ID, AppUtil.parseInt(bundle.getString("targetId")));
                startIntent.putExtra(ThreadDiscussChatActivity.THREAD_TARGET_TYPE, bundle.getString("targetType"));
                startIntent.putExtra(ThreadDiscussChatActivity.FROM_ID, AppUtil.parseInt(bundle.getString("threadId")));
                startIntent.putExtra(ThreadDiscussChatActivity.THREAD_TYPE, bundle.getString(ThreadDiscussChatActivity.THREAD_TYPE));
            }
        });
    }
}
