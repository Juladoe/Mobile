package com.edusoho.kuozhi.v3.view;

import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by bhy on 14-7-18.
 */
public class EduSohoAnimWrap {
    private View mTarget;

    public EduSohoAnimWrap(View target) {
        this.mTarget = target;
    }

    public int getWidth() {
        return mTarget.getLayoutParams().width;
    }

    public void setWidth(int width) {
        mTarget.getLayoutParams().width = width;
        mTarget.requestLayout();
    }

    public int getHeight() {
        return mTarget.getLayoutParams().height;
    }

    public void setHeight(int height) {
        mTarget.getLayoutParams().height = height;
        mTarget.requestLayout();
    }

    public void setBackground(int color) {
        mTarget.setBackgroundColor(color);
    }

    public float getScaleX() {
        return mTarget.getScaleX();
    }

    public void setScaleX(float x) {
        mTarget.setScaleX(x);
    }

    public void setScaleY(float y) {
        mTarget.setScaleY(y);
    }

    public float getScaleY() {
        return mTarget.getScaleY();
    }

    public int getLeftMargin() {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mTarget.getLayoutParams();
        return layoutParams.leftMargin;
    }

    public void setLeftMargin(int leftMargin) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mTarget.getLayoutParams();
        layoutParams.leftMargin = leftMargin;
        mTarget.requestLayout();
    }

    public int getRightMargin() {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mTarget.getLayoutParams();
        return layoutParams.rightMargin;
    }

    public void setRightMargin(int rightMargin) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mTarget.getLayoutParams();
        layoutParams.rightMargin = rightMargin;
        mTarget.requestLayout();
    }

    public int getBackground() {
        return mTarget.getDrawingCacheBackgroundColor();
    }

    public int getPaddingTop() {
        return mTarget.getPaddingTop();
    }

    public void setPaddingTop(int top) {
        mTarget.setPadding(mTarget.getPaddingLeft(), top, mTarget.getPaddingRight(), mTarget.getPaddingBottom());
    }

    public void setPaddingLeft(int left) {
        mTarget.setPadding(left, mTarget.getPaddingTop(), mTarget.getPaddingRight(), mTarget.getPaddingBottom());
    }

}
