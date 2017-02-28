package com.edusoho.kuozhi.imserver.ui;

import android.os.Bundle;

import com.edusoho.kuozhi.imserver.entity.MessageEntity;
import com.edusoho.kuozhi.imserver.entity.message.MessageBody;
import com.edusoho.kuozhi.imserver.ui.listener.MessageControllerListener;

import java.io.File;

/**
 * Created by suju on 16/9/16.
 */
public interface IMessageListPresenter {

    String CONV_NO = "convNo";
    String TARGET_TYPE = "targetType";
    String TARGET_ID = "targetId";

    void start();

    void processResourceDownload(int resId, String resUri);

    void processResourceStatusChange(int resId, String resUri);

    void updateMessageReceiveStatus(MessageEntity messageEntity, int status);

    void insertMessageList();

    void deleteMessageById(int msgId);

    void uploadMedia(File file, MessageBody messageBody);

    void sendTextMessage(String content);

    void sendAudioMessage(File audioFile, int audioLength);

    void sendImageMessage(File imageFile);

    void onSendMessageAgain(MessageEntity messageEntity);

    void addMessageReceiver();

    void removeReceiver();

    void refresh();

    void onShowActivity(Bundle bundle);

    void onShowUser(int userId);

    void selectPhoto(String action);

    void updateRole(String type, int rid);

    void addMessageControllerListener(MessageControllerListener listener);

    void unEnableChatView();

    void enableChatView();

    boolean canRefresh();

    void onMessageSuccess(MessageEntity messageEntity);

    boolean onReceiverMessageEntity(MessageEntity msg);

    void reSendMessageList();
}
