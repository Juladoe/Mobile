package com.edusoho.test;

import android.content.Intent;
import android.test.UiThreadTest;
import android.view.View;

import com.edusoho.kuozhi.v3.ui.NetSchoolActivity;
import com.edusoho.kuozhi.v3.view.EdusohoAutoCompleteTextView;
import com.edusoho.test.base.BaseActivityUnitTestCase;

/**
 * Created by JesseHuang on 15/8/24.
 */
public class NetSchoolActivityTest extends BaseActivityUnitTestCase<NetSchoolActivity> {
    public NetSchoolActivityTest() {
        super(NetSchoolActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mLaunchIntent = new Intent(mInstrumentation.getTargetContext(),
                NetSchoolActivity.class);
    }

    @UiThreadTest
    public void testNetSchoolActivity() {
        NetSchoolActivity mActivity = getActivity();
        assertNotNull(mActivity);
    }

    @UiThreadTest
    public void testLayout() {
        NetSchoolActivity mActivity = getActivity();
        View mSearchBtn = mActivity.findViewById(R.id.normal_search_btn);
        assertNotNull(mSearchBtn);
        EdusohoAutoCompleteTextView mSearchEdt = (EdusohoAutoCompleteTextView) mActivity.findViewById(R.id.school_url_edit);
        assertNotNull(mSearchEdt);

    }
}
