package com.edusoho.test;

import android.app.Instrumentation;
import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.test.UiThreadTest;
import android.view.ContextThemeWrapper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TabWidget;

import com.edusoho.kuozhi.v3.ui.RegisterActivity;
import com.edusoho.kuozhi.v3.view.EduSohoLoadingButton;

/**
 * Created by JesseHuang on 15/8/23.
 */
public class RegisterActivityTest extends ActivityUnitTestCase<RegisterActivity> {

    private RegisterActivity mActivity;
    protected Instrumentation mInstrumentation;
    protected Intent mLaunchIntent;

    public RegisterActivityTest() {
        super(RegisterActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mInstrumentation = getInstrumentation();
        ContextThemeWrapper context = new ContextThemeWrapper(
                mInstrumentation.getTargetContext(), R.style.Theme_AppCompat);
        setActivityContext(context);

        TestEduSohoApp app = (TestEduSohoApp) mInstrumentation.newApplication(
                getClass().getClassLoader(), TestEduSohoApp.class.getName(), context);
        mInstrumentation.callApplicationOnCreate(app);
        setApplication(app);
        mLaunchIntent = new Intent(mInstrumentation.getTargetContext(),
                RegisterActivity.class);
    }

    @Override
    public RegisterActivity getActivity() {
        mActivity = super.getActivity();
        if (mActivity == null) {
            mActivity = startActivity(mLaunchIntent, null, null);
        }

        return mActivity;
    }

    @UiThreadTest
    public void testActivity() {
        mActivity = getActivity();
        assertNotNull(mActivity);
    }

    @UiThreadTest
    public void testEmailRegLayout() {
        mActivity = getActivity();
        EditText etMail = (EditText) mActivity.findViewById(com.edusoho.kuozhi.R.id.et_mail);
        assertNotNull(etMail);
        EditText etMailPass = (EditText) mActivity.findViewById(com.edusoho.kuozhi.R.id.et_mail_pass);
        assertNotNull(etMailPass);
        EduSohoLoadingButton btnMailReg = (EduSohoLoadingButton) mActivity.findViewById(com.edusoho.kuozhi.R.id.btn_mail_reg);
        assertNotNull(btnMailReg);
    }

    @UiThreadTest
    public void testPhoneRegLayout() {
        mActivity = getActivity();
        TabHost tabHost = (TabHost) mActivity.findViewById(R.id.tabHost);
        assertNotNull(tabHost);
        TabWidget tabWidget = tabHost.getTabWidget();
        assertEquals(2, tabWidget.getTabCount());

        EditText etPhone = (EditText) mActivity.findViewById(R.id.et_phone);
        assertNotNull(etPhone);
        EditText etCode = (EditText) mActivity.findViewById(R.id.et_code);
        assertNotNull(etCode);
        EditText etPhonePass = (EditText) mActivity.findViewById(R.id.et_phone_pass);
        assertNotNull(etPhonePass);
        Button btnSendCode = (Button) mActivity.findViewById(R.id.btn_send_code);
        assertNotNull(btnSendCode);
        EduSohoLoadingButton btnPhoneReg = (EduSohoLoadingButton) mActivity.findViewById(R.id.btn_phone_reg);
        assertNotNull(btnPhoneReg);
    }
}
