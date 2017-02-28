package com.edusoho.kuozhi.v3.view.test;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.model.bal.test.TestpaperItem;

import java.util.ArrayList;

/**
 * Created by howzhi on 14-9-23.
 */
public class MuiltTextView extends TextView {

    private Context mContext;
    private String mTitle;
    private ArrayList<String> mContent;
    private int mTitleColor;
    private int mContentColor;
    private boolean isFirstPadding;

    public MuiltTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init(attrs);
    }

    public MuiltTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init(attrs);
    }

    private void init(AttributeSet attrs)
    {
        TypedArray ta = mContext.obtainStyledAttributes(attrs, R.styleable.MuiltTextView);
        mTitle = ta.getString(R.styleable.MuiltTextView_muiltTitle);
        mTitleColor = ta.getColor(R.styleable.MuiltTextView_muiltColor, Color.BLACK);
        isFirstPadding = ta.getBoolean(R.styleable.MuiltTextView_isFirstPadding, false);
        mContentColor = ta.getColor(R.styleable.MuiltTextView_contentColor, Color.GRAY);

        mContent = new ArrayList<String>();
    }

    public void setContent(TestpaperItem testpaperItem)
    {
        mTitle = testpaperItem.title;
        isFirstPadding = testpaperItem.isFirstPadding;
        setContent(testpaperItem.content);
    }

    public void setContent(String[] contents)
    {
        mContent.clear();
        for (String content : contents) {
            mContent.add(content);
        }

        int contentStart = mTitle.length();
        SpannableStringBuilder spannable = null;
        if (!TextUtils.isEmpty(mTitle)) {
            spannable = new SpannableStringBuilder(mTitle);
            spannable.setSpan(
                    new ForegroundColorSpan(mTitleColor), 0, contentStart, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            spannable.append("\n\n");
            contentStart ++;
        }

        spannable.append(isFirstPadding ? "        " : "");
        spannable.append(arrayToContent(contents));
        spannable.setSpan(
                new ForegroundColorSpan(mContentColor), contentStart, spannable.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

        setText(spannable);
    }

    private String arrayToContent(String[] contents)
    {
        StringBuffer stringBuffer = new StringBuffer();
        for (String str : contents) {
            stringBuffer.append(str).append("\n\n");
        }

        if (stringBuffer.length() > 2) {
            stringBuffer.delete(stringBuffer.length() - 2, stringBuffer.length());
        }
        return stringBuffer.toString();
    }

    public String getContent()
    {
        StringBuffer stringBuffer = new StringBuffer();
        for (String str : mContent) {
            stringBuffer.append(str).append("\n\n");
        }
        if (stringBuffer.length() > 2) {
            stringBuffer.delete(stringBuffer.length() - 2, stringBuffer.length());
        }
        return stringBuffer.toString();
    }

    public void append(String text)
    {
        mContent.add(text);
    }
}
