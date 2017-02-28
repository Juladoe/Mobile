package com.edusoho.kuozhi.imserver.ui;

import com.edusoho.kuozhi.imserver.entity.MessageEntity;

import java.util.List;

/**
 * Created by suju on 16/9/16.
 */
public interface IMessageListView {

    void setPresenter(IMessageListPresenter presenter);

    void notifiy(String content);

    void setMessageList(List<MessageEntity> messageEntityList);

    void updateMessageEntity(MessageEntity updateMessageEntity);

    void insertMessage(MessageEntity messageEntity);

    void insertMessageList(List<MessageEntity> messageEntityList);

    void notifyDataSetChanged();

    void setEnable(boolean isEnable);

    void setInputTextMode(int mode);

    void onUserKicked();
}
