package com.edusoho.kuozhi.v3.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.edusoho.kuozhi.R;

/**
 * Created by JesseHuang on 16/1/29.
 */
public class EduSohoItemTitle extends RelativeLayout {
    private TextView mTitle;
    private TextView mRole;

    public EduSohoItemTitle(Context context) {
        super(context);
        initView(context);
    }

    public EduSohoItemTitle(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        RelativeLayout.LayoutParams titleLayout = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        mTitle = new TextView(context);
        mTitle.setId(R.id.expand_collapse);
        mTitle.setEllipsize(TextUtils.TruncateAt.END);
        mTitle.setLines(1);
        mTitle.setMaxEms(12);
        mTitle.setSingleLine(true);
        mTitle.setTextColor(getResources().getColor(R.color.base_black_87));
        mTitle.setTextSize(getResources().getDimension(R.dimen.medium_font_size));
        addView(mTitle, titleLayout);


        mRole = new TextView(context);

    }
}
