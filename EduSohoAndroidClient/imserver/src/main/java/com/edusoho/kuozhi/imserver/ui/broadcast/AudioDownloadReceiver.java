package com.edusoho.kuozhi.imserver.ui.broadcast;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.edusoho.kuozhi.imserver.ui.listener.MessageFileDownloadListener;

/**
 * Created by JesseHuang on 15/7/19.
 */
public class AudioDownloadReceiver extends BroadcastReceiver {

    private MessageFileDownloadListener downloadListener;

    public void setAdapter(MessageFileDownloadListener adapter) {
        downloadListener = adapter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
            long downId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (downloadListener != null && downloadListener.getDownloadList() != null) {
                downloadListener.updateVoiceDownloadStatus(downId);
            }
        }
    }
}
