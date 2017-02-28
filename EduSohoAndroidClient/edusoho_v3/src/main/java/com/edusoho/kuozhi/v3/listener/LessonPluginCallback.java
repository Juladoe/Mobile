package com.edusoho.kuozhi.v3.listener;

import android.view.View;

import com.edusoho.kuozhi.v3.entity.lesson.PluginViewItem;

/**
 * Created by howzhi on 15/11/2.
 */
public interface LessonPluginCallback {

    void initPlugin(PluginViewItem item);

    void initState(PluginViewItem item);

    boolean click(View view);
}
