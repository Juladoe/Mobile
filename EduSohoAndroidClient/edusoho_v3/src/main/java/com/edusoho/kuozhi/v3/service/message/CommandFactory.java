package com.edusoho.kuozhi.v3.service.message;

import android.content.Context;

import com.edusoho.kuozhi.imserver.entity.message.MessageBody;
import com.edusoho.kuozhi.imserver.listener.IMMessageReceiver;
import com.edusoho.kuozhi.v3.util.PushUtil;

/**
 * Created by 菊 on 2016/4/25.
 */
public class CommandFactory {

    public static AbstractCommand create(Context context, String cmd, IMMessageReceiver receiver, MessageBody messageBody) {

        String toType = messageBody.getDestination().getType();
        String bodyType = messageBody.getType();

        switch (bodyType) {
            case PushUtil.ChatMsgType.AUDIO:
            case PushUtil.ChatMsgType.IMAGE:
            case PushUtil.ChatMsgType.TEXT:
            case PushUtil.ChatMsgType.MULTI:
                if (PushUtil.ChatUserType.CLASSROOM.equals(toType)) {
                    return new DiscussMsgCommand(context, cmd, receiver, messageBody);
                } else if (PushUtil.ChatUserType.USER.equals(toType)) {
                    return new MessageCommand(context, cmd, receiver, messageBody);
                } else if (PushUtil.ChatUserType.COURSE.equals(toType)) {
                    return new DiscussMsgCommand(context, cmd, receiver, messageBody);
                }
                break;
            case PushUtil.ChatMsgType.PUSH:
                return new PushMsgCommand(context, receiver, messageBody);
        }
        return new EmptyCommand(context, receiver, messageBody);
    }

    private static class EmptyCommand extends AbstractCommand
    {
        public EmptyCommand(Context context, IMMessageReceiver receiver, MessageBody messageBody)
        {
            super(context, "", receiver, messageBody);
        }

        @Override
        public void invoke() {
        }
    }
}
