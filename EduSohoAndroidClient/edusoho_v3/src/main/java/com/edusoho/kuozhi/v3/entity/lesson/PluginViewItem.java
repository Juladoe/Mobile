package com.edusoho.kuozhi.v3.entity.lesson;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.edusoho.kuozhi.v3.listener.LessonPluginCallback;

/**
 * Created by suju on 16/12/27.
 */

public class PluginViewItem {

    public static final int LOAD = 0001;
    public static final int ENABLE = 0002;
    public static final int UNENABLE = 0003;
    public static final int NEW = 0004;

    public Drawable iconRes;
    public String title;
    public String action;
    public Bundle bundle;
    public int status = LOAD;
    public LessonPluginCallback callback;

    public void setStatus(int status) {
        this.status = status;
    }
}
