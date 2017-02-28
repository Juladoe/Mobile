package com.edusoho.kuozhi.v3.listener;

import java.util.HashMap;

/**
 * Created by JesseHuang on 15/10/16.
 */
public interface ChatDownloadListener {
    void updateVoiceDownloadStatus(long downId);

    HashMap<Long, Integer> getDownloadList();
}
