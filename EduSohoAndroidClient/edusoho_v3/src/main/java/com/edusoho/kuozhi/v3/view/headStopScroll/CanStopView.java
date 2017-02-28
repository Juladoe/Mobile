package com.edusoho.kuozhi.v3.view.headStopScroll;

/**
 * Created by remilia on 2017/1/6.
 */
public interface CanStopView {
    int getFirstViewHeight();
    boolean isStay();
    void scrollTo(int x, int y);
    void scrollBy(int x, int y);
    int getScrollY();
    void smoothScrollTo(int x, int y);
    void smoothScrollBy(int x, int y);

}
