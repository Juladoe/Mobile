package com.edusoho.test.base;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.view.ContextThemeWrapper;

import com.edusoho.test.R;
import com.edusoho.test.TestEduSohoApp;
import com.edusoho.test.utils.TestUtils;

/**
 * Created by JesseHuang on 15/8/24.
 */
public class BaseActivityUnitTestCase<T extends Activity> extends ActivityUnitTestCase<T> {

    protected Intent mLaunchIntent;
    protected Instrumentation mInstrumentation;
    protected TestEduSohoApp mApp;

    public BaseActivityUnitTestCase(Class<T> activityClass) {
        super(activityClass);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mInstrumentation = getInstrumentation();
        ContextThemeWrapper context = new ContextThemeWrapper(
                mInstrumentation.getTargetContext(), R.style.Theme_AppCompat);
        setActivityContext(context);

        mApp = (TestEduSohoApp) mInstrumentation.newApplication(
                getClass().getClassLoader(), TestEduSohoApp.class.getName(), context);
        TestUtils.initApplication(mApp, mInstrumentation.getTargetContext());
        mInstrumentation.callApplicationOnCreate(mApp);
        setApplication(mApp);
    }

    @Override
    public T getActivity() {
        T mActivity = super.getActivity();
        if (mActivity == null) {
            mActivity = startActivity(mLaunchIntent, null, null);
        }
        return mActivity;
    }


}
