package com.edusoho.kuozhi.v3.broadcast;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.edusoho.kuozhi.v3.listener.ChatDownloadListener;

/**
 * Created by JesseHuang on 15/7/19.
 */
public class AudioDownloadReceiver extends BroadcastReceiver {

    private ChatDownloadListener chatDownloadListener;

    public void setAdapter(ChatDownloadListener adapter) {
        chatDownloadListener = adapter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
            long downId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (chatDownloadListener != null && chatDownloadListener.getDownloadList() != null) {
                chatDownloadListener.updateVoiceDownloadStatus(downId);
            }
        }
    }
}
