package com.edusoho.kuozhi.v3.model.sys;

import android.os.Bundle;

import com.edusoho.kuozhi.v3.listener.NormalCallback;

/**
 * Created by JesseHuang on 15/4/23.
 */
public class WidgetMessage {
    public MessageType type;
    public Bundle data;
    public Object target;
    public NormalCallback callback;

    public WidgetMessage(MessageType type, Bundle body) {
        this.type = type;
        this.data = body;
    }

    public WidgetMessage(
            MessageType type, Bundle body, NormalCallback callback) {
        this(type, body);
        this.callback = callback;
    }
}
