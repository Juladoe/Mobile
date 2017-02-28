package com.edusoho.kuozhi.imserver.ui.view;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Created by suju on 16/9/5.
 */
public class ChatListView extends ListView {

    private Context mContext;

    public ChatListView(Context context) {
        super(context);
        mContext = context;
        initView();
    }

    public ChatListView(Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
    }

    private void initView() {
    }

}
