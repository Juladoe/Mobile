package com.edusoho.kuozhi.v3.listener;

import com.edusoho.kuozhi.v3.model.sys.MessageModel;

/**
 * Created by JesseHuang on 15/4/23.
 */
public interface CoreEngineMsgCallback {
    void invoke(MessageModel obj);
}
