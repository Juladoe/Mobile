package com.edusoho.kuozhi.v3.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

import com.edusoho.kuozhi.v3.EdusohoApp;


/**
 * Created by howzhi on 14-10-8.
 */
public class FixHeightListView extends ListView {

    public FixHeightListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FixHeightListView(Context context) {
        super(context);
    }

    public FixHeightListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(
                EdusohoApp.screenH, MeasureSpec.AT_MOST);
        measureChildren(widthMeasureSpec, expandSpec);

        View v = getChildAt(getChildCount() - 1);
        if (v != null) {
            expandSpec = MeasureSpec.makeMeasureSpec(
                    getChildTotalHeight(), MeasureSpec.AT_MOST);
        }

        super.onMeasure(widthMeasureSpec, expandSpec);
    }

    private int getChildTotalHeight() {
        int total = 0;
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View v = getChildAt(i);
            total += v.getHeight();
        }

        return total;
    }
}
