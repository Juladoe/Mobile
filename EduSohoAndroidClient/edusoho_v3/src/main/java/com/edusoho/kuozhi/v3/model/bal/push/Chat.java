package com.edusoho.kuozhi.v3.model.bal.push;

import com.edusoho.kuozhi.imserver.entity.message.MessageBody;
import com.edusoho.kuozhi.v3.util.PushUtil;

/**
 * Created by JesseHuang on 15/7/2.
 */
public class Chat extends BaseMsgEntity {

    public String mid;
    public String msgNo;
    public int fromId;
    public int toId;
    public String nickname;

    public Direct direct;

    public String custom;

    public Direct getDirect() {
        if (fromId != 0 && toId != 0) {
            return Direct.getDirect(fromId == toId);
        }
        return direct;
    }

    public void setDirect(Direct direct) {
        this.direct = direct;
    }


    public Chat() {
    }

    public Chat(int fromId, int toId, String nickname, String headImgUrl, String content, String type, long createdTime) {
        super(0, content, headImgUrl, 2, type, createdTime);
        this.fromId = fromId;
        this.toId = toId;
        this.nickname = nickname;
    }

    public Chat(int chatId, int id, int fromId, int toId, String nickname, String headImgUrl, String content, String type, int delivery, int createdTime) {
        super(id, content, headImgUrl, delivery, type, createdTime);
        this.fromId = fromId;
        this.toId = toId;
        this.nickname = nickname;
    }

    public Chat(MessageBody messageBody) {
        super(0, messageBody.getBody(), messageBody.getSource().getImage(), PushUtil.MsgDeliveryType.UPLOADING, messageBody.getType(), messageBody.getCreatedTime());

        msgNo = messageBody.getMsgNo();
        mid = messageBody.getMessageId();
        fromId = messageBody.getSource().getId();
        toId = messageBody.getDestination().getId();
        nickname = messageBody.getSource().getNickname();
        if (type == PushUtil.ChatMsgType.TEXT) {
            delivery = PushUtil.MsgDeliveryType.SUCCESS;
        }
    }

    public Chat(OffLineMsgEntity offlineMsgModel) {
        V2CustomContent v2CustomContent = offlineMsgModel.getCustom();
        id = v2CustomContent.getMsgId();
        fromId = v2CustomContent.getFrom().getId();
        toId = v2CustomContent.getTo().getId();
        nickname = v2CustomContent.getFrom().getNickname();
        headImgUrl = v2CustomContent.getFrom().getImage();
        content = v2CustomContent.getBody().getContent();
        type = v2CustomContent.getBody().getType();
        createdTime = v2CustomContent.getCreatedTime();
        direct = Direct.getDirect(fromId == toId);
    }

    public enum Direct {
        SEND, RECEIVE;

        public static Direct getDirect(boolean n) {
            if (n) {
                return SEND;
            } else {
                return RECEIVE;
            }
        }
    }

    public static class Builder {

        private Chat mChat;

        public Builder() {
            mChat = new Chat();
        }

        public Builder addToId(int toId) {
            mChat.toId = toId;
            return this;
        }

        public Builder addFromId(int fromId) {
            mChat.fromId = fromId;
            return this;
        }

        public Builder addNickname(String nickname) {
            mChat.nickname = nickname;
            return this;
        }

        public Builder addAvatar(String avatar) {
            mChat.headImgUrl = avatar;
            return this;
        }

        public Builder addContent(String content) {
            mChat.content = content;
            return this;
        }

        public Builder addType(String type) {
            mChat.type = type;
            return this;
        }

        public Builder addMessageId(String mid) {
            mChat.mid = mid;
            return this;
        }

        public Builder addCreatedTime(long createdTime) {
            mChat.createdTime = createdTime;
            return this;
        }

        public Chat builder() {
            return mChat;
        }
    }
}
