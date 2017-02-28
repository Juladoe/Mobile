package com.edusoho.test;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import com.edusoho.kuozhi.v3.ui.base.ActionBarBaseActivity;
import com.edusoho.kuozhi.v3.util.Const;

import java.util.List;

/**
 * Created by howzhi on 15/8/17.
 */
public class FragmentTestActivity extends ActionBarBaseActivity {

    private String mFragment;
    private String mTitle;
    public static final String FRAGMENT = "fragment";
    private Fragment tmpFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(null, "FragmentPageActivity start");
        setContentView(com.edusoho.kuozhi.R.layout.fragment_page_layout);
        initView();
    }

    public Fragment loadFragment(String fragmentName, Bundle bundle) {
        try {
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            Fragment fragment = Fragment.instantiate(getBaseContext(), fragmentName);
            fragment.setArguments(bundle);
            fragmentTransaction.replace(android.R.id.content, fragment);
            fragmentTransaction.commit();
            tmpFragment = fragment;
        } catch (Exception ex) {
            Log.d("FragmentTestActivity", ex.toString());
        }
        return tmpFragment;
    }

    protected void initView() {

        Intent data = getIntent();
        if (data != null) {
            mFragment = data.getStringExtra(FRAGMENT);
            mTitle = data.getStringExtra(Const.ACTIONBAR_TITLE);
        }

        mFragment = data.getStringExtra(FRAGMENT);
        mTitle = data.getStringExtra(Const.ACTIONBAR_TITLE);
        try {
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            Fragment fragment = app.mEngine.runPluginWithFragmentByBundle(
                    mFragment, mActivity, data != null ? data.getExtras() : null);
            fragmentTransaction.replace(android.R.id.content, fragment);
            fragmentTransaction.commit();
        } catch (Exception ex) {
            Log.d("FragmentPageActivity", ex.toString());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (getSupportFragmentManager().getFragments().size() > 0) {
            getSupportFragmentManager().getFragments().get(0).onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
