package com.edusoho.kuozhi.v3.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


/**
 * Created by howzhi on 14-6-10.
 */
public class ResourceDownStatusReceiver extends BroadcastReceiver {

    //所发的Intent的名字
    public static final String ACTION = "android.intent.action.RESOURCE_DOWNLOAD_STATUS";

    private DownloadStatusReceiver.StatusCallback mStatusCallback;

    public ResourceDownStatusReceiver() {
        super();
    }

    public ResourceDownStatusReceiver(DownloadStatusReceiver.StatusCallback callback) {
        this();
        this.mStatusCallback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("DownStatusReceiver", "onReceive");
        if (mStatusCallback != null) {
            mStatusCallback.invoke(intent);
        }
    }
}
