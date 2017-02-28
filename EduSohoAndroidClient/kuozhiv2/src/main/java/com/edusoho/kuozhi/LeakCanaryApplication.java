package com.edusoho.kuozhi;

import com.edusoho.kuozhi.v3.EdusohoApp;

/**
 * Created by suju on 16/8/22.
 */
public class LeakCanaryApplication extends EdusohoApp {

    @Override public void onCreate() {
        super.onCreate();
        //LeakCanary.install(this);
    }
}
