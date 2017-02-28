package com.edusoho.kuozhi.v3.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * Created by howzhi on 14-10-8.
 */
public class FixHeightScrollView extends ScrollView {

    public FixHeightScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FixHeightScrollView(Context context) {
        super(context);
    }

    public FixHeightScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int expandSpec = MeasureSpec.makeMeasureSpec(
                Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
