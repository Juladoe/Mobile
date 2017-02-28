package com.edusoho.kuozhi.v3.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import in.srain.cube.views.ptr.PtrClassicFrameLayout;

/**
 * Created by su on 2016/2/22.
 */
public class FixPtrClassicFrameLayout extends PtrClassicFrameLayout {

    private GestureDetector mDetector;
    private boolean disallowInterceptTouchEvent = false;

    public FixPtrClassicFrameLayout(Context context) {
        super(context);
        initDetestor();
    }

    public FixPtrClassicFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initDetestor();
    }

    public FixPtrClassicFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initDetestor();
    }

    private void initDetestor() {
        mDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return Math.abs(1.5 * distanceY) < Math.abs(distanceX);
            }
        });
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        disallowInterceptTouchEvent = disallowIntercept;
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        if (mDetector.onTouchEvent(e) && disallowInterceptTouchEvent) {
            return dispatchTouchEventSupper(e);
        }
        return super.dispatchTouchEvent(e);
    }
}
