package com.edusoho.kuozhi.v3.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.edusoho.kuozhi.R;

/**
 * Created by JesseHuang on 15/9/6.
 */
public class EduSohoLoadingButton extends FrameLayout {

    private TextView tvLoading;
    private ImageView ivLoading;
    private Drawable mInitBackground;
    private Drawable mLoadingBackground;
    private String mInitText;
    private String mSuccessText;
    private String mLoadingText;

    public EduSohoLoadingButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public EduSohoLoadingButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    public void initView(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.view_loading_button, this);
        tvLoading = (TextView) findViewById(R.id.tv_loading);
        ivLoading = (ImageView) findViewById(R.id.iv_loading);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.EduSohoLoadingButton);

        mInitText = typedArray.getString(R.styleable.EduSohoLoadingButton_init_text);
        mLoadingText = typedArray.getString(R.styleable.EduSohoLoadingButton_loading_text);
        mSuccessText = typedArray.getString(R.styleable.EduSohoLoadingButton_success_text);

        mInitBackground = typedArray.getDrawable(R.styleable.EduSohoLoadingButton_init_background);
        mLoadingBackground = typedArray.getDrawable(R.styleable.EduSohoLoadingButton_loading_background);

        tvLoading.setText(mInitText);
        setBackgroundDrawable(mInitBackground);

    }

    public void setLoadingState() {
        tvLoading.setText(mLoadingText);
        setBackgroundDrawable(mLoadingBackground);
    }

    public void setSuccessState() {
        tvLoading.setText(mSuccessText);
        setBackgroundDrawable(mLoadingBackground);
    }

    public void setInitState() {
        tvLoading.setText(mInitText);
        setBackgroundDrawable(mInitBackground);
    }

}
