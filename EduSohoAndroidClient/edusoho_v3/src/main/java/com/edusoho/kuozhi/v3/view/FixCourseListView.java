package com.edusoho.kuozhi.v3.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by DF on 2016/12/22.
 */

public class FixCourseListView extends ListView {

    public FixCourseListView(Context context) {
        super(context);
    }

    public FixCourseListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FixCourseListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec
                ,MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE>>2,MeasureSpec.AT_MOST));
    }
}
