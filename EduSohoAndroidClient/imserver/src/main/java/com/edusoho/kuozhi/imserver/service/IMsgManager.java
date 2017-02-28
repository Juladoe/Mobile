package com.edusoho.kuozhi.imserver.service;

import com.edusoho.kuozhi.imserver.entity.ConvEntity;
import com.edusoho.kuozhi.imserver.entity.MessageEntity;

/**
 * Created by 菊 on 2016/4/22.
 */
public interface IMsgManager {

    void clear();

    void reset();

    boolean hasMessageByNo(String msgNo);

    String getLaterNo();

    void createConvNoEntity(ConvEntity convEntity);

    ConvEntity getConvByTypeAndId(String type, int id);

    ConvEntity getConvByConvNo(String convNo);

    MessageEntity getMessageByMsgNo(String msgNo);

    void updateConvEntityByConvNo(ConvEntity convEntity);

    void updateConvEntityById(ConvEntity convEntity);

    long createMessageEntity(MessageEntity messageEntity);

    void updateMessageEntityByUID(MessageEntity messageEntity);

    MessageEntity getMessageByUID(String uid);
}
