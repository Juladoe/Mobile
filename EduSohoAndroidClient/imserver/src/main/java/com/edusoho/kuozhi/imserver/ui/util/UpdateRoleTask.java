package com.edusoho.kuozhi.imserver.ui.util;

import android.os.Handler;

import com.edusoho.kuozhi.imserver.IMClient;
import com.edusoho.kuozhi.imserver.entity.Role;
import com.edusoho.kuozhi.imserver.ui.listener.MessageControllerListener;

/**
 * Created by suju on 16/9/1.
 */
public class UpdateRoleTask implements IResourceTask {

    private String type;
    private int rid;
    private TaskCallback mTaskCallback;

    public UpdateRoleTask(String type, int rid, TaskCallback callback) {
        this.type = type;
        this.rid = rid;
        this.mTaskCallback = callback;
    }

    @Override
    public int getTaskId() {
        return rid;
    }

    @Override
    public TaskFeature execute() {
        final TaskFeature taskFeature = new TaskFeature(rid, ITaskStatusListener.NO_BROADCAST);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mTaskCallback.run(taskFeature);
            }
        }, 300);
        return taskFeature;
    }

    @Override
    public void cancel() {
    }

    public interface TaskCallback {

        void run(TaskFeature taskFeature);
    }
}
