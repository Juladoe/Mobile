package com.edusoho.kuozhi.imserver.util;

import com.edusoho.kuozhi.imserver.SendEntity;

/**
 * Created by 菊 on 2016/4/27.
 */
public class SendEntityBuildr {

    private SendEntity sendEntity;

    public SendEntityBuildr()
    {
        this.sendEntity = new SendEntity();
    }

    public static SendEntityBuildr getBuilder() {
        return new SendEntityBuildr();
    }

    public SendEntityBuildr addToId(String toId) {
        sendEntity.setToId(toId);
        return this;
    }

    public SendEntityBuildr addConvNo(String convNo) {
        sendEntity.setConvNo(convNo);
        return this;
    }

    public SendEntityBuildr addMsg(String msg) {
        sendEntity.setMsg(msg);
        return this;
    }

    public SendEntityBuildr addCmd(String cmd) {
        sendEntity.setCmd(cmd);
        return this;
    }

    public SendEntityBuildr addToName(String toName) {
        sendEntity.setToName(toName);
        return this;
    }

    public SendEntityBuildr addK(String k) {
        sendEntity.setK(k);
        return this;
    }

    public SendEntity builder() {
        return sendEntity;
    }
}
