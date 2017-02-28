package com.edusoho.kuozhi.v3.service.message;

import android.content.Context;
import com.edusoho.kuozhi.imserver.IMClient;
import com.edusoho.kuozhi.imserver.entity.ConvEntity;
import com.edusoho.kuozhi.imserver.entity.Role;
import com.edusoho.kuozhi.imserver.entity.message.Destination;
import com.edusoho.kuozhi.imserver.entity.message.MessageBody;
import com.edusoho.kuozhi.imserver.listener.IMMessageReceiver;
import com.edusoho.kuozhi.v3.service.message.push.ArticlePushProcessor;
import com.edusoho.kuozhi.v3.service.message.push.CoursePushProcessor;
import com.edusoho.kuozhi.v3.service.message.push.GlobalPushProcessor;
import com.edusoho.kuozhi.v3.service.message.push.IPushProcessor;
import com.edusoho.kuozhi.v3.service.message.push.LessonPushProcessor;

/**
 * Created by suju on 16/8/24.
 */
public class PushMsgCommand extends AbstractCommand {

    private static final String TAG = "PushMsgCommand";

    public PushMsgCommand(Context context, IMMessageReceiver receiver, MessageBody messageBody) {
        super(context, "push", receiver, messageBody);
    }

    @Override
    public void invoke() {
        IPushProcessor pushProcessor = null;
        String fromType = mMessageBody.getSource().getType();
        switch (fromType) {
            case Destination.ARTICLE:
                pushProcessor = new ArticlePushProcessor(mContext, mMessageBody);
                break;
            case Destination.GLOBAL:
                pushProcessor = new GlobalPushProcessor(mContext, mMessageBody);
                break;
            case Destination.COURSE:
                pushProcessor = new CoursePushProcessor(mContext, mMessageBody);
                break;
            case Destination.LESSON:
                pushProcessor = new LessonPushProcessor(mContext, mMessageBody);
        }

        String[] content = pushProcessor.getNotificationContent(mMessageBody.getBody());
        showNotification(content, pushProcessor.getNotifyIntent());
        pushProcessor.processor();
    }
}
