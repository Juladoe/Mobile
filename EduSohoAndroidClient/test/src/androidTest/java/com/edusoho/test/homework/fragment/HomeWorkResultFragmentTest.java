package com.edusoho.test.homework.fragment;

import android.support.v4.app.Fragment;
import android.test.UiThreadTest;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.edusoho.kuozhi.homework.ui.fragment.HomeWorkResultFragment;
import com.edusoho.test.base.BaseFragmentTestCase;

/**
 * Created by Melomelon on 2015/11/13.
 */
public class HomeWorkResultFragmentTest extends BaseFragmentTestCase<HomeWorkResultFragment> {

    public HomeWorkResultFragmentTest() {
        super(HomeWorkResultFragment.class);
    }

    @UiThreadTest
    public void testGetFragment() {
        Fragment mFragment = getFragment();
        assertNotNull(mFragment);
    }

    @UiThreadTest
    public void testViewLayout() {
        View layoutView = getFragment().getView();
        View mResultBtnLayout = layoutView.findViewById(com.edusoho.kuozhi.homework.R.id.hw_result_btn_layout);
        assertNotNull(mResultBtnLayout);
        ListView mResultListView = (ListView) layoutView.findViewById(com.edusoho.kuozhi.homework.R.id.hw_result_listview);
        assertNotNull(mResultListView);
        TextView mResultView = (TextView) layoutView.findViewById(com.edusoho.kuozhi.homework.R.id.hw_result_total);
        assertNotNull(mResultView);
        View mResultParseBtn = layoutView.findViewById(com.edusoho.kuozhi.homework.R.id.hw_result_parse);
        assertNotNull(mResultParseBtn);
        View mResultReDoBtn = layoutView.findViewById(com.edusoho.kuozhi.homework.R.id.hw_result_redo);
        assertNotNull(mResultReDoBtn);
    }
}
