package com.edusoho.kuozhi.imserver.ui.data;

import android.content.ContentValues;

import com.edusoho.kuozhi.imserver.entity.IMUploadEntity;
import com.edusoho.kuozhi.imserver.entity.MessageEntity;
import com.edusoho.kuozhi.imserver.entity.Role;
import com.edusoho.kuozhi.imserver.entity.message.MessageBody;
import com.edusoho.kuozhi.imserver.managar.IMMessageManager;

import java.util.List;

/**
 * Created by suju on 16/9/6.
 */
public interface IMessageDataProvider {

    MessageEntity createMessageEntity(MessageBody messageBody);

    void sendMessage(String convNo, MessageBody messageBody);

    List<MessageEntity> getMessageList(String convNo, int start);

    MessageEntity getMessage(int msgId);

    IMUploadEntity getUploadEntity(String muid);

    long saveUploadEntity(String muid, String type, String source);

    int updateMessageFieldByMsgNo(String msgNo, ContentValues cv);

    MessageEntity getMessageByUID(String uid);

    int updateMessageFieldByUid(String uid, ContentValues cv);

    int deleteMessageById(int msgId);

    void sendMessage(MessageEntity messageEntity);

    MessageEntity insertMessageEntity(MessageEntity messageEntity);
}
