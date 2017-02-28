package com.edusoho.kuozhi.imserver.ui.util;

/**
 * Created by suju on 16/8/28.
 */
public class TaskFeature {

    private int mTaskId;
    private int mTaskType;
    private ITaskStatusListener mITaskStatusListener;

    public TaskFeature(int taskId, int taskType) {
        this.mTaskId = taskId;
        this.mTaskType = taskType;
    }

    public void addITaskStatusListener(ITaskStatusListener listener) {
        this.mITaskStatusListener = listener;
    }

    public void success(String uri) {
        if (mITaskStatusListener == null) {
            return;
        }
        mITaskStatusListener.onSuccess(mTaskId, mTaskType, uri);
    }

    public void fail() {
        if (mITaskStatusListener == null) {
                return;
        }
        mITaskStatusListener.onFail(mTaskId, mTaskType);
    }
}
