package com.edusoho.test;

import android.content.Intent;
import android.test.UiThreadTest;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.edusoho.kuozhi.v3.ui.SuggestionActivity;
import com.edusoho.test.base.BaseActivityUnitTestCase;

/**
 * Created by JesseHuang on 15/8/24.
 */
public class SuggestionActivityTest extends BaseActivityUnitTestCase<SuggestionActivity> {
    public SuggestionActivityTest() {
        super(SuggestionActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mLaunchIntent = new Intent(mInstrumentation.getTargetContext(),
                SuggestionActivity.class);
    }

    @UiThreadTest
    public void testSuggestionActivity() {
        SuggestionActivity mActivity = getActivity();
        assertNotNull(mActivity);
    }

    @UiThreadTest
    public void testSuggestionActivityLayout() {
        SuggestionActivity mActivity = getActivity();
        EditText mInfoEdt = (EditText) mActivity.findViewById(R.id.suggestion_info_edt);
        assertNotNull(mInfoEdt);
        EditText mContactEdt = (EditText) mActivity.findViewById(R.id.suggestion_contact_edt);
        assertNotNull(mContactEdt);
        RadioGroup mFixRadioGroup = (RadioGroup) mActivity.findViewById(R.id.suggestion_fix_group);
        assertNotNull(mFixRadioGroup);
        assertEquals(2, mFixRadioGroup.getChildCount());

        String[] radioButtonTexts = {"客户端有BUG", "功能有缺失"};
        for (int i = 0; i < mFixRadioGroup.getChildCount(); i++) {
            RadioButton radioButton = (RadioButton) mFixRadioGroup.getChildAt(i);
            assertEquals(radioButtonTexts[i], radioButton.getText().toString());
        }

        View mSubmitBtn = mActivity.findViewById(R.id.suggestion_submit);
        assertNotNull(mSubmitBtn);
    }
}
