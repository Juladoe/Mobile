package com.edusoho.kuozhi.shard;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

/**
 * Created by onewoman on 14-11-6.
 */
public class ListData {
    public Drawable icon;
    //中文
    public String text;
    //平台的英文
    public String type;
    private Context mContext;

    public ListData() {

    }

    public ListData(Drawable icon, String type, Context context) {
        this.icon = icon;
        this.type = type;
        mContext = context;
        Resources resources = mContext.getResources();
        text = resources.getString(resources.getIdentifier(
                type, "string", mContext.getPackageName()));
    }

}
