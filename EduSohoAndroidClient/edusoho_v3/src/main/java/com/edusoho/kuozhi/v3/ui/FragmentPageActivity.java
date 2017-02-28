package com.edusoho.kuozhi.v3.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.ui.base.ActionBarBaseActivity;
import com.edusoho.kuozhi.v3.util.Const;

public class FragmentPageActivity extends ActionBarBaseActivity {

    public static final String FRAGMENT = "fragment";
    private String mFragment;
    private String mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("FragmentPageActivity", "start");
        setContentView(R.layout.fragment_page_layout);
        initView();
    }

    protected void initView() {
        Intent data = getIntent();
        if (data != null) {
            mFragment = data.getStringExtra(FRAGMENT);
            mTitle = data.getStringExtra(Const.ACTIONBAR_TITLE);
        }

        setBackMode(BACK, mTitle == null ? "标题" : mTitle);
        loadFragment(mFragment, data != null ? data.getExtras() : null);
    }

    protected void loadFragment(String fragmentName, Bundle bundle) {
        try {
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            Fragment fragment = app.mEngine.runPluginWithFragmentByBundle(
                    fragmentName, mActivity, bundle);
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
        } catch (Exception ex) {
            Log.d("FragmentPageActivity", ex.toString());
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("FragmentPageActivity", "onActivityResult");
        if (getSupportFragmentManager().getFragments().size() > 0) {
            getSupportFragmentManager().getFragments().get(0).onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            return;
        }
        super.onBackPressed();
    }
}
