package com.edusoho.kuozhi.v3.listener;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import com.android.volley.VolleyError;
import com.edusoho.kuozhi.v3.entity.lesson.PluginViewItem;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;

/**
 * Created by howzhi on 15/11/2.
 */

public abstract class BaseLessonPluginCallback implements LessonPluginCallback, NormalCallback<VolleyError>
{
    protected Context mContext;
    private PluginViewItem mItem;

    public BaseLessonPluginCallback(Context context)
    {
        this.mContext = context;
    }

    @Override
    public boolean click(View view) {
        return mItem.status != PluginViewItem.UNENABLE;
    }

    @Override
    public void initState(PluginViewItem item) {
    }

    @Override
    public void initPlugin(PluginViewItem item) {
        this.mItem = item;
        item.status = PluginViewItem.LOAD;
        loadPlugin(item.bundle);
    }

    protected abstract RequestUrl getRequestUrl(int lessonId);

    protected abstract void loadPlugin(Bundle bundle);

    protected void setViewStatus(int state) {
        mItem.setStatus(state);
    }

    protected void setViewLearnState(boolean isLearn) {
        if (mItem.status == PluginViewItem.ENABLE || mItem.status == PluginViewItem.NEW) {
            mItem.setStatus(isLearn ? PluginViewItem.ENABLE : PluginViewItem.NEW);
            return;
        }
    }

    @Override
    public void success(VolleyError obj) {
        setViewStatus(PluginViewItem.UNENABLE);
    }
}