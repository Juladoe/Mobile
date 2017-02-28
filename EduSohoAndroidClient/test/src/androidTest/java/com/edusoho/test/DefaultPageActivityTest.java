package com.edusoho.test;

import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.test.UiThreadTest;
import android.test.suitebuilder.annotation.MediumTest;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.edusoho.kuozhi.v3.ui.DefaultPageActivity;
import com.edusoho.kuozhi.v3.view.EduSohoIconView;
import com.edusoho.test.base.BaseActivityNoActionBarUnitTestCase;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by howzhi on 15/8/13.
 */

@MediumTest
public class DefaultPageActivityTest extends BaseActivityNoActionBarUnitTestCase<DefaultPageActivity> {

    private DefaultPageActivity mActivity;

    public DefaultPageActivityTest() {
        super(DefaultPageActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mLaunchIntent = new Intent(mInstrumentation.getTargetContext(),
                DefaultPageActivity.class);
    }

    @UiThreadTest
    public void testActivity() {
        mActivity = getActivity();
        assertNotNull(mActivity);
    }

    @UiThreadTest
    public void testActivityBtnLayout() {
        mActivity = getActivity();
        LinearLayout navLayout = (LinearLayout) mActivity.findViewById(com.edusoho.kuozhi.R.id.nav_bottom_layout);
        assertNotNull(navLayout);
        assertEquals(3, navLayout.getChildCount());

        assertEquals(R.id.nav_tab_news, navLayout.getChildAt(0).getId());
        assertEquals(R.id.nav_tab_find, navLayout.getChildAt(1).getId());
        assertEquals(R.id.nav_tab_friends, navLayout.getChildAt(2).getId());
    }

    @UiThreadTest
    public void testActivityDestroy() {
        mActivity = getActivity();
        mInstrumentation.callActivityOnDestroy(mActivity);
    }

    @UiThreadTest
    public void testLoginDrawerUI() {
        mActivity = getActivity();
        //Visible
        EduSohoIconView ivSetting = (EduSohoIconView) mActivity.findViewById(R.id.iv_setting);
        assertNotNull(ivSetting);
        assertEquals(View.GONE, ivSetting.getVisibility());
        CircleImageView civAvatar = (CircleImageView) mActivity.findViewById(R.id.circleIcon);
        assertNotNull(civAvatar);
        TextView tvNickname = (TextView) mActivity.findViewById(R.id.tv_nickname);
        assertNotNull(tvNickname);
        assertEquals(View.VISIBLE, tvNickname.getVisibility());
        TextView tvTitle = (TextView) mActivity.findViewById(R.id.tv_user_title);
        assertNotNull(tvTitle);
        assertEquals(View.VISIBLE, tvTitle.getVisibility());
        View vItems = mActivity.findViewById(R.id.ll_item);
        assertNotNull(vItems);
        assertEquals(View.VISIBLE, vItems.getVisibility());

        //gone
        TextView tvLogin = (TextView) mActivity.findViewById(R.id.tv_login);
        assertNotNull(tvLogin);
        assertEquals(View.GONE, tvLogin.getVisibility());
    }
}
