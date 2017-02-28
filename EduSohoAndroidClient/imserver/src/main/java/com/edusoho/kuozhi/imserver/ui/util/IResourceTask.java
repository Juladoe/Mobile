package com.edusoho.kuozhi.imserver.ui.util;

/**
 * Created by suju on 16/8/28.
 */
public interface IResourceTask {

    TaskFeature execute();

    void cancel();

    int getTaskId();
}
