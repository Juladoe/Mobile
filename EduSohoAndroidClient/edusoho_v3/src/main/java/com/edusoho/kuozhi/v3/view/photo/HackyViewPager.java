package com.edusoho.kuozhi.v3.view.photo;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewParent;

/**
 * Hacky fix for Issue #4 and
 * http://code.google.com/p/android/issues/detail?id=18990
 * <p/>
 * ScaleGestureDetector seems to mess up the touch events, which means that
 * ViewGroups which make use of onInterceptTouchEvent throw a lot of
 * IllegalArgumentException: pointerIndex out of range.
 * <p/>
 * There's not much I can do in my code for now, but we can mask the result by
 * just catching the problem and ignoring it.
 *
 * @author Chris Banes
 */
public class HackyViewPager extends ViewPager {

    private int isDown = 1;
    private float mLastMotionX;
    private float mLastMotionY;
    private GestureDetector mGestureDetector;
    private OnClickListener mViewPagerClickListener;


    public HackyViewPager(Context context) {
        super(context);
    }

    public HackyViewPager(Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
    }

    protected void initView() {
        mGestureDetector = new GestureDetector(null);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        ViewParent parent = getParent();
        if (e.getAction() != MotionEvent.ACTION_UP) {
            if (parent != null) {
                parent.requestDisallowInterceptTouchEvent(true);
            }
        }
        return super.dispatchTouchEvent(e);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        ViewParent parent = getParent();
        if (e.getAction() != MotionEvent.ACTION_UP) {
            if (parent != null) {
                parent.requestDisallowInterceptTouchEvent(true);
            }
        }
        return super.onInterceptTouchEvent(e);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        ViewParent parent = getParent();
        if (ev.getAction() != MotionEvent.ACTION_UP) {
            if (parent != null) {
                parent.requestDisallowInterceptTouchEvent(true);
            }
        }
        return super.onTouchEvent(ev);
    }

    public boolean dispatchTouchEvent2(MotionEvent ev) {
        /*
        ViewParent parent = getParent();
        final float x = ev.getX();
        final float y = ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                parent.requestDisallowInterceptTouchEvent(true);
                isDown = 1;
                mLastMotionX = x;
                mLastMotionY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                if (isDown == 1) {
                    if (x - mLastMotionX > 5 && getCurrentItem() == 0) {
                        isDown = 0;
                        parent.requestDisallowInterceptTouchEvent(false);
                        break;
                    }

                    if (x - mLastMotionX < -5 && getCurrentItem() == getAdapter().getCount() - 1) {
                        isDown = 0;
                        parent.requestDisallowInterceptTouchEvent(false);
                        break;
                    }

                    float delayY = y - mLastMotionY;
                    float delayX = x - mLastMotionX;
                    if (Math.abs(delayY) > Math.abs(delayX)) {
                        parent.requestDisallowInterceptTouchEvent(false);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                float delayY = y - mLastMotionY;
                float delayX = x - mLastMotionX;
                if (Math.abs(delayX) < 100 && Math.abs(delayY) < 100 && mViewPagerClickListener != null) {
                    mViewPagerClickListener.onClick(this);
                }
            case MotionEvent.ACTION_CANCEL:
                parent.requestDisallowInterceptTouchEvent(false);
                break;
        }
        */
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        mViewPagerClickListener = l;
    }
}
