package com.edusoho.kuozhi.imserver.ui.listener;

import java.util.HashMap;

/**
 * Created by JesseHuang on 15/10/16.
 */
public interface MessageFileDownloadListener {

    void updateVoiceDownloadStatus(long downId);

    HashMap<Long, Integer> getDownloadList();
}
