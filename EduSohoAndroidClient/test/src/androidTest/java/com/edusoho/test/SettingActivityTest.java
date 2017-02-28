package com.edusoho.test;

import android.content.Intent;
import android.test.UiThreadTest;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.edusoho.kuozhi.v3.ui.SettingActivity;
import com.edusoho.kuozhi.v3.view.CleanCacheTextView;
import com.edusoho.test.base.BaseActivityUnitTestCase;

/**
 * Created by JesseHuang on 15/8/24.
 */
public class SettingActivityTest extends BaseActivityUnitTestCase<SettingActivity> {
    public SettingActivityTest() {
        super(SettingActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mLaunchIntent = new Intent(mInstrumentation.getTargetContext(),
                SettingActivity.class);
    }

    @UiThreadTest
    public void testSettingActivity() {
        SettingActivity mActivity = getActivity();
        assertNotNull(mActivity);
    }

    @UiThreadTest
    public void testLayout() {
        SettingActivity mActivity = getActivity();
        View viewScan = mActivity.findViewById(R.id.linear_scan);
        assertNotNull(viewScan);
        TextView tvScan = (TextView) mActivity.findViewById(R.id.tv_scan);
        assertNotNull(tvScan);
        assertEquals(mActivity.getResources().getString(R.string.setting_scan), tvScan.getText());

        TextView tvMsgNotify = (TextView) mActivity.findViewById(R.id.tvMsgNotify);
        assertNotNull(tvMsgNotify);
        assertEquals(mActivity.getResources().getString(R.string.setting_new_msg_notify), tvMsgNotify.getText());

        TextView tvOnlineDuration = (TextView) mActivity.findViewById(R.id.tvOnlineDuration);
        assertNotNull(tvOnlineDuration);
        assertEquals(mActivity.getResources().getString(R.string.setting_online_duration), tvOnlineDuration.getText());

        CheckBox cbOfflineType = (CheckBox) mActivity.findViewById(R.id.cb_offline_type);
        assertNotNull(cbOfflineType);
        assertEquals(false, cbOfflineType.isChecked());

        CleanCacheTextView tvCleanCache = (CleanCacheTextView) mActivity.findViewById(R.id.tv_clean_cache);
        assertNotNull(tvCleanCache);

        TextView tvAbout = (TextView) mActivity.findViewById(R.id.tv_about);
        assertNotNull(tvAbout);

        Button btnLogout = (Button) mActivity.findViewById(R.id.setting_logout_btn);
        assertNotNull(btnLogout);
    }
}
