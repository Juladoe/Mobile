package com.edusoho.kuozhi.v3.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.edusoho.kuozhi.R;

/**
 * Created by suju on 16/12/20.
 */

public class InnerLoadView extends FrameLayout {

    public InnerLoadView(Context context) {
        super(context);
        initView();
    }

    public InnerLoadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        LayoutParams lp = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        lp.gravity = Gravity.CENTER;
        addView(LayoutInflater.from(getContext()).inflate(R.layout.load_dig_layout, null), lp);
    }
}
