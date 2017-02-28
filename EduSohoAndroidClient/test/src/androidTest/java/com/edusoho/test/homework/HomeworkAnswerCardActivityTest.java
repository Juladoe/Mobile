package com.edusoho.test.homework;

import android.content.Intent;
import android.test.UiThreadTest;
import android.widget.Button;
import android.widget.LinearLayout;

import com.edusoho.kuozhi.homework.HomeworkAnswerCardActivity;
import com.edusoho.kuozhi.v3.view.EduSohoButton;
import com.edusoho.test.base.BaseActivityUnitTestCase;

/**
 * Created by Melomelon on 2015/11/12.
 */
public class HomeworkAnswerCardActivityTest extends BaseActivityUnitTestCase<HomeworkAnswerCardActivity> {

    public HomeworkAnswerCardActivityTest() {
        super(HomeworkAnswerCardActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mLaunchIntent = new Intent(mInstrumentation.getTargetContext(),
                HomeworkAnswerCardActivity.class);
    }

    @UiThreadTest
    public void testGetActivity() {
        HomeworkAnswerCardActivity mActivity = getActivity();
        assertNotNull(mActivity);
    }

    @UiThreadTest
    public void testViewLayout(){
        HomeworkAnswerCardActivity mActivity = getActivity();
        EduSohoButton submitBtn = (EduSohoButton) mActivity.findViewById(com.edusoho.kuozhi.homework.R.id.homework_submit_btn);
        assertNotNull(submitBtn);
        LinearLayout layout = (LinearLayout) mActivity.findViewById(com.edusoho.kuozhi.homework.R.id.homework_answer_card_layout);
        assertNotNull(layout);
    }
}
