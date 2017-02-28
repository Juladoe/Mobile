package com.edusoho.kuozhi.v3.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * Created by JesseHuang on 15/10/28.
 */
public class EduSohoGridView extends GridView {
    public EduSohoGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EduSohoGridView(Context context) {
        super(context);
    }

    public EduSohoGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
