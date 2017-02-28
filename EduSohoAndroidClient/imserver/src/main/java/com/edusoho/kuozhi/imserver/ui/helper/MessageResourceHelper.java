package com.edusoho.kuozhi.imserver.ui.helper;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.edusoho.kuozhi.imserver.IMClient;
import com.edusoho.kuozhi.imserver.ui.broadcast.ResourceStatusReceiver;
import com.edusoho.kuozhi.imserver.ui.util.IResourceTask;
import com.edusoho.kuozhi.imserver.ui.util.ITaskStatusListener;
import com.edusoho.kuozhi.imserver.ui.util.ResourceDownloadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Queue;

/**
 * Created by suju on 16/8/28.
 */
public class MessageResourceHelper {

    private static final String TAG = "MessageResourceHelper";

    private Context mContext;
    private HashMap<Integer, IResourceTask> mTaskMap;
    private Queue mTaskQueue;

    public MessageResourceHelper(Context context) {
        this.mContext = context;
        mTaskMap = new HashMap<>();
        mTaskQueue = new ArrayDeque();
    }

    public void addAudioDownloadTask(int taskId, String readFilePath) throws IOException {
        File realFile = new MessageHelper(mContext).createAudioFile(readFilePath);
        ResourceDownloadTask downloadTask = new ResourceDownloadTask(mContext, taskId, readFilePath, realFile);
        addTask(downloadTask);
    }

    public void addImageDownloadTask(int taskId, String readFilePath) throws IOException {
        File realFile = new MessageHelper(mContext).createImageFile(readFilePath);
        ResourceDownloadTask downloadTask = new ResourceDownloadTask(mContext, taskId, readFilePath, realFile);
        addTask(downloadTask);
    }

    public void addTask(IResourceTask task) {
        if (mTaskMap == null) {
            return;
        }
        if (hasTask(task.getTaskId())) {
            Log.d(TAG, "task has:" + task.getTaskId());
            return;
        }
        Log.d(TAG, "add task:" + task.getTaskId());
        mTaskMap.put(task.getTaskId(), task);
        task.execute().addITaskStatusListener(createITaskStatusListener());
    }

    private ITaskStatusListener createITaskStatusListener() {
        return new ITaskStatusListener() {
            @Override
            public void onSuccess(int taskId, int taskType, String uri) {
                if (taskType != ITaskStatusListener.NO_BROADCAST) {
                    Log.d(TAG, "onSuccess");
                    Intent intent = new Intent(ResourceStatusReceiver.ACTION);
                    intent.putExtra(ResourceStatusReceiver.RES_ID, taskId);
                    intent.putExtra(ResourceStatusReceiver.TASK_TYPE, taskType);
                    intent.putExtra(ResourceStatusReceiver.RES_URI, uri);
                    mContext.sendBroadcast(intent);
                }
                removeTask(taskId);
            }

            @Override
            public void onFail(int taskId, int taskType) {
                if (taskType != ITaskStatusListener.NO_BROADCAST) {
                    Log.d(TAG, "onFail");
                    Intent intent = new Intent(ResourceStatusReceiver.ACTION);
                    intent.putExtra(ResourceStatusReceiver.RES_ID, taskId);
                    mContext.sendBroadcast(intent);
                }
                removeTask(taskId);
            }
        };
    }

    public void removeTask(int id) {
        if (mTaskMap == null) {
            return;
        }
        IResourceTask task = mTaskMap.get(id);
        if (task == null) {
            mTaskMap.remove(id);
            return;
        }
        Log.d(TAG, "removeTask task:" + id);
        task.cancel();
        mTaskMap.remove(id);
    }

    public boolean hasTask(int id) {
        if (mTaskMap == null) {
            return false;
        }
        return mTaskMap.containsKey(id);
    }

    public void clear() {
        for (Integer id : mTaskMap.keySet()) {
            IResourceTask task = mTaskMap.get(id);
            if (task != null) {
                task.cancel();
            }
        }
        mTaskMap.clear();
        mTaskMap = null;
    }
}
