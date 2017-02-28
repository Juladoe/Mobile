package com.edusoho.longinus.persenter;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.edusoho.kuozhi.imserver.entity.MessageEntity;
import com.edusoho.kuozhi.imserver.entity.Role;
import com.edusoho.kuozhi.imserver.entity.message.Destination;
import com.edusoho.kuozhi.imserver.entity.message.MessageBody;
import com.edusoho.kuozhi.imserver.managar.IMConvManager;
import com.edusoho.kuozhi.imserver.managar.IMRoleManager;
import com.edusoho.kuozhi.imserver.ui.IMessageListView;
import com.edusoho.kuozhi.imserver.ui.MessageListPresenterImpl;
import com.edusoho.kuozhi.imserver.ui.data.IMessageDataProvider;
import com.edusoho.kuozhi.imserver.ui.helper.MessageResourceHelper;
import com.edusoho.kuozhi.imserver.ui.view.MessageInputView;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.model.bal.User;
import com.edusoho.kuozhi.v3.model.provider.UserProvider;
import com.edusoho.kuozhi.v3.util.ApiTokenUtil;
import com.edusoho.kuozhi.v3.util.AppUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by suju on 16/10/18.
 */
public class LiveChatMessageListPresenterImpl extends MessageListPresenterImpl {

    private Bundle mLiveData;
    private Context mContext;
    private String mConversationNo;
    private Map<String, MessageBody> mNotSendMessageMap;

    public LiveChatMessageListPresenterImpl(
                                            Context context,
                                            Bundle params,
                                            IMConvManager convManager,
                                            IMRoleManager roleManager,
                                            MessageResourceHelper messageResourceHelper,
                                            IMessageDataProvider mIMessageDataProvider,
                                            IMessageListView messageListView) {
        super(params, convManager, roleManager, messageResourceHelper, mIMessageDataProvider, messageListView);
        this.mContext = context;
        mNotSendMessageMap = new ConcurrentHashMap<>();
        messageListView.setInputTextMode(MessageInputView.INPUT_TEXT);
    }

    public void setLiveData(Bundle liveData) {
        this.mLiveData = liveData;
        mConversationNo = liveData.get("convNo").toString();
        String clientId = mLiveData.get("clientId").toString();
        String clientName = mLiveData.get("clientName").toString();
        setClientInfo(AppUtil.parseInt(clientId), clientName);
    }

    @Override
    protected Map<String, String> getRequestHeaders() {
        HashMap<String, String> map = new HashMap();
        String token = ApiTokenUtil.getApiToken(mContext);
        map.put("Auth-Token", TextUtils.isEmpty(token) ? "" : token);
        return map;
    }

    @Override
    protected void createRole(String type, int rid, MessageListPresenterImpl.RoleUpdateCallback callback) {
        createTargetRole(type, rid, callback);
    }

    @Override
    protected void createConvNo(MessageListPresenterImpl.ConvNoCreateCallback convNoCreateCallback) {
        convNoCreateCallback.onCreateConvNo(mConversationNo);
    }

    protected void createTargetRole(String type, int rid, final MessageListPresenterImpl.RoleUpdateCallback callback) {
        new UserProvider(mContext).getUserInfo(rid)
                .success(new NormalCallback<User>() {
                    @Override
                    public void success(User user) {
                        Role role = new Role();
                        if (user == null) {
                            callback.onCreateRole(role);
                            return;
                        }
                        role.setRid(user.id);
                        role.setAvatar(user.mediumAvatar);
                        role.setType(Destination.USER);
                        role.setNickname(user.nickname);
                        callback.onCreateRole(role);
                    }
                });
    }

    @Override
    public void addMessageReceiver() {
    }

    @Override
    public void removeReceiver() {
    }

    @Override
    public boolean canRefresh() {
        return false;
    }

    @Override
    public void sendMessageToServer(MessageBody messageBody) {
        mNotSendMessageMap.put(messageBody.getMessageId(), messageBody);
        super.sendMessageToServer(messageBody);
    }

    @Override
    public void reSendMessageList() {
        if (mNotSendMessageMap.isEmpty()) {
            return;
        }
        for (MessageBody messageBody : mNotSendMessageMap.values()) {
            MessageEntity messageEntity = mIMessageDataProvider.getMessageByUID(messageBody.getMessageId());
            updateMessageReceiveStatus(messageEntity, MessageEntity.StatusType.FAILED);
        }
    }

    protected void updateMessageSendStatus(MessageEntity messageEntity, int status) {
        ContentValues cv = new ContentValues();
        cv.put("status", status);
        mIMessageDataProvider.updateMessageFieldByUid(messageEntity.getUid(), cv);

        messageEntity = mIMessageDataProvider.getMessageByUID(messageEntity.getUid());
        mIMessageListView.updateMessageEntity(messageEntity);
    }

    @Override
    public void onSendMessageAgain(MessageEntity messageEntity) {
        mNotSendMessageMap.put(messageEntity.getUid(), new MessageBody(messageEntity));

        messageEntity.setStatus(MessageEntity.StatusType.UPLOADING);
        updateMessageSendStatus(messageEntity, MessageEntity.StatusType.UPLOADING);
        mIMessageDataProvider.sendMessage(messageEntity);
    }

    @Override
    public boolean onReceiverMessageEntity(MessageEntity message) {
        String clientId = mLiveData.get("clientId").toString();
        String clientName = mLiveData.get("clientName").toString();

        if (clientId.equals(message.getFromId()) && clientName.equals(message.getFromName())) {
            return true;
        }

        message = mIMessageDataProvider.insertMessageEntity(message);
        if (message == null) {
            return true;
        }
        mIMessageListView.insertMessage(message);
        return true;
    }

    @Override
    public void onMessageSuccess(MessageEntity successEntity) {
        MessageEntity messageEntity = mIMessageDataProvider.getMessageByUID(successEntity.getUid());
        if (messageEntity == null) {
            return;
        }
        mNotSendMessageMap.remove(successEntity.getUid());
        messageEntity.setStatus(MessageEntity.StatusType.SUCCESS);
        mIMessageListView.updateMessageEntity(messageEntity);
    }
}
