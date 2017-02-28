package com.edusoho.kuozhi.imserver.ui;

import android.content.ContentValues;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import com.edusoho.kuozhi.imserver.IMClient;
import com.edusoho.kuozhi.imserver.entity.ConvEntity;
import com.edusoho.kuozhi.imserver.entity.IMUploadEntity;
import com.edusoho.kuozhi.imserver.entity.MessageEntity;
import com.edusoho.kuozhi.imserver.entity.ReceiverInfo;
import com.edusoho.kuozhi.imserver.entity.Role;
import com.edusoho.kuozhi.imserver.entity.message.Destination;
import com.edusoho.kuozhi.imserver.entity.message.MessageBody;
import com.edusoho.kuozhi.imserver.entity.message.Source;
import com.edusoho.kuozhi.imserver.listener.IMMessageReceiver;
import com.edusoho.kuozhi.imserver.managar.IMConvManager;
import com.edusoho.kuozhi.imserver.managar.IMRoleManager;
import com.edusoho.kuozhi.imserver.ui.entity.AudioBody;
import com.edusoho.kuozhi.imserver.ui.entity.PushUtil;
import com.edusoho.kuozhi.imserver.ui.helper.MessageResourceHelper;
import com.edusoho.kuozhi.imserver.ui.data.IMessageDataProvider;
import com.edusoho.kuozhi.imserver.ui.listener.MessageControllerListener;
import com.edusoho.kuozhi.imserver.ui.util.AudioUtil;
import com.edusoho.kuozhi.imserver.ui.util.TaskFeature;
import com.edusoho.kuozhi.imserver.ui.util.UpYunUploadTask;
import com.edusoho.kuozhi.imserver.ui.util.UpdateRoleTask;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by suju on 16/9/16.
 */
public abstract class MessageListPresenterImpl implements IMessageListPresenter {

    public static final String TAG = "MessageListPresenter";
    private static final int EXPAID_TIME = 3600 * 1 * 1000;

    private int mStart = 0;
    private String mConversationNo;
    private int mTargetId;
    private String mTargetType;
    private Role mTargetRole;
    protected int mClientId;
    protected String mClientName;
    private Map<String, Boolean> mMessageFilterMap;

    private IMConvManager mIMConvManager;
    private IMRoleManager mIMRoleManager;
    private MessageResourceHelper mMessageResourceHelper;
    protected IMMessageReceiver mIMMessageReceiver;
    protected IMessageListView mIMessageListView;
    protected IMessageDataProvider mIMessageDataProvider;
    private MessageControllerListener mMessageControllerListener;

    public MessageListPresenterImpl(
            Bundle params,
            IMConvManager convManager,
            IMRoleManager roleManager,
            MessageResourceHelper messageResourceHelper,
            IMessageDataProvider mIMessageDataProvider,
            IMessageListView messageListView) {
        this.mIMConvManager = convManager;
        this.mIMRoleManager = roleManager;
        this.mMessageResourceHelper = messageResourceHelper;
        this.mIMessageDataProvider = mIMessageDataProvider;
        this.mIMessageListView = messageListView;
        this.mMessageFilterMap = new ConcurrentHashMap<>();
        initParams(params);
        checkConvNo();
        mIMessageListView.setPresenter(this);
    }

    @Override
    public void processResourceStatusChange(int resId, String resUri) {
        MessageEntity messageEntity = mIMessageDataProvider.getMessage(resId);
        if (messageEntity == null) {
            return;
        }

        if (resUri == null || TextUtils.isEmpty(resUri)) {
            updateMessageReceiveStatus(messageEntity, MessageEntity.StatusType.FAILED);
            return;
        }

        MessageBody messageBody = new MessageBody(messageEntity);
        String body = resUri;
        if (PushUtil.ChatMsgType.AUDIO.equals(messageBody.getType())) {
            AudioBody audioBody = AudioUtil.getAudioBody(messageBody.getBody());
            audioBody.setFile(resUri);
            body = audioBody.toString();
        }
        messageBody.setBody(body);
        sendMessageToServer(messageBody);
    }

    public void setClientInfo(int clientId, String clientName) {
        this.mClientId = clientId;
        this.mClientName = clientName;
    }

    @Override
    public boolean canRefresh() {
        return true;
    }

    @Override
    public void refresh() {
        mStart = 0;
        List<MessageEntity> messageEntityList = mIMessageDataProvider.getMessageList(mConversationNo, mStart);
        coverMessageEntityStatus(messageEntityList);
        mIMessageListView.setMessageList(messageEntityList);
    }

    @Override
    public void sendTextMessage(String content) {
        MessageBody messageBody = createSendMessageBody(content, PushUtil.ChatMsgType.TEXT);
        MessageEntity messageEntity = mIMessageDataProvider.createMessageEntity(messageBody);
        mStart++;
        messageEntity.setStatus(MessageEntity.StatusType.UPLOADING);
        insertMessageToList(messageEntity);
        sendMessageToServer(messageBody);
    }

    @Override
    public void deleteMessageById(int msgId) {
        mIMessageDataProvider.deleteMessageById(msgId);
    }

    @Override
    public void sendAudioMessage(File audioFile, int audioLength) {
        String content = wrapAudioMessageContent(audioFile.getAbsolutePath(), audioLength);
        MessageBody messageBody = createSendMessageBody(content, PushUtil.ChatMsgType.AUDIO);
        uploadMedia(audioFile, messageBody);
    }

    @Override
    public void sendImageMessage(File imageFile) {
        MessageBody messageBody = createSendMessageBody(imageFile.getAbsolutePath(), PushUtil.ChatMsgType.IMAGE);
        uploadMedia(imageFile, messageBody);
    }

    @Override
    public void removeReceiver() {
        IMClient.getClient().removeReceiver(mIMMessageReceiver);
        mMessageFilterMap.clear();
    }

    @Override
    public void onSendMessageAgain(MessageEntity messageEntity) {
        MessageBody messageBody = new MessageBody(messageEntity);
        switch (messageBody.getType()) {
            case PushUtil.ChatMsgType.TEXT:
            case PushUtil.ChatMsgType.MULTI:
                sendMessageToServer(messageBody);
                break;
            case PushUtil.ChatMsgType.AUDIO:
                messageEntity.setUid(messageBody.getMessageId());
                if (mClientId == messageBody.getSource().getId()) {
                    sendAudioMessageAgain(messageEntity);
                    return;
                }
                //receive
                receiveAudioMessageAgain(messageBody);
                break;
            case PushUtil.ChatMsgType.IMAGE:
                messageEntity.setUid(messageBody.getMessageId());
                if (mClientId == messageBody.getSource().getId()) {
                    sendImageMediaMessageAgain(messageEntity);
                    return;
                }
                receiveImageMessageAgain(messageBody);
        }
    }

    public void addMessageControllerListener(MessageControllerListener listener) {
        this.mMessageControllerListener = listener;
    }

    protected void coverMessageEntityStatus(List<MessageEntity> messageEntityList) {
        for (MessageEntity messageEntity : messageEntityList) {
            if (messageEntity.getStatus() != PushUtil.MsgDeliveryType.SUCCESS
                    && mMessageResourceHelper.hasTask(messageEntity.getId())) {
                messageEntity.setStatus(PushUtil.MsgDeliveryType.UPLOADING);
            }
        }
    }

    private void receiveImageMessageAgain(MessageBody messageBody) {
        try {
            mMessageResourceHelper.addImageDownloadTask(messageBody.getMid(), messageBody.getBody());
        } catch (IOException e) {
            mIMessageListView.notifiy("图片文件不存在,语音消息接受失败");
        }
    }

    private void receiveAudioMessageAgain(MessageBody messageBody) {
        AudioBody audioBody = AudioUtil.getAudioBody(messageBody.getBody());
        try {
            if (TextUtils.isEmpty(audioBody.getFile())) {
                mIMessageListView.notifiy("音频文件不存在,语音消息接受失败");
                return;
            }
            mMessageResourceHelper.addAudioDownloadTask(messageBody.getMid(), audioBody.getFile());
        } catch (IOException e) {
            mIMessageListView.notifiy("音频文件不存在,语音消息接受失败");
        }
    }

    private void uploadMediaAgain(File file, MessageEntity messageEntity) {
        try {
            messageEntity.setStatus(MessageEntity.StatusType.UPLOADING);
            mIMessageListView.updateMessageEntity(messageEntity);

            UpYunUploadTask upYunUploadTask = new UpYunUploadTask(messageEntity.getId(), mTargetId, file, getRequestHeaders());
            mMessageResourceHelper.addTask(upYunUploadTask);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void sendImageMediaMessageAgain(MessageEntity messageEntity) {
        IMUploadEntity uploadEntity = mIMessageDataProvider.getUploadEntity(messageEntity.getUid());
        if (uploadEntity == null) {
            mIMessageListView.notifiy("媒体文件不存在,请重新发送消息");
            return;
        }
        File audioFile = new File(uploadEntity.getSource());
        uploadMediaAgain(audioFile, messageEntity);
    }

    private void sendAudioMessageAgain(MessageEntity messageEntity) {
        IMUploadEntity uploadEntity = mIMessageDataProvider.getUploadEntity(messageEntity.getUid());
        if (uploadEntity == null) {
            mIMessageListView.notifiy("媒体文件不存在,请重新发送消息");
            return;
        }
        File audioFile = new File(uploadEntity.getSource());
        uploadMediaAgain(audioFile, messageEntity);
    }

    public void sendMessageToServer(MessageBody messageBody) {
        mIMessageDataProvider.sendMessage(mConversationNo, messageBody);
    }

    @Override
    public void processResourceDownload(int resId, String resUri) {
        MessageEntity messageEntity = mIMessageDataProvider.getMessage(resId);
        if (messageEntity == null) {
            return;
        }
        if (TextUtils.isEmpty(resUri)) {
            updateMessageReceiveStatus(messageEntity, MessageEntity.StatusType.FAILED);
            return;
        }

        MessageBody messageBody = new MessageBody(messageEntity);
        if (PushUtil.ChatMsgType.AUDIO.equals(messageBody.getType())) {
            updateMessageReceiveStatus(messageEntity, MessageEntity.StatusType.UNREAD);
            return;
        }
        updateMessageReceiveStatus(messageEntity, MessageEntity.StatusType.SUCCESS);
    }

    @Override
    public void uploadMedia(File file, MessageBody messageBody) {
        try {
            MessageEntity messageEntity = mIMessageDataProvider.createMessageEntity(messageBody);
            mStart++;
            messageEntity.setStatus(MessageEntity.StatusType.UPLOADING);

            if (messageEntity.getStatus() != PushUtil.MsgDeliveryType.SUCCESS
                    && mMessageResourceHelper.hasTask(messageEntity.getId())) {
                messageEntity.setStatus(PushUtil.MsgDeliveryType.UPLOADING);
            }
            mIMessageListView.insertMessage(messageEntity);
            mIMessageDataProvider.saveUploadEntity(
                    messageBody.getMessageId(), messageBody.getType(), file.getPath()
            );

            UpYunUploadTask upYunUploadTask = new UpYunUploadTask(messageEntity.getId(), mTargetId, file, getRequestHeaders());
            mMessageResourceHelper.addTask(upYunUploadTask);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void insertMessageList() {
        List<MessageEntity> messageEntityList = loadMessageList();
        coverMessageEntityStatus(messageEntityList);
        mIMessageListView.insertMessageList(messageEntityList);
    }

    @Override
    public void addMessageReceiver() {
        if (mIMMessageReceiver == null) {
            mIMMessageReceiver = getIMMessageListener();
        }

        IMClient.getClient().addMessageReceiver(mIMMessageReceiver);
        mIMConvManager.clearReadCount(mConversationNo);
    }

    @Override
    public void updateMessageReceiveStatus(MessageEntity messageEntity, int status) {
        ContentValues cv = new ContentValues();
        cv.put("status", status);
        mIMessageDataProvider.updateMessageFieldByMsgNo(messageEntity.getMsgNo(), cv);
        messageEntity.setStatus(status);
        mIMessageListView.updateMessageEntity(messageEntity);
    }

    @Override
    public void unEnableChatView() {
        mIMessageListView.setEnable(false);
    }

    @Override
    public void enableChatView() {
        mIMessageListView.setEnable(true);
    }

    @Override
    public void start() {
        if (convNoIsEmpty(mConversationNo)) {
            createConvNo();
            return;
        }

        ConvEntity convEntity = mIMConvManager.getConvByConvNo(mConversationNo);
        if (convEntity == null) {
            createConvNo();
            return;
        }
        if ((System.currentTimeMillis() - convEntity.getUpdatedTime() > 3600000)) {
            valideConvNo();
            return;
        }
        checkTargetRole();
    }

    private void valideConvNo() {
        createConvNo(new ConvNoCreateCallback() {
            @Override
            public void onCreateConvNo(String convNo) {
                if (!convNoIsEmpty(convNo)) {
                    mConversationNo = convNo;
                }
                checkTargetRole();
            }
        });
    }

    private void createConvNo() {
        createConvNo(new ConvNoCreateCallback() {
            @Override
            public void onCreateConvNo(String convNo) {
                if (convNoIsEmpty(convNo)) {
                    Log.d(TAG, "mConversationNo is null");
                    return;
                }
                Log.d(TAG, "onCreateConvNo " + convNo);
                mConversationNo = convNo;
                checkTargetRole();
            }
        });
    }

    protected void insertMessageToList(MessageEntity messageEntity) {
        if (messageEntity.getStatus() != PushUtil.MsgDeliveryType.SUCCESS
                && mMessageResourceHelper.hasTask(messageEntity.getId())) {
            messageEntity.setStatus(PushUtil.MsgDeliveryType.UPLOADING);
        }
        mIMessageListView.insertMessage(messageEntity);
    }

    private String wrapAudioMessageContent(String audioFilePath, int audioTime) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("f", audioFilePath);
            jsonObject.put("d", audioTime);
        } catch (JSONException e) {
        }

        return jsonObject.toString();
    }

    protected MessageBody createSendMessageBody(String content, String type) {
        MessageBody messageBody = new MessageBody(1, type, content);
        messageBody.setCreatedTime(System.currentTimeMillis());
        messageBody.setDestination(new Destination(mTargetRole.getRid(), mTargetRole.getType()));
        messageBody.getDestination().setNickname(mTargetRole.getNickname());
        messageBody.setSource(new Source(mClientId, Destination.USER));
        messageBody.getSource().setNickname(mClientName);
        messageBody.setConvNo(mConversationNo);
        messageBody.setMessageId(UUID.randomUUID().toString());

        return messageBody;
    }

    private void checkConvEntity(Role role) {
        ConvEntity convEntity = mIMConvManager.getConvByConvNo(mConversationNo);
        if (convEntity == null) {
            Log.d("DefautlMessageProvider", "create ConvNo");
            convEntity = createConvNo(mConversationNo, role);
        }

        if ((System.currentTimeMillis() - convEntity.getUpdatedTime()) > EXPAID_TIME) {
            Log.d("DefautlMessageProvider", "update ConvNo");
            convEntity.setAvatar(role.getAvatar());
            convEntity.setTargetName(role.getNickname());
            convEntity.setUpdatedTime(System.currentTimeMillis());
            mIMConvManager.updateConvByConvNo(convEntity);
        }
    }

    private ConvEntity createConvNo(String convNo, Role role) {
        ConvEntity convEntity = new ConvEntity();
        convEntity.setTargetId(role.getRid());
        convEntity.setTargetName(role.getNickname());
        convEntity.setConvNo(convNo);
        convEntity.setType(role.getType());
        convEntity.setAvatar(role.getAvatar());
        convEntity.setCreatedTime(System.currentTimeMillis());
        convEntity.setUpdatedTime(0);
        mIMConvManager.createConv(convEntity);

        return convEntity;
    }

    private List<MessageEntity> loadMessageList() {
        List<MessageEntity> messageEntityList = mIMessageDataProvider.getMessageList(mConversationNo, mStart);
        mStart += messageEntityList.size();
        return messageEntityList;
    }

    private void checkTargetRole() {
        if (mTargetRole.getRid() != 0) {
            checkConvEntity(mTargetRole);

            List<MessageEntity> messageEntityList = loadMessageList();
            coverMessageEntityStatus(messageEntityList);
            mIMessageListView.setMessageList(messageEntityList);
            return;
        }
        createRole(mTargetType, mTargetId, new RoleUpdateCallback() {
            @Override
            public void onCreateRole(Role role) {
                if (role.getRid() == 0) {
                    Log.d(TAG, "mTargetRole is null");
                    return;
                }
                Log.d(TAG, "mTargetRole " + role.getRid());
                mTargetRole = role;
                mIMRoleManager.createRole(role);
                checkConvEntity(role);

                List<MessageEntity> messageEntityList = loadMessageList();
                coverMessageEntityStatus(messageEntityList);
                mIMessageListView.setMessageList(messageEntityList);
            }
        });
    }

    protected abstract Map<String, String> getRequestHeaders();

    protected abstract void createRole(String type, int rid, RoleUpdateCallback callback);

    protected abstract void createConvNo(ConvNoCreateCallback convNoCreateCallback);

    private void initParams(Bundle bundle) {
        if (bundle == null) {
            return;
        }
        mConversationNo = bundle.getString(CONV_NO);
        mTargetId = bundle.getInt(TARGET_ID, 0);
        mTargetType = bundle.getString(TARGET_TYPE);

        mTargetRole = mIMRoleManager.getRole(mTargetType, mTargetId);
    }

    /**
     * 检查是否有convNo
     */
    private void checkConvNo() {
        ConvEntity convEntity = mIMConvManager.getConvByTypeAndId(mTargetType, mTargetId);
        if (convNoIsEmpty(mConversationNo) && convEntity != null) {
            mConversationNo = convEntity.getConvNo();
        }
    }

    protected void updateMessageSendStatus(MessageBody messageBody) {
        ContentValues cv = new ContentValues();
        cv.put("status", MessageEntity.StatusType.SUCCESS);
        mIMessageDataProvider.updateMessageFieldByUid(messageBody.getMessageId(), cv);

        MessageEntity messageEntity = mIMessageDataProvider.getMessageByUID(messageBody.getMessageId());
        mIMessageListView.updateMessageEntity(messageEntity);
    }

    protected boolean convNoIsEmpty(String convNo) {
        return TextUtils.isEmpty(convNo) || "0".equals(convNo);
    }

    protected synchronized boolean filterReceiverdMessage(MessageEntity messageEntity) {
        if (TextUtils.isEmpty(messageEntity.getMsgNo())) {
            return false;
        }

        boolean result = mMessageFilterMap.containsKey(messageEntity.getMsgNo());
        if (!result) {
            addMessageFilter(messageEntity.getMsgNo());
        }
        return result;
    }

    protected List<MessageEntity> filterReceiverdMessageList(List<MessageEntity> filterList) {
        List<MessageEntity> messageEntityList = new ArrayList<>();
        for (MessageEntity filter : filterList) {
            if (filterReceiverdMessage(filter)) {
                continue;
            }
            messageEntityList.add(filter);
        }

        return messageEntityList;
    }

    private void addMessageFilter(String msgNo) {
        if (mMessageFilterMap.size() > 300) {
            mMessageFilterMap.clear();
        }
        mMessageFilterMap.put(msgNo, true);
    }

    protected IMMessageReceiver getIMMessageListener() {
        return new IMMessageReceiver() {
            @Override
            public boolean onReceiver(MessageEntity msg) {
                return onReceiverMessageEntity(msg);
            }

            @Override
            public boolean onOfflineMsgReceiver(List<MessageEntity> messageEntities) {
                messageEntities = filterReceiverdMessageList(messageEntities);
                coverMessageEntityStatus(messageEntities);
                mIMessageListView.insertMessageList(messageEntities);
                mIMConvManager.clearReadCount(mConversationNo);
                return false;
            }

            @Override
            public void onSuccess(MessageEntity messageEntity) {
                onMessageSuccess(messageEntity);
            }

            @Override
            public ReceiverInfo getType() {
                return new ReceiverInfo(mTargetType, mConversationNo);
            }
        };
    }

    @Override
    public boolean onReceiverMessageEntity(MessageEntity msg) {
        if (!mConversationNo.equals(msg.getConvNo()) || filterReceiverdMessage(msg)) {
            return true;
        }

        mIMessageListView.insertMessage(msg);
        mIMConvManager.clearReadCount(mConversationNo);
        return true;
    }

    @Override
    public void onMessageSuccess(MessageEntity messageEntity) {
        MessageBody messageBody = new MessageBody(messageEntity.getMsg());
        if (messageBody == null) {
            return;
        }
        messageBody.setConvNo(mConversationNo);
        updateMessageSendStatus(messageBody);
    }

    @Override
    public void onShowActivity(Bundle bundle) {
        String type = bundle.getString("type");
        if (TextUtils.isEmpty(type)) {
            return;
        }

        switch (type) {
            case "webpage":
                mMessageControllerListener.onShowWebPage(bundle.getString("url"));
                return;
            case "showImage":
                int index = bundle.getInt("index");
                ArrayList<String> imageUrls = bundle.getStringArrayList("imageList");
                mMessageControllerListener.onShowImage(index, imageUrls);
                return;
        }
        mMessageControllerListener.onShowActivity(bundle);
    }

    @Override
    public void onShowUser(int userId) {
        Role role = mIMRoleManager.getRole(Destination.USER, userId);
        role.setRid(userId);
        mMessageControllerListener.onShowUser(role);
    }

    @Override
    public void updateRole(String type, int rid) {
        Role role = mIMRoleManager.getRole(type, rid);
        if (role.getRid() != 0) {
            return;
        }
        new Handler().postDelayed(new UpdateRoleRunnable(type, rid), 200);
    }

    @Override
    public void selectPhoto(String action) {
        if ("take".equals(action)) {
            mMessageControllerListener.takePhoto();
            return;
        }

        mMessageControllerListener.selectPhoto();
    }

    private class UpdateRoleRunnable implements Runnable {

        private String type;
        private int rid;

        public UpdateRoleRunnable(String type, int rid) {
            this.type = type;
            this.rid = rid;
        }

        @Override
        public void run() {
            UpdateRoleTask task = new UpdateRoleTask(type, rid, new UpdateRoleTask.TaskCallback() {
                @Override
                public void run(final TaskFeature taskFeature) {
                    createRole(type, rid, new RoleUpdateCallback() {
                        @Override
                        public void onCreateRole(Role role) {
                            if (role.getRid() != 0) {
                                mIMRoleManager.createRole(role);
                                Log.d(TAG, "create role:" + rid);
                                mIMessageListView.notifyDataSetChanged();
                                taskFeature.success(null);
                                return;
                            }
                            taskFeature.fail();
                        }
                    });
                }
            });
            mMessageResourceHelper.addTask(task);
        }
    }

    public interface RoleUpdateCallback {
        void onCreateRole(Role role);
    }

    public interface ConvNoCreateCallback {
        void onCreateConvNo(String convNo);
    }

    @Override
    public void reSendMessageList() {
    }
}
