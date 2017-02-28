package com.edusoho.kuozhi.imserver.util;

import com.edusoho.kuozhi.imserver.entity.MessageEntity;

/**
 * Created by 菊 on 2016/4/27.
 */
public class MessageEntityBuildr {

    private MessageEntity messageEntity;

    public MessageEntityBuildr()
    {
        this.messageEntity = new MessageEntity();
    }

    public static MessageEntityBuildr getBuilder() {
        return new MessageEntityBuildr();
    }

    public MessageEntityBuildr addFromId(String fromId) {
        messageEntity.setFromId(fromId);
        return this;
    }

    public MessageEntityBuildr addFromName(String fromName) {
        messageEntity.setFromName(fromName);
        return this;
    }

    public MessageEntityBuildr addConvNo(String convNo) {
        messageEntity.setConvNo(convNo);
        return this;
    }

    public MessageEntityBuildr addMsg(String msg) {
        messageEntity.setMsg(msg);
        return this;
    }

    public MessageEntityBuildr addUID(String uid) {
        messageEntity.setUid(uid);
        return this;
    }

    public MessageEntityBuildr addToId(String toId) {
        messageEntity.setToId(toId);
        return this;
    }

    public MessageEntityBuildr addToName(String toName) {
        messageEntity.setToName(toName);
        return this;
    }

    public MessageEntityBuildr addMsgNo(String msgNo) {
        messageEntity.setMsgNo(msgNo);
        return this;
    }

    public MessageEntityBuildr addTime(int time) {
        messageEntity.setTime(time);
        return this;
    }

    public MessageEntityBuildr addId(int id) {
        messageEntity.setId(id);
        return this;
    }

    public MessageEntityBuildr addCmd(String cmd) {
        messageEntity.setCmd(cmd);
        return this;
    }

    public MessageEntityBuildr addStatus(int status) {
        messageEntity.setStatus(status);
        return this;
    }

    public MessageEntity builder() {
        return messageEntity;
    }
}
