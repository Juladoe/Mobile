package com.edusoho.kuozhi;

import android.view.View;

import com.edusoho.kuozhi.v3.ui.SplashActivity;

import java.util.ArrayList;

import jazzyviewpager.JazzyViewPager;

/**
 * Created by howzhi on 14-8-6.
 */
public class KuozhiSplashActivity extends SplashActivity {

    @Override
    protected void loadConfig() {
        mSplashMode = JazzyViewPager.TransitionEffect.Standard;
    }

    @Override
    public ArrayList<View> initSplashList() {
        int[] imageIds = new int[]{
                com.edusoho.R.drawable.splash_1,
                com.edusoho.R.drawable.splash_2,
                com.edusoho.R.drawable.splash_3
        };
        return createSplashList(imageIds);
    }
}
