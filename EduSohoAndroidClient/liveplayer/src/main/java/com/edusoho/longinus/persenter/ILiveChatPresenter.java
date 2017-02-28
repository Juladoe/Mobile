package com.edusoho.longinus.persenter;

import com.edusoho.kuozhi.imserver.entity.MessageEntity;
import com.edusoho.kuozhi.imserver.ui.IMessageListView;

/**
 * Created by suju on 16/10/18.
 */
public interface ILiveChatPresenter {

    void setUserCanChatStatus(MessageEntity message);

    void joinLiveChatRoom();

    void setView(IMessageListView view);

    void onReplace();

    void checkClientIsBan(String clientId);

    void connectLiveChatServer();

    void reConnectChatServer();
}
