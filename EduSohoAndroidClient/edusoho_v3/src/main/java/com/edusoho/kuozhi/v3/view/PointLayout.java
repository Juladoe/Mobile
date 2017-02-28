package com.edusoho.kuozhi.v3.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.edusoho.kuozhi.R;

/**
 * Created by howzhi on 14-10-14.
 */
public class PointLayout extends LinearLayout {

    protected int mPointNormalSrc = R.drawable.viewpager_point_normal;
    protected int mPointSellSrc = R.drawable.viewpager_point_sel;

    protected int mCount;
    protected int mPadding = 5;

    protected Context mContext;
    protected ViewPager mViewPager;

    public PointLayout(Context context) {
        super(context);
        mContext = context;
    }

    public PointLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public void setViewPaper(ViewPager viewPaper) {
        mViewPager = viewPaper;
    }

    public void addPointImages(int count) {
        mCount = count;
        for (int i = 0; i < count; i++) {
            ImageView imageView = new ImageView(mContext);
            addView(imageView);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) imageView.getLayoutParams();
            layoutParams.bottomMargin = mPadding;
            layoutParams.topMargin = mPadding;
            layoutParams.leftMargin = mPadding;
            layoutParams.rightMargin = mPadding;
            imageView.setLayoutParams(layoutParams);
        }
        refresh();
    }

    public void refresh() {
        for (int i = 0; i < mCount; i++) {
            ImageView imageView = (ImageView) getChildAt(i);
            if (i == mViewPager.getCurrentItem()) {
                imageView.setImageResource(mPointSellSrc);
                continue;
            }
            imageView.setImageResource(mPointNormalSrc);
        }
    }
}
