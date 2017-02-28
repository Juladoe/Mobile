package com.edusoho.kuozhi.imserver.ui.util;

/**
 * Created by suju on 16/8/28.
 */
public interface ITaskStatusListener {

    int DOWNLOAD = 0001;
    int UPLOAD = 0002;
    int NO_BROADCAST = 0003;

    void onSuccess(int taskId, int taskType, String uri);

    void onFail(int taskId, int taskType);
}
