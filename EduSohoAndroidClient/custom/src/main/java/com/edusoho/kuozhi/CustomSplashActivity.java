package com.edusoho.kuozhi;

import android.content.res.Resources;
import android.view.View;

import com.edusoho.kuozhi.v3.ui.SplashActivity;
import com.edusoho.kuozhi.v3.util.AppUtil;

import java.util.ArrayList;

import jazzyviewpager.JazzyViewPager;

/**
 * Created by howzhi on 14-8-6.
 */
public class CustomSplashActivity extends SplashActivity {

    @Override
    protected void loadConfig() {
        mSplashMode = JazzyViewPager.TransitionEffect.Standard;
    }

    @Override
    public ArrayList<View> initSplashList() {
        Resources resource = getResources();
        int num = AppUtil.parseInt(resource.getString(R.string.splash_num));
        int[] imageIds = new int[num];
        for (int i=0; i < num; i++) {
            int id = resource.getIdentifier(
                    "splash_" + (i + 1), "drawable", getPackageName());
            imageIds[i] = id;
        }
        return createSplashList(imageIds);
    }
}
