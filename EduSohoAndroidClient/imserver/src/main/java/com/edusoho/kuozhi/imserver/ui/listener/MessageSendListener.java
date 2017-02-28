package com.edusoho.kuozhi.imserver.ui.listener;

import java.io.File;

/**
 * Created by suju on 16/8/26.
 */
public interface MessageSendListener {

    void onSendMessage(String message);

    void onSendAudio(File audioFile, int audioLength);

    void onSendImage(File imageFile);

    void onStartRecordAudio();

    void onStopRecordAudio();
}
