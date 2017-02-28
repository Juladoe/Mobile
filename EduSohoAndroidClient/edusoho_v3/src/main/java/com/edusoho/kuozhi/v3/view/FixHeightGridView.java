package com.edusoho.kuozhi.v3.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * Created by howzhi on 14-10-8.
 */
public class FixHeightGridView extends GridView {

    public FixHeightGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FixHeightGridView(Context context) {
        super(context);
    }

    public FixHeightGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int expandSpec = MeasureSpec.makeMeasureSpec(
                Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
