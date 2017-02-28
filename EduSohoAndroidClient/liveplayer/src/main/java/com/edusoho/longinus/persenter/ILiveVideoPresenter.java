package com.edusoho.longinus.persenter;


import com.edusoho.kuozhi.imserver.entity.MessageEntity;

/**
 * Created by suju on 16/10/18.
 */
public interface ILiveVideoPresenter {

    void updateLivePlayStatus(MessageEntity messageEntity);

    void updateNotice(MessageEntity messageEntity);

    void handleHistorySignals();

    void updateLiveNotice(boolean alawsShow);

    void onKill(MessageEntity messageEntity);

    void setChatRoomNetWorkStatus(int status);
}
