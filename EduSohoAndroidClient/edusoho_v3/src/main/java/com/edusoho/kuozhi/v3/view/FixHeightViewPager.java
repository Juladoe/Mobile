package com.edusoho.kuozhi.v3.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import java.util.ArrayList;

/**
 * Created by howzhi on 14/12/3.
 */
public class FixHeightViewPager extends ViewPager {

    private boolean isMeasure;
    private ArrayList<Integer> childHeightList;
    private GestureDetector mGestureDetector;
    private TouchCallback mTouchCallback = new TouchCallback() {
        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            return false;
        }
    };

    public FixHeightViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        childHeightList = new ArrayList<Integer>();
    }

    public FixHeightViewPager(Context context) {
        super(context);
        childHeightList = new ArrayList<Integer>();
    }

    public void setInterceptTouchCallback(TouchCallback touchCallback) {
        mTouchCallback = touchCallback;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (isMeasure) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        /*
        int height = 0;
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            child.measure(0, 0);
            int h = child.getMeasuredHeight();
            childHeightList.add(i, h);
            if (h > height)
                height = h;
        }

        Log.d(null, "height-> " + height);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        */
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setIsMeasure(boolean isMeasure) {
        this.isMeasure = isMeasure;
    }

    public int getChildHeight(int i) {
        Integer height = childHeightList.get(i);
        return height == null ? 0 : height;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mTouchCallback.onTouchEvent(ev)) {
            return false;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mTouchCallback.onTouchEvent(ev)) {
            return false;
        }
        return super.onInterceptTouchEvent(ev);
    }

    public interface TouchCallback {
        boolean onTouchEvent(MotionEvent ev);
    }
}
