package com.edusoho.test;

import android.app.Instrumentation;
import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.test.UiThreadTest;
import android.view.ContextThemeWrapper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.edusoho.kuozhi.v3.ui.LoginActivity;
import com.edusoho.kuozhi.v3.view.EduSohoLoadingButton;

/**
 * Created by JesseHuang on 15/8/23.
 */
public class LoginActivityTest extends ActivityUnitTestCase<LoginActivity> {

    private LoginActivity mActivity;
    protected Instrumentation mInstrumentation;
    protected Intent mLaunchIntent;

    public LoginActivityTest() {
        super(LoginActivity.class);
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
                LoginActivity.class);
    }

    @Override
    public LoginActivity getActivity() {
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
    public void testLayout() {
        mActivity = getActivity();
        EditText etUsername = (EditText) mActivity.findViewById(R.id.et_username);
        assertNotNull(etUsername);
        EditText etPassword = (EditText) mActivity.findViewById(R.id.et_password);
        assertNotNull(etPassword);
        EduSohoLoadingButton mBtnLogin = (EduSohoLoadingButton) mActivity.findViewById(R.id.btn_login);
        assertNotNull(mBtnLogin);
        ImageView ivWeibo = (ImageView) mActivity.findViewById(R.id.iv_weibo);
        assertNotNull(ivWeibo);
        ImageView ivQQ = (ImageView) mActivity.findViewById(R.id.iv_qq);
        assertNotNull(ivQQ);
        ImageView ivWeixin = (ImageView) mActivity.findViewById(R.id.iv_weixin);
        assertNotNull(ivWeixin);
    }


}
