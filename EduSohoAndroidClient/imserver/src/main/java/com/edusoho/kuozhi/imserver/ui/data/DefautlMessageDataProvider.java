package com.edusoho.kuozhi.imserver.ui.data;

import android.content.ContentValues;
import android.util.Log;

import com.edusoho.kuozhi.imserver.IMClient;
import com.edusoho.kuozhi.imserver.SendEntity;
import com.edusoho.kuozhi.imserver.entity.ConvEntity;
import com.edusoho.kuozhi.imserver.entity.IMUploadEntity;
import com.edusoho.kuozhi.imserver.entity.MessageEntity;
import com.edusoho.kuozhi.imserver.entity.Role;
import com.edusoho.kuozhi.imserver.entity.message.Destination;
import com.edusoho.kuozhi.imserver.entity.message.MessageBody;
import com.edusoho.kuozhi.imserver.managar.IMMessageManager;
import com.edusoho.kuozhi.imserver.ui.data.IMessageDataProvider;
import com.edusoho.kuozhi.imserver.util.MessageEntityBuildr;
import com.edusoho.kuozhi.imserver.util.SendEntityBuildr;
import java.util.List;

/**
 * Created by suju on 16/9/6.
 */
public class DefautlMessageDataProvider implements IMessageDataProvider {

    @Override
    public int updateMessageFieldByUid(String uid, ContentValues cv) {
        return IMClient.getClient().getMessageManager().updateMessageFieldByUid(uid, cv);
    }

    @Override
    public MessageEntity getMessageByUID(String uid) {
        return IMClient.getClient().getMessageManager().getMessageByUID(uid);
    }

    @Override
    public int updateMessageFieldByMsgNo(String msgNo, ContentValues cv) {
        return IMClient.getClient().getMessageManager().updateMessageFieldByMsgNo(msgNo, cv);
    }

    @Override
    public long saveUploadEntity(String muid, String type, String source) {
        return IMClient.getClient().getMessageManager().saveUploadEntity(muid, type, source);
    }

    @Override
    public IMUploadEntity getUploadEntity(String muid) {
        return IMClient.getClient().getMessageManager().getUploadEntity(muid);
    }

    @Override
    public List<MessageEntity> getMessageList(String convNo, int start) {
        return IMClient.getClient().getChatRoom(convNo).getMessageList(start);
    }

    @Override
    public MessageEntity getMessage(int msgId) {
        return IMClient.getClient().getMessageManager().getMessage(msgId);
    }

    @Override
    public void sendMessage(String convNo, MessageBody messageBody) {
        try {
            String toId = "";
            switch (messageBody.getDestination().getType()) {
                case Destination.CLASSROOM:
                case Destination.COURSE:
                    toId = "all";
                    break;
                case Destination.USER:
                    toId = String.valueOf(messageBody.getDestination().getId());
            }
            messageBody.setMsgStatus(MessageEntity.StatusType.NONE);
            SendEntity sendEntity = SendEntityBuildr.getBuilder()
                    .addToId(toId)
                    .addCmd("send")
                    .addMsg(messageBody.toJson())
                    .builder();
            IMClient.getClient().getChatRoom(convNo).send(sendEntity);
        } catch (Exception e) {
        }
    }

    private MessageEntity createMessageEntityByBody(MessageBody messageBody) {
        return new MessageEntityBuildr()
                .addUID(messageBody.getMessageId())
                .addConvNo(messageBody.getConvNo())
                .addToId(String.valueOf(messageBody.getDestination().getId()))
                .addToName(messageBody.getDestination().getNickname())
                .addFromId(String.valueOf(messageBody.getSource().getId()))
                .addFromName(messageBody.getSource().getNickname())
                .addCmd("message")
                .addStatus(MessageEntity.StatusType.FAILED)
                .addMsg(messageBody.toJson())
                .addTime((int) (messageBody.getCreatedTime() / 1000))
                .builder();
    }

    @Override
    public MessageEntity createMessageEntity(MessageBody messageBody) {
        MessageEntity messageEntity = createMessageEntityByBody(messageBody);
        messageEntity = IMClient.getClient().getMessageManager().createMessage(messageEntity);
        updateConv(messageBody);
        return messageEntity;
    }

    private void updateConv(MessageBody messageBody) {
        ContentValues cv = new ContentValues();
        cv.put("laterMsg", messageBody.toJson());
        cv.put("updatedTime", System.currentTimeMillis());
        IMClient.getClient().getConvManager().updateConvField(messageBody.getConvNo(), cv);
    }

    @Override
    public int deleteMessageById(int msgId) {
        return IMClient.getClient().getMessageManager().deleteById(msgId);
    }

    @Override
    public void sendMessage(MessageEntity messageEntity) {
    }

    @Override
    public MessageEntity insertMessageEntity(MessageEntity messageEntity) {
        return null;
    }
}
