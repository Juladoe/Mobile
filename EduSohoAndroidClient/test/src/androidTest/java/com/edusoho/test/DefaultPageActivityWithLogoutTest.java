package com.edusoho.test;

import android.app.Instrumentation;
import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.test.UiThreadTest;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.edusoho.kuozhi.v3.ui.DefaultPageActivity;
import com.edusoho.kuozhi.v3.view.EduSohoIconView;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by JesseHuang on 15/8/23.
 */
public class DefaultPageActivityWithLogoutTest extends ActivityUnitTestCase<DefaultPageActivity> {
    private DefaultPageActivity mActivity;
    protected Instrumentation mInstrumentation;
    protected Intent mLaunchIntent;

    public DefaultPageActivityWithLogoutTest() {
        super(DefaultPageActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mInstrumentation = getInstrumentation();
        ContextThemeWrapper context = new ContextThemeWrapper(
                mInstrumentation.getTargetContext(), R.style.AppThemeNoActionBar);
        setActivityContext(context);

        TestEduSohoApp app = (TestEduSohoApp) mInstrumentation.newApplication(
                getClass().getClassLoader(), TestEduSohoApp.class.getName(), context);
        mInstrumentation.callApplicationOnCreate(app);
        setApplication(app);
        mLaunchIntent = new Intent(mInstrumentation.getTargetContext(),
                DefaultPageActivity.class);
    }

    @Override
    public DefaultPageActivity getActivity() {
        mActivity = super.getActivity();
        if (mActivity == null) {
            mActivity = startActivity(mLaunchIntent, null, null);
        }

        return mActivity;
    }

    @UiThreadTest
    public void testInitLayout() {
        mActivity = getActivity();
        //VISIBLE
        CircleImageView civAvatar = (CircleImageView) mActivity.findViewById(R.id.circleIcon);
        assertNotNull(civAvatar);
        TextView tvLogin = (TextView) mActivity.findViewById(R.id.tv_login);
        assertNotNull(tvLogin);
        assertEquals(View.VISIBLE, tvLogin.getVisibility());
        EduSohoIconView ivSetting = (EduSohoIconView) mActivity.findViewById(R.id.iv_setting);
        assertNotNull(ivSetting);
        assertEquals(View.VISIBLE, ivSetting.getVisibility());
        Button btnLogin = (Button) mActivity.findViewById(R.id.btn_login);
        assertNotNull(btnLogin);
        assertEquals(View.VISIBLE, btnLogin.getVisibility());
        Button btnRegister = (Button) mActivity.findViewById(R.id.btn_register);
        assertNotNull(btnRegister);
        assertEquals(View.VISIBLE, btnRegister.getVisibility());

        //GONE
        View vItems = mActivity.findViewById(R.id.ll_item);
        assertNotNull(vItems);
        assertEquals(View.GONE, vItems.getVisibility());
        TextView tvNickname = (TextView) mActivity.findViewById(R.id.tv_nickname);
        assertNotNull(tvNickname);
        assertEquals(View.GONE, tvNickname.getVisibility());
        TextView tvTitle = (TextView) mActivity.findViewById(R.id.tv_user_title);
        assertNotNull(tvTitle);
        assertEquals(View.GONE, tvTitle.getVisibility());
    }
}
