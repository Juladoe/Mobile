package com.edusoho.kuozhi.v3.ui.fragment;

import android.content.ContentValues;

import com.edusoho.kuozhi.imserver.IMClient;
import com.edusoho.kuozhi.imserver.SendEntity;
import com.edusoho.kuozhi.imserver.entity.MessageEntity;
import com.edusoho.kuozhi.imserver.entity.message.Destination;
import com.edusoho.kuozhi.imserver.entity.message.MessageBody;
import com.edusoho.kuozhi.imserver.entity.message.Source;
import com.edusoho.kuozhi.imserver.util.MessageEntityBuildr;
import com.edusoho.kuozhi.imserver.util.SendEntityBuildr;
import com.edusoho.kuozhi.v3.factory.FactoryManager;
import com.edusoho.kuozhi.v3.factory.UtilFactory;
import com.edusoho.kuozhi.v3.factory.provider.AppSettingProvider;
import com.edusoho.kuozhi.v3.model.bal.User;
import com.edusoho.kuozhi.v3.model.bal.push.RedirectBody;
import com.edusoho.kuozhi.v3.ui.base.BaseFragment;
import com.edusoho.kuozhi.v3.util.PushUtil;
import java.util.UUID;
import cn.trinea.android.common.util.ToastUtils;

/**
 * Created by suju on 16/8/19.
 */
public abstract class AbstractChatSendFragment extends BaseFragment {

    protected RedirectBody mRedirectBody;

    protected MessageBody createSendMessageBody(int fromId, String convNo, String type) {
        User currentUser = getAppSettingProvider().getCurrentUser();
        MessageBody messageBody = createMessageBody();
        messageBody.setCreatedTime(System.currentTimeMillis());
        messageBody.setDestination(new Destination(fromId, type));
        messageBody.getDestination().setNickname(currentUser.nickname);
        messageBody.setSource(new Source(currentUser.id, Destination.USER));
        messageBody.getSource().setNickname(currentUser.nickname);
        messageBody.setConvNo(convNo);
        messageBody.setMessageId(UUID.randomUUID().toString());
        return messageBody;
    }

    private MessageBody createMessageBody() {
        switch (mRedirectBody.type) {
            case PushUtil.ChatMsgType.TEXT:
            case PushUtil.ChatMsgType.IMAGE:
            case PushUtil.ChatMsgType.AUDIO:
                return new MessageBody(MessageBody.VERSION, mRedirectBody.type, mRedirectBody.content);
        }
        return new MessageBody(MessageBody.VERSION, PushUtil.ChatMsgType.MULTI, getUtilFactory().getJsonParser().jsonToString(mRedirectBody));
    }

    private MessageEntity createMessageEntityByBody(MessageBody messageBody) {
        return new MessageEntityBuildr()
                .addUID(messageBody.getMessageId())
                .addConvNo(messageBody.getConvNo())
                .addToId(String.valueOf(messageBody.getDestination().getId()))
                .addToName(messageBody.getDestination().getNickname())
                .addFromId(String.valueOf(messageBody.getSource().getId()))
                .addFromName(messageBody.getSource().getNickname())
                .addCmd("message")
                .addMsg(messageBody.toJson())
                .addTime((int) (messageBody.getCreatedTime() / 1000))
                .builder();
    }

    protected void sendMessageToServer(String convNo, MessageBody messageBody) {
        try {
            String toId = "";
            switch (messageBody.getDestination().getType()) {
                case Destination.CLASSROOM:
                case Destination.COURSE:
                    toId = "all";
                    break;
                case Destination.USER:
                    toId = String.valueOf(messageBody.getDestination().getId());
            }
            SendEntity sendEntity = SendEntityBuildr.getBuilder()
                    .addToId(toId)
                    .addCmd("send")
                    .addMsg(messageBody.toJson())
                    .builder();
            IMClient.getClient().getChatRoom(convNo).send(sendEntity);
            sendSuccessCallback();
            ToastUtils.show(mContext, "发送成功");
        } catch (Exception e) {
            ToastUtils.show(mContext, "发送失败");
            sendFailCallback();
        }
    }

    protected abstract void sendSuccessCallback();

    protected abstract void sendFailCallback();

    protected MessageBody saveMessageToLoacl(int fromId, String convNo, String type) {
        MessageBody messageBody = createSendMessageBody(fromId, convNo, type);

        MessageEntity messageEntity = createMessageEntityByBody(messageBody);
        IMClient.getClient().getMessageManager().createMessage(messageEntity);
        updateConv(messageBody);
        return messageBody;
    }

    private void updateConv(MessageBody messageBody) {
        ContentValues cv = new ContentValues();
        cv.put("laterMsg", messageBody.toJson());
        cv.put("updatedTime", System.currentTimeMillis());
        IMClient.getClient().getConvManager().updateConvField(messageBody.getConvNo(), cv);
    }

    protected RedirectBody getShowRedirectBody(String title, String icon) {
        RedirectBody redirectBody = new RedirectBody();
        redirectBody.title = mRedirectBody.title;
        switch (mRedirectBody.fromType) {
            case Destination.USER:
                redirectBody.content = title;
                redirectBody.image = icon;
                break;
            default:
                redirectBody.content = mRedirectBody.content;
                redirectBody.image = mRedirectBody.image;
        }

        return redirectBody;
    }

    protected AppSettingProvider getAppSettingProvider() {
        return FactoryManager.getInstance().create(AppSettingProvider.class);
    }

    protected UtilFactory getUtilFactory() {
        return FactoryManager.getInstance().create(UtilFactory.class);
    }
}
