package com.edusoho.kuozhi.imserver.ui.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.edusoho.kuozhi.imserver.ui.util.ITaskStatusListener;

/**
 * Created by suju on 16/8/28.
 */
public class ResourceStatusReceiver extends BroadcastReceiver {

    public static final String TAG = "ResourceStatusReceiver";
    public static final String ACTION = "com.edusoho.kuozhi.broadcast.ResourceStatusReceiver";
    public static final String RES_ID = "res_id";
    public static final String RES_URI = "res_uri";
    public static final String TASK_TYPE = "task_type";

    public static final int SUCCESS = 0010;
    public static final int FAIL = 0012;

    private StatusReceiverCallback mStatusReceiverCallback;

    public ResourceStatusReceiver(StatusReceiverCallback callback) {
        this.mStatusReceiverCallback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.hasExtra(RES_ID)) {
            int resId = intent.getIntExtra(RES_ID, 0);
            int taskType = intent.getIntExtra(TASK_TYPE, ITaskStatusListener.UPLOAD);
            String resUri = intent.getStringExtra(RES_URI);

            if (taskType == ITaskStatusListener.UPLOAD) {
                mStatusReceiverCallback.onResourceStatusInvoke(resId, resUri);
                return;
            }
            mStatusReceiverCallback.onResourceDownloadInvoke(resId, resUri);
        }
    }

    public interface StatusReceiverCallback {

        void onResourceStatusInvoke(int resId, String resUri);

        void onResourceDownloadInvoke(int resId, String resUri);
    }
}
