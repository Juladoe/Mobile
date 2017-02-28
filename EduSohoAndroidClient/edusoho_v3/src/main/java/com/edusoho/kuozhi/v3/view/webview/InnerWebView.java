package com.edusoho.kuozhi.v3.view.webview;

import android.content.Context;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.webkit.WebView;

/**
 * Created by suju on 16/12/21.
 */

public class InnerWebView extends WebView {

    private NestedScrollView.OnScrollChangeListener mOnScrollListener;

    public InnerWebView(Context context) {
        super(context, null);
    }

    public InnerWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InnerWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mOnScrollListener != null) {
            mOnScrollListener.onScrollChange(null, l, t, oldl, oldt);
        }
    }

    public void setOnScrollListener(NestedScrollView.OnScrollChangeListener onScrollListener) {
        this.mOnScrollListener = onScrollListener;
    }
}
