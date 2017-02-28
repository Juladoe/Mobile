package com.edusoho.kuozhi.imserver.entity;

/**
 * Created by 菊 on 2016/4/26.
 */
public class ReceiverInfo {

    public String convNo;

    public String msgType;

    public boolean isProcessed;

    public ReceiverInfo(String msgType, String convNo)
    {
        this.convNo = convNo;
        this.msgType = msgType;
    }
}
