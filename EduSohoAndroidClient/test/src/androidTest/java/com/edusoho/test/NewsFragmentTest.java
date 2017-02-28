package com.edusoho.test;

import android.support.v4.app.Fragment;
import android.test.UiThreadTest;

import com.edusoho.kuozhi.v3.ui.fragment.NewsFragment;
import com.edusoho.kuozhi.v3.view.swipemenulistview.SwipeMenuListView;
import com.edusoho.test.base.BaseFragmentTestCase;

import junit.framework.Assert;

/**
 * Created by JesseHuang on 15/8/19.
 */
public class NewsFragmentTest extends BaseFragmentTestCase<NewsFragment> {

    public NewsFragmentTest() {
        super(NewsFragment.class);
    }

    @UiThreadTest
    public void testNewsFragment() {
        Fragment mFragment = getFragment();
        assertNotNull(mFragment);
    }

    @UiThreadTest
    public void testNewsFragmentLayout() {
        SwipeMenuListView swipeMenuListView = (SwipeMenuListView) getFragment().getView().findViewById(R.id.lv_news_list);
        Assert.assertNotNull(swipeMenuListView);
    }

}
