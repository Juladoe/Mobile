package com.edusoho.kuozhi.v3.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.listener.PluginFragmentCallback;
import com.edusoho.kuozhi.v3.ui.base.ActionBarBaseActivity;
import com.edusoho.kuozhi.v3.ui.fragment.FindPasswordFragment;

/**
 * Created by JesseHuang on 2016/11/25.
 */

public class ForgetPasswordActivity extends ActionBarBaseActivity {

    public static final String RESET_INFO = "reset_info";
    private ImageView ivBack;
    private String mCurrentTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        initView();
        showFragment("FindPasswordFragment", null);
    }

    private void initView() {
        ivBack = (ImageView) findViewById(R.id.iv_back);
        ivBack.setOnClickListener(getBackClickListener());
    }

    public void showFragment(String fragmentTag, final Bundle fragmentBundle) {
        Fragment fragment;
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.setCustomAnimations(R.anim.fragment_slide_left_enter,
                R.anim.fragment_slide_left_exit,
                R.anim.fragment_slide_right_enter,
                R.anim.fragment_slide_right_exit);
        fragment = mFragmentManager.findFragmentByTag(fragmentTag);
        if (fragment != null) {
            fragmentTransaction.show(fragment);
        } else {
            fragment = app.mEngine.runPluginWithFragment(fragmentTag, mActivity, new PluginFragmentCallback() {
                @Override
                public void setArguments(Bundle bundle) {
                    if (fragmentBundle != null) {
                        bundle.putString(RESET_INFO, fragmentBundle.getString(ForgetPasswordActivity.RESET_INFO));
                        bundle.putString(FindPasswordFragment.SMS_TOKEN, fragmentBundle.getString(FindPasswordFragment.SMS_TOKEN));
                    }
                }
            });
            fragmentTransaction.add(R.id.fl_container, fragment, fragmentTag);
        }
        fragmentTransaction.commit();
        mCurrentTag = fragmentTag;
    }

    public void hideFragment(String fragmentTag) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        Fragment fragment = mFragmentManager.findFragmentByTag(fragmentTag);
        if (fragment != null) {
            fragmentTransaction.hide(fragment);
        }
        fragmentTransaction.commit();
    }

    public void removeFragment(String fragmentTag) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        Fragment fragment = mFragmentManager.findFragmentByTag(fragmentTag);
        if (fragment != null) {
            fragmentTransaction.remove(fragment);
        }
        fragmentTransaction.commit();
    }

    public void switchFragment(String fragmentTag, Bundle bundle) {
        if (mCurrentTag != null) {
            hideFragment(mCurrentTag);
            showFragment(fragmentTag, bundle);
        }
    }

    private View.OnClickListener getBackClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        };
    }

    @Override
    public void onBackPressed() {
        if (mCurrentTag != null) {
            if ("FindPasswordFragment".equals(mCurrentTag)) {
                super.onBackPressed();
            } else {
                removeFragment(mCurrentTag);
                showFragment("FindPasswordFragment", null);
            }
        }
    }
}
