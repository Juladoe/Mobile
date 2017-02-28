package com.edusoho.test;

import android.content.Intent;
import android.test.UiThreadTest;
import android.widget.CheckBox;

import com.edusoho.kuozhi.v3.ui.MsgReminderActivity;
import com.edusoho.test.base.BaseActivityUnitTestCase;

/**
 * Created by JesseHuang on 15/8/24.
 */
public class MsgReminderActivityTest extends BaseActivityUnitTestCase<MsgReminderActivity> {
    public MsgReminderActivityTest() {
        super(MsgReminderActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mLaunchIntent = new Intent(mInstrumentation.getTargetContext(),
                MsgReminderActivity.class);
    }

    @UiThreadTest
    public void testMsgReminderActivity() {
        MsgReminderActivity mActivity = getActivity();
        assertNotNull(mActivity);
    }

    @UiThreadTest
    public void testMsgReminderActivityLayout() {
        MsgReminderActivity mActivity = getActivity();
        CheckBox cbMsgSound = (CheckBox) mActivity.findViewById(R.id.cb_msg_sound);
        assertNotNull(cbMsgSound);
        assertEquals(true, cbMsgSound.isChecked());
        CheckBox cbMsgVibrate = (CheckBox) mActivity.findViewById(R.id.cb_msg_vibrate);
        assertNotNull(cbMsgVibrate);
        assertEquals(true, cbMsgSound.isChecked());
    }
}
