package com.edusoho.kuozhi.imserver.entity;

/**
 * Created by 菊 on 2016/5/16.
 */
public class IMUploadEntity {

    private int messageId;
    private String type;
    private String source;

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
