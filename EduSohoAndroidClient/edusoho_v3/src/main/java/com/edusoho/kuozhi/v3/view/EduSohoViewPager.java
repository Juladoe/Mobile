package com.edusoho.kuozhi.v3.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by melomelon on 16/2/29.
 */
public class EduSohoViewPager extends ViewPager {

    private boolean isPagingEnable = true;

    public EduSohoViewPager(Context context) {
        super(context);
    }

    public EduSohoViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return this.isPagingEnable && super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return this.isPagingEnable && super.onInterceptTouchEvent(ev);
    }

    public void setIsPagingEnable(boolean isPagingEnable) {
        this.isPagingEnable = isPagingEnable;
    }

}
