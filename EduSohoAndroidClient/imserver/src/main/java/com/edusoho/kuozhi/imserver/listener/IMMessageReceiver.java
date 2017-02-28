package com.edusoho.kuozhi.imserver.listener;

import com.edusoho.kuozhi.imserver.entity.MessageEntity;
import com.edusoho.kuozhi.imserver.entity.ReceiverInfo;
import java.util.List;

/**
 * Created by su on 2016/3/22.
 */
public interface IMMessageReceiver {

    boolean onReceiver(MessageEntity msg);

    boolean onOfflineMsgReceiver(List<MessageEntity> messageEntities);

    void onSuccess(MessageEntity messageEntity);

    ReceiverInfo getType();
}
