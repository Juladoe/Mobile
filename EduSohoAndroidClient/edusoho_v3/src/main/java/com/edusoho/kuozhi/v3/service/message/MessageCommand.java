package com.edusoho.kuozhi.v3.service.message;

import android.content.Context;
import com.edusoho.kuozhi.imserver.IMClient;
import com.edusoho.kuozhi.imserver.entity.message.Destination;
import com.edusoho.kuozhi.imserver.entity.message.MessageBody;
import com.edusoho.kuozhi.imserver.listener.IMMessageReceiver;
import com.edusoho.kuozhi.v3.model.provider.IMProvider;

/**
 * Created by 菊 on 2016/4/25.
 */
public class MessageCommand extends AbstractCommand {

    public MessageCommand(Context context, String cmd, IMMessageReceiver receiver, MessageBody messageBody)
    {
        super(context, cmd, receiver, messageBody);
    }

    @Override
    public void invoke() {
        if (!isInBlackList(mMessageBody.getConvNo())
                && !IMClient.getClient().isHandleMessageInFront(Destination.USER, mMessageBody.getConvNo())) {
            showNotification(getNotificationContent(), getNotifyIntent());
        }

        String type = mMessageBody.getSource().getType();
        int targetId = mMessageBody.getSource().getId();
        new IMProvider(mContext).updateConvEntityByMessage(mMessageBody.getConvNo(), type, targetId);
    }

}
