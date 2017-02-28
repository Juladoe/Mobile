package com.edusoho.kuozhi.v3.ui.fragment;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.imserver.IMClient;
import com.edusoho.kuozhi.imserver.SendEntity;
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
import com.edusoho.kuozhi.imserver.util.MessageEntityBuildr;
import com.edusoho.kuozhi.imserver.util.SendEntityBuildr;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.adapter.ChatAdapter;
import com.edusoho.kuozhi.v3.adapter.CourseDiscussAdapter;
import com.edusoho.kuozhi.v3.broadcast.AudioDownloadReceiver;
import com.edusoho.kuozhi.v3.factory.FactoryManager;
import com.edusoho.kuozhi.v3.factory.NotificationProvider;
import com.edusoho.kuozhi.v3.factory.UtilFactory;
import com.edusoho.kuozhi.v3.factory.provider.AppSettingProvider;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.model.bal.User;
import com.edusoho.kuozhi.v3.model.bal.course.CourseDetailsResult;
import com.edusoho.kuozhi.v3.model.bal.push.Chat;
import com.edusoho.kuozhi.v3.model.bal.push.UpYunUploadResult;
import com.edusoho.kuozhi.v3.model.provider.CourseProvider;
import com.edusoho.kuozhi.v3.model.provider.IMProvider;
import com.edusoho.kuozhi.v3.model.sys.MessageType;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.model.sys.WidgetMessage;
import com.edusoho.kuozhi.v3.ui.NewsCourseActivity;
import com.edusoho.kuozhi.v3.ui.base.BaseFragment;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.ChatAudioRecord;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.PushUtil;
import com.edusoho.kuozhi.v3.view.EduSohoIconView;
import com.edusoho.kuozhi.v3.view.dialog.LoadDialog;
import com.google.gson.reflect.TypeToken;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import cn.trinea.android.common.util.ToastUtils;
import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;

/**
 * Created by JesseHuang on 15/12/14.
 */
public class IMDiscussFragment extends BaseFragment implements
        View.OnClickListener, View.OnTouchListener, View.OnFocusChangeListener, ChatAdapter.ImageErrorClick {
    private static final String TAG = "DiscussFragment";

    private int mToId;
    private int mCourseId;

    private IMMessageReceiver mIMMessageReceiver;
    private ChatAdapter<Chat> mAdapter;

    protected EduSohoIconView btnVoice;
    protected EduSohoIconView btnKeyBoard;
    protected EditText etSend;
    protected ListView lvMessage;
    protected Button btnSend;
    protected EduSohoIconView ivAddMedia;
    protected PtrClassicFrameLayout mPtrFrame;
    protected View viewMediaLayout;
    protected View viewPressToSpeak;
    protected View viewMsgInput;
    /**
     * 语音录制按钮
     */
    protected TextView tvSpeak;
    protected TextView tvSpeakHint;
    protected View mViewSpeakContainer;
    protected ImageView ivRecordImage;

    protected float mPressDownY;
    protected MediaRecorderTask mMediaRecorderTask;
    protected VolumeHandler mHandler;
    protected AudioDownloadReceiver mAudioDownloadReceiver;

    protected int[] mSpeakerAnimResId = new int[]{R.drawable.record_animate_1,
            R.drawable.record_animate_2,
            R.drawable.record_animate_3,
            R.drawable.record_animate_4};

    private static final int SEND_IMAGE = 1;
    private static final int SEND_CAMERA = 2;

    protected long mSendTime;
    protected int mStart = 0;
    private String mConversationNo;
    private Role mTargetRole;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContainerView(R.layout.activity_chat);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mIMMessageReceiver == null) {
            mIMMessageReceiver = getIMMessageListener();
        }

        IMClient.getClient().addMessageReceiver(mIMMessageReceiver);
        IMClient.getClient().getConvManager().clearReadCount(mConversationNo);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        mContext.registerReceiver(mAudioDownloadReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mIMMessageReceiver != null) {
            IMClient.getClient().removeReceiver(mIMMessageReceiver);
        }
    }

    protected IMMessageReceiver getIMMessageListener() {
        return new IMMessageReceiver() {
            @Override
            public boolean onReceiver(MessageEntity msg) {
                if (!mConversationNo.equals(msg.getConvNo())) {
                    return true;
                }
                handleMessage(msg);
                return true;
            }

            @Override
            public boolean onOfflineMsgReceiver(List<MessageEntity> messageEntities) {
                handleOfflineMessage(messageEntities);
                return false;
            }

            @Override
            public void onSuccess(MessageEntity messageEntity) {
                MessageBody messageBody = new MessageBody(messageEntity);
                if (messageBody == null) {
                    return;
                }
                messageBody.setConvNo(mConversationNo);
                updateMessageSendStatus(messageBody);
            }

            @Override
            public ReceiverInfo getType() {
                return new ReceiverInfo(Destination.COURSE, mConversationNo);
            }
        };
    }

    protected void updateMessageSendStatus(MessageBody messageBody) {
        Chat chat = new Chat(messageBody);
        updateSendMsgToListView(PushUtil.MsgDeliveryType.SUCCESS, chat);

        ContentValues cv = new ContentValues();
        cv.put("status", MessageEntity.StatusType.SUCCESS);
        IMClient.getClient().getMessageManager().updateMessageFieldByUid(messageBody.getMessageId(), cv);
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
                .addTime((int)(messageBody.getCreatedTime() / 1000))
                .builder();
    }

    protected void handleOfflineMessage(List<MessageEntity> messageEntityList) {
        ArrayList<Chat> chatList = new ArrayList<>();
        for (MessageEntity messageEntity : messageEntityList) {
            MessageBody messageBody = new MessageBody(messageEntity);
            Chat chat = new Chat(messageBody);
            if (mTargetRole != null) {
                chat.headImgUrl = mTargetRole.getAvatar();
            }
            chatList.add(chat);
        }

        mAdapter.addItems(chatList);
    }

    protected void handleMessage(MessageEntity messageEntity) {
        MessageBody messageBody = new MessageBody(messageEntity);
        Chat chat = new Chat(messageBody);
        Role role = IMClient.getClient().getRoleManager().getRole(Destination.USER, chat.fromId);
        chat.headImgUrl = role.getAvatar();
        mAdapter.addItem(chat);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            mAdapter = new CourseDiscussAdapter<>(mContext, getChatList(0));
            mAdapter.setSendImageClickListener(this);
            lvMessage.setAdapter(mAdapter);
            mStart = mAdapter.getCount();
            lvMessage.postDelayed(mListViewSelectRunnable, 500);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }

    @Override
    protected void initView(View view) {
        mHandler = new VolumeHandler(this);
        mAudioDownloadReceiver = new AudioDownloadReceiver();
        etSend = (EditText) view.findViewById(R.id.et_send_content);
        etSend.addTextChangedListener(msgTextWatcher);
        etSend.setOnFocusChangeListener(this);
        etSend.setOnClickListener(this);
        btnSend = (Button) view.findViewById(R.id.btn_send);
        btnSend.setOnClickListener(this);
        lvMessage = (ListView) view.findViewById(R.id.lv_messages);
        lvMessage.setOnTouchListener(this);
        ivAddMedia = (EduSohoIconView) view.findViewById(R.id.iv_show_media_layout);
        ivAddMedia.setOnClickListener(this);
        viewMediaLayout = view.findViewById(R.id.ll_media_layout);
        btnVoice = (EduSohoIconView) view.findViewById(R.id.btn_voice);
        btnVoice.setOnClickListener(this);
        btnKeyBoard = (EduSohoIconView) view.findViewById(R.id.btn_set_mode_keyboard);
        btnKeyBoard.setOnClickListener(this);
        viewPressToSpeak = view.findViewById(R.id.rl_btn_press_to_speak);
        viewPressToSpeak.setOnClickListener(this);
        viewMsgInput = view.findViewById(R.id.rl_msg_input);
        EduSohoIconView ivPhoto = (EduSohoIconView) view.findViewById(R.id.iv_image);
        ivPhoto.setOnClickListener(this);
        EduSohoIconView ivCamera = (EduSohoIconView) view.findViewById(R.id.iv_camera);
        ivCamera.setOnClickListener(this);
        viewPressToSpeak.setOnTouchListener(this);
        tvSpeak = (TextView) view.findViewById(R.id.tv_speak);
        tvSpeakHint = (TextView) view.findViewById(R.id.tv_speak_hint);
        ivRecordImage = (ImageView) view.findViewById(R.id.iv_voice_volume);
        mViewSpeakContainer = view.findViewById(R.id.recording_container);
        mViewSpeakContainer.bringToFront();
        mPtrFrame = (PtrClassicFrameLayout) view.findViewById(R.id.rotate_header_list_view_frame);
    }

    protected String getTargetType() {
        return Destination.COURSE;
    }

    private void initConvNoInfo() {
        if (TextUtils.isEmpty(mConversationNo)) {
            IMConvManager imConvManager = IMClient.getClient().getConvManager();
            ConvEntity convEntity = imConvManager.getConvByTypeAndId(getTargetType(), mCourseId);
            if (convEntity != null) {
                mConversationNo = convEntity.getConvNo();
            }
        }

        mTargetRole = IMClient.getClient().getRoleManager().getRole(getTargetType(), mCourseId);
    }

    protected void initData() {
        mCourseId = getArguments().getInt(Const.COURSE_ID, 0);
        mConversationNo = getArguments().getString(NewsCourseActivity.CONV_NO);
        if (mCourseId == 0) {
            CommonUtil.longToast(mContext, "聊天记录读取错误");
            return;
        }

        User user = getAppSettingProvider().getCurrentUser();
        mToId = user.id;
        initCacheFolder();
        initConvNoInfo();

        if (convNoIsEmpty(mConversationNo)) {
            createChatConvNo();
            return;
        }

        initAdapter();
        if (! convNoIsEmpty(mConversationNo)) {
            getNotificationProvider().cancelNotification(mConversationNo.hashCode());
        }
    }

    protected boolean convNoIsEmpty(String convNo) {
        return TextUtils.isEmpty(convNo) || "0".equals(convNo);
    }

    protected void createChatConvNo() {
        final LoadDialog loadDialog = LoadDialog.create(getActivity());
        loadDialog.show();
        new CourseProvider(mContext).getCourse(mCourseId)
        .success(new NormalCallback<CourseDetailsResult>() {
            @Override
            public void success(CourseDetailsResult courseDetailsResult) {
                String conversationNo = null;
                if (courseDetailsResult == null
                        || courseDetailsResult.course == null
                        || convNoIsEmpty(courseDetailsResult.course.convNo)
                        ) {
                    ToastUtils.show(mContext, "创建聊天失败!");
                    return;
                }

                mConversationNo = conversationNo;
                new IMProvider(mContext).createConvInfoByCourse(mConversationNo, courseDetailsResult.course)
                        .success(new NormalCallback<ConvEntity>() {
                            @Override
                            public void success(ConvEntity convEntity) {
                                loadDialog.dismiss();
                                getActivity().setTitle(convEntity.getTargetName());
                                initAdapter();
                            }
                        });
            }
        });
    }

    private void initAdapter() {
        mAdapter = new CourseDiscussAdapter(mContext, getChatList(0));
        mAdapter.setSendImageClickListener(this);
        lvMessage.setAdapter(mAdapter);
        mAudioDownloadReceiver.setAdapter(mAdapter);
        mStart = mAdapter.getCount();
        lvMessage.postDelayed(mListViewSelectRunnable, 500);
        mPtrFrame.setLastUpdateTimeRelateObject(this);
        mPtrFrame.setPtrHandler(new PtrHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                mAdapter.addItems(getChatList(mStart));
                mStart = mAdapter.getCount();
                mPtrFrame.refreshComplete();
                //lvMessage.postDelayed(mListViewSelectRunnable, 100);
            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                boolean canDoRefresh = PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
                int count = getChatList(mStart).size();
                return count > 0 && canDoRefresh;
            }
        });
    }

    private ArrayList<Chat> getChatList(int start) {
        List<MessageEntity> messageEntityList = IMClient.getClient().getChatRoom(mConversationNo).getMessageList(start);
        ArrayList<Chat> chats = new ArrayList<>();
        User currentUser = getAppSettingProvider().getCurrentUser();
        for (MessageEntity messageEntity : messageEntityList) {
            MessageBody messageBody = new MessageBody(messageEntity);
            Chat chat = new Chat(messageBody);
            chat.id = messageEntity.getId();
            Role role = IMClient.getClient().getRoleManager().getRole(messageBody.getSource().getType(), chat.fromId);
            chat.setDirect(chat.fromId == currentUser.id ? Chat.Direct.SEND : Chat.Direct.RECEIVE);
            chat.headImgUrl = role.getAvatar();
            if (messageEntity.getStatus() != PushUtil.MsgDeliveryType.NONE) {
                chat.delivery = messageEntity.getStatus();
            }
            chats.add(chat);
        }
        Collections.reverse(chats);
        return chats;
    }

    protected TextWatcher msgTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!TextUtils.isEmpty(s)) {
                btnSend.setVisibility(View.VISIBLE);
                ivAddMedia.setVisibility(View.GONE);
            } else {
                ivAddMedia.setVisibility(View.VISIBLE);
                btnSend.setVisibility(View.GONE);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private void sendMsg(String content) {
        MessageBody messageBody = saveMessageToLoacl(content, PushUtil.ChatMsgType.TEXT);
        etSend.setText("");
        etSend.requestFocus();
        sendMessageToServer(messageBody);
    }

    protected MessageBody createSendMessageBody(String content, String type) {
        User currentUser = getAppSettingProvider().getCurrentUser();
        MessageBody messageBody = new MessageBody(1, type, content);
        messageBody.setCreatedTime(System.currentTimeMillis());
        messageBody.setDestination(new Destination(mCourseId, getTargetType()));
        messageBody.getDestination().setNickname(currentUser.nickname);
        messageBody.setSource(new Source(mToId, Destination.USER));
        messageBody.getSource().setNickname(mTargetRole.getNickname());
        messageBody.setConvNo(mConversationNo);
        messageBody.setMessageId(UUID.randomUUID().toString());

        return messageBody;
    }

    protected MessageBody saveMessageToLoacl(String content, String type) {
        MessageBody messageBody = createSendMessageBody(content, type);
        mSendTime = messageBody.getCreatedTime();

        MessageEntity messageEntity = createMessageEntityByBody(messageBody);
        IMClient.getClient().getMessageManager().createMessage(messageEntity);
        updateConv(messageBody);

        User currentUser = getAppSettingProvider().getCurrentUser();
        Chat chat = new Chat.Builder()
                .addToId(mToId)
                .addFromId(mCourseId)
                .addAvatar(currentUser.mediumAvatar)
                .addContent(messageBody.getBody())
                .addNickname(currentUser.nickname)
                .addType(messageBody.getType())
                .addMessageId(messageBody.getMessageId())
                .addCreatedTime(mSendTime).builder();
        addSendMsgToListView(PushUtil.MsgDeliveryType.UPLOADING, chat);

        return messageBody;
    }

    private void updateConv(MessageBody messageBody) {
        ContentValues cv = new ContentValues();
        cv.put("laterMsg", messageBody.toJson());
        cv.put("updatedTime", System.currentTimeMillis());
        IMClient.getClient().getConvManager().updateConvField(mConversationNo, cv);
    }

    protected void sendMessageToServer(MessageBody messageBody) {
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
            IMClient.getClient().getChatRoom(mConversationNo).send(sendEntity);
        } catch (Exception e) {
        }
    }

    @Override
    public void sendMsgAgain(Chat chat) {
        MessageBody messageBody = new MessageBody(1, chat.type, chat.content);
        messageBody.setConvNo(mConversationNo);
        messageBody.setMessageId(chat.mid);
        messageBody.setCreatedTime(chat.createdTime);
        messageBody.setDestination(new Destination(chat.fromId, Destination.USER));
        messageBody.setSource(new Source(chat.toId, Destination.USER));

        updateSendMsgToListView(PushUtil.MsgDeliveryType.UPLOADING, chat);
        sendMessageToServer(messageBody);
    }

    public void sendMediaMsg(final Chat model, String type) {
    }

    private void uploadMedia(final File file, final String type, String strType) {
        if (file == null || !file.exists()) {
            CommonUtil.shortToast(mContext, String.format("%s不存在", strType));
            return;
        }
        try {
            mSendTime = System.currentTimeMillis();
            String content = file.getAbsolutePath();
            if (PushUtil.ChatMsgType.AUDIO.equals(type)) {
                content = wrapAudioMessageContent(file.getAbsolutePath(), getAudioDuration(file.getAbsolutePath()));
            }
            MessageBody messageBody = saveMessageToLoacl(content, type);
            IMClient.getClient().getMessageManager().saveUploadEntity(
                    messageBody.getMessageId(), messageBody.getType(), file.getPath()
            );
            getUpYunUploadInfo(file, mCourseId, new UpYunUploadCallback(messageBody));
            viewMediaLayout.setVisibility(View.GONE);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private int getAudioDuration(String audioFile) {
        MediaPlayer mediaPlayer = MediaPlayer.create(mContext, Uri.parse(audioFile));
        int duration = mediaPlayer.getDuration();
        mediaPlayer.release();
        return duration;
    }

    private String wrapAudioMessageContent(String audioFilePath, int audioTime) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("file", audioFilePath);
            jsonObject.put("duration", audioTime);
        } catch (JSONException e) {
        }

        return jsonObject.toString();
    }

    public void getUpYunUploadInfo(File file, int fromId, final NormalCallback<UpYunUploadResult> callback) {
        String path = String.format(Const.GET_UPLOAD_INFO, fromId, file.length(), file.getName());
        RequestUrl url = app.bindPushUrl(path);
        mActivity.ajaxGet(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                UpYunUploadResult result = mActivity.parseJsonValue(response, new TypeToken<UpYunUploadResult>() {
                });
                callback.success(result);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.success(null);
                CommonUtil.longToast(mActivity, getString(R.string.request_fail_text));
                Log.d(TAG, "get upload info from upyun failed");
            }
        });
    }

    private void uploadUnYunMedia(String uploadUrl, final File file, HashMap<String, String> headers, final MessageBody messageBody) {
        RequestUrl putUrl = new RequestUrl(uploadUrl);
        putUrl.setHeads(headers);
        putUrl.setMuiltParams(new Object[]{"file", file});
        mActivity.ajaxPostMultiUrl(putUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "success");
                sendMessageToServer(messageBody);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CommonUtil.longToast(mContext, getString(R.string.request_fail_text));
                Log.d(TAG, "upload media res to upyun failed");
            }
        }, Request.Method.PUT);
    }

    public void saveUploadResult(String putUrl, String getUrl, int fromId) {
        String path = String.format(Const.SAVE_UPLOAD_INFO, fromId);
        RequestUrl url = app.bindPushUrl(path);
        Map<String, String> hashMap = url.getParams();
        hashMap.put("putUrl", putUrl);
        hashMap.put("getUrl", getUrl);
        mActivity.ajaxPost(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject result = new JSONObject(response);
                    if ("success".equals(result.getString("result"))) {
                        Log.d(TAG, "save upload result success");
                    }
                } catch (JSONException e) {
                    Log.d(TAG, "convert json to obj error");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "save upload info error");
            }
        });
    }

    @Override
    public void uploadMediaAgain(File file, Chat chat, String type, String strType) {
        MessageBody messageBody = new MessageBody(1, chat.type, chat.content);
        messageBody.setConvNo(mConversationNo);
        messageBody.setMessageId(chat.mid);
        messageBody.setCreatedTime(chat.createdTime);
        messageBody.setDestination(new Destination(chat.fromId, Destination.USER));
        messageBody.setSource(new Source(chat.toId, Destination.USER));

        updateSendMsgToListView(PushUtil.MsgDeliveryType.UPLOADING, chat);
        getUpYunUploadInfo(file, mCourseId, new UpYunUploadCallback(messageBody));
    }

    public void addSendMsgToListView(int delivery, Chat chat) {
        chat.direct = Chat.Direct.SEND;
        chat.delivery = delivery;
        User currentUser = getAppSettingProvider().getCurrentUser();
        chat.headImgUrl = currentUser.mediumAvatar;
        mAdapter.addItem(chat);
        mStart = mStart + 1;
    }

    public void updateSendMsgToListView(int delivery, Chat chat) {
        chat.delivery = delivery;
        mAdapter.updateItemByMsgId(chat);
    }

    /**
     * 从图库获取图片
     */
    protected void openPictureFromLocal() {
        Intent intent = new Intent(mContext, MultiImageSelectorActivity.class);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, true);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, 5);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_MULTI);
        startActivityForResult(intent, SEND_IMAGE);
    }

    protected void initCacheFolder() {
        File imageFolder = new File(EdusohoApp.getChatCacheFile() + Const.UPLOAD_IMAGE_CACHE_FILE);
        if (!imageFolder.exists()) {
            imageFolder.mkdirs();
        }
        File imageThumbFolder = new File(EdusohoApp.getChatCacheFile() + Const.UPLOAD_IMAGE_CACHE_THUMB_FILE);
        if (!imageThumbFolder.exists()) {
            imageThumbFolder.mkdirs();
        }
        File audioFolder = new File(EdusohoApp.getChatCacheFile() + Const.UPLOAD_AUDIO_CACHE_FILE);
        if (!audioFolder.exists()) {
            audioFolder.mkdirs();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.lv_messages) {
            if (viewMediaLayout.getVisibility() == View.VISIBLE) {
                viewMediaLayout.setVisibility(View.GONE);
            } else {
                AppUtil.setSoftKeyBoard(etSend, mActivity, Const.HIDE_KEYBOARD);
            }
        } else if (v.getId() == R.id.rl_btn_press_to_speak) {
            lvMessage.post(mListViewSelectRunnable);
            boolean mHandUpAndCancel = false;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    try {
                        if (!CommonUtil.isExitsSdcard()) {
                            CommonUtil.longToast(mContext, "发送语音需要sdcard");
                            return false;
                        }
                        mPressDownY = event.getY();
                        mMediaRecorderTask = new MediaRecorderTask();
                        mMediaRecorderTask.execute();
                    } catch (Exception e) {
                        mMediaRecorderTask.getAudioRecord().clear();
                        Log.d(TAG, e.getMessage());
                        return false;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mMediaRecorderTask.getStopRecord()) {
                        return true;
                    }
                    float mPressMoveY = event.getY();
                    if (Math.abs(mPressDownY - mPressMoveY) > EdusohoApp.screenH * 0.1) {
                        tvSpeak.setText(getString(R.string.hand_up_and_exit));
                        tvSpeakHint.setText(getString(R.string.hand_up_and_exit));
                        tvSpeakHint.setBackgroundResource(R.drawable.speak_hint_bg);
                        ivRecordImage.setImageResource(R.drawable.record_cancel);
                        mHandUpAndCancel = true;
                    } else {
                        if (!mMediaRecorderTask.isCountDown()) {
                            ivRecordImage.setImageResource(R.drawable.record_animate_1);
                        }
                        tvSpeakHint.setText(getString(R.string.hand_move_up_and_send_cancel));
                        tvSpeakHint.setBackgroundResource(R.drawable.speak_hint_transparent_bg);
                        tvSpeak.setText(getString(R.string.hand_up_and_end));
                        mHandUpAndCancel = false;
                    }
                    mMediaRecorderTask.setCancel(mHandUpAndCancel);
                    return true;
                case MotionEvent.ACTION_UP:
                    mMediaRecorderTask.setAudioStop(true);
                    return true;
            }
        }
        return false;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            viewMediaLayout.setVisibility(View.GONE);
            AppUtil.setSoftKeyBoard(etSend, mActivity, Const.SHOW_KEYBOARD);
            lvMessage.post(mListViewSelectRunnable);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.et_send_content) {
            lvMessage.post(mListViewSelectRunnable);
        } else if (v.getId() == R.id.iv_show_media_layout) {
            //加号，显示多媒体框
            if (viewMediaLayout.getVisibility() == View.GONE) {
                viewMediaLayout.setVisibility(View.VISIBLE);
                etSend.clearFocus();
                AppUtil.setSoftKeyBoard(etSend, mActivity, Const.HIDE_KEYBOARD);
            } else {
                viewMediaLayout.setVisibility(View.GONE);
            }
            lvMessage.post(mListViewSelectRunnable);
        } else if (v.getId() == R.id.btn_send) {
            //发送消息
            if (etSend.getText().length() == 0) {
                return;
            }
            sendMsg(etSend.getText().toString());
        } else if (v.getId() == R.id.btn_voice) {
            //语音
            viewMediaLayout.setVisibility(View.GONE);
            btnKeyBoard.setVisibility(View.VISIBLE);
            btnVoice.setVisibility(View.GONE);
            viewMsgInput.setVisibility(View.GONE);
            viewPressToSpeak.setVisibility(View.VISIBLE);
            AppUtil.setSoftKeyBoard(etSend, mActivity, Const.HIDE_KEYBOARD);
        } else if (v.getId() == R.id.btn_set_mode_keyboard) {
            //键盘
            viewMediaLayout.setVisibility(View.GONE);
            btnVoice.setVisibility(View.VISIBLE);
            viewPressToSpeak.setVisibility(View.GONE);
            viewMsgInput.setVisibility(View.VISIBLE);
            btnKeyBoard.setVisibility(View.GONE);
            etSend.requestFocus();
            lvMessage.post(mListViewSelectRunnable);
        } else if (v.getId() == R.id.iv_image) {
            //选择图片
            openPictureFromLocal();
        } else if (v.getId() == R.id.iv_camera) {
            openPictureFromCamera();
        }
    }

    protected void openPictureFromCamera() {
        Intent intent = new Intent(mContext, MultiImageSelectorActivity.class);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, true);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_TAKE_CAMERA, true);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_SINGLE);
        startActivityForResult(intent, SEND_IMAGE);
    }

    public class MediaRecorderTask extends AsyncTask<Void, Integer, Boolean> {

        private int COUNT_DOWN_NUM = 50;
        private int TOTAL_NUM = 59;

        private ChatAudioRecord mAudioRecord;
        private boolean mCancelSave = false;
        private boolean mStopRecord = false;
        private boolean mIsCountDown = false;
        private File mUploadAudio;

        @Override
        protected void onPreExecute() {
            if (mAudioRecord == null) mAudioRecord = new ChatAudioRecord(mContext);
            mViewSpeakContainer.setVisibility(View.VISIBLE);
            tvSpeak.setText(getString(R.string.hand_up_and_end));
            tvSpeakHint.setText(getResources().getString(R.string.hand_move_up_and_send_cancel));
            tvSpeakHint.setBackgroundResource(R.drawable.speak_hint_transparent_bg);
            ivRecordImage.setImageResource(R.drawable.record_animate_1);
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            mAudioRecord.ready();
            mAudioRecord.start();
            while (true) {
                if (mStopRecord) {
                    //结束录音
                    mUploadAudio = mAudioRecord.stop(mCancelSave);
                    int audioLength = mAudioRecord.getAudioLength();
                    if (audioLength >= 1) {
                        Log.d(TAG, "上传成功");
                    } else {
                        return false;
                    }
                    mAudioRecord.clear();
                    break;
                } else {
                    long recordTime = (System.currentTimeMillis() - mAudioRecord.getAudioStartTime()) / 1000;
                    if (recordTime > TOTAL_NUM) {
                        mStopRecord = true;
                        mCancelSave = false;
                        continue;
                    }
                    if (!mCancelSave) {
                        //录音中动画
                        double ratio = 0;
                        if (mAudioRecord.getMediaRecorder() != null) {
                            ratio = (double) mAudioRecord.getMediaRecorder().getRealVolume();
                        }

                        double db = 0;
                        if (ratio > 1) {
                            db = 20 * Math.log10(ratio);
                        }
                        if (recordTime > COUNT_DOWN_NUM) {
                            mIsCountDown = true;
                            mHandler.obtainMessage(VolumeHandler.COUNT_DOWN, (int)(TOTAL_NUM - recordTime), 0).sendToTarget();
                        } else if (db < 60) {
                            mHandler.sendEmptyMessage(0);
                        } else if (db < 70) {
                            mHandler.sendEmptyMessage(1);
                        } else if (db < 80) {
                            mHandler.sendEmptyMessage(2);
                        } else if (db < 90) {
                            mHandler.sendEmptyMessage(3);
                        }
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean isSave) {
            if (mCancelSave) {
                mViewSpeakContainer.setVisibility(View.GONE);
                Log.d(TAG, "手指松开取消保存");
            } else {
                if (isSave) {
                    Log.d(TAG, "正常保存上传");
                    uploadMedia(mUploadAudio, PushUtil.ChatMsgType.AUDIO, Const.MEDIA_AUDIO);
                    mViewSpeakContainer.setVisibility(View.GONE);
                } else {
                    Log.d(TAG, "录制时间太短");
                    tvSpeakHint.setText(getString(R.string.audio_length_too_short));
                    tvSpeakHint.setBackgroundResource(R.drawable.speak_hint_transparent_bg);
                    ivRecordImage.setImageResource(R.drawable.record_duration_short);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mViewSpeakContainer.setVisibility(View.GONE);
                        }
                    }, 200);
                    mAudioRecord.delete();
                }
            }
            tvSpeak.setText(getString(R.string.hand_press_and_speak));
            viewPressToSpeak.setPressed(false);
            super.onPostExecute(isSave);
        }

        public void setCancel(boolean cancel) {
            mCancelSave = cancel;
        }

        public void setAudioStop(boolean stop) {
            mStopRecord = stop;
        }

        public ChatAudioRecord getAudioRecord() {
            return mAudioRecord;
        }

        public boolean getStopRecord() {
            return mStopRecord;
        }

        public boolean isCountDown() {
            return mIsCountDown;
        }
    }

    public static class VolumeHandler extends Handler {

        public static final int COUNT_DOWN = 4;
        private WeakReference<IMDiscussFragment> mWeakReference;

        private VolumeHandler(IMDiscussFragment fragment) {
            if (this.mWeakReference == null) {
                this.mWeakReference = new WeakReference<>(fragment);
            }
        }

        @Override
        public void handleMessage(Message msg) {
            IMDiscussFragment fragment = this.mWeakReference.get();
            if (fragment != null) {
                if (msg.what == COUNT_DOWN) {
                    int w = fragment.ivRecordImage.getWidth();
                    int h = fragment.ivRecordImage.getWidth();
                    fragment.ivRecordImage.setImageBitmap(getCountDownBitmap(w, h, msg.arg1));
                    return;
                }
                fragment.ivRecordImage.setImageResource(fragment.mSpeakerAnimResId[msg.what]);
            }
        }

        private Bitmap getCountDownBitmap(int w, int h, int number) {
            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            paint.setTextSize(w * 0.9f);
            paint.setAntiAlias(true);
            paint.setColor(Color.WHITE);

            Rect rect = new Rect();
            paint.getTextBounds(String.valueOf(number), 0, 1, rect);
            canvas.drawText(String.valueOf(number), (w - (rect.right - rect.left)) / 2, (h - rect.bottom - rect.top) / 2, paint);
            return bitmap;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if(requestCode == SEND_IMAGE){
            List<String> pathList = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
            if (pathList == null || pathList.isEmpty()) {
                return;
            }

            for (String path : pathList) {
                uploadMedia(compressImage(path), PushUtil.ChatMsgType.IMAGE, Const.MEDIA_IMAGE);
            }
        }
    }

    /**
     * 选择图片并压缩
     *
     * @param selectedImage 原图
     * @return file
     */
    protected File selectPicture(Uri selectedImage) {
        Cursor cursor = mActivity.getContentResolver().query(selectedImage, null, null, null, null);
        String picturePath = null;
        if (cursor != null) {
            cursor.moveToFirst();
            picturePath = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
            cursor.close();

            if (TextUtils.isEmpty(picturePath)) {
                CommonUtil.shortToast(mContext, "找不到图片");
                return null;
            }
        }
        if (TextUtils.isEmpty(picturePath)) {
            CommonUtil.shortToast(mContext, "图片不存在");
            return null;
        }
        return compressImage(picturePath);
    }

    private File compressImage(String filePath) {
        File compressedFile;
        try {
            Bitmap tmpBitmap = AppUtil.CompressImage(filePath);
            Bitmap resultBitmap = AppUtil.scaleImage(tmpBitmap, tmpBitmap.getWidth(), AppUtil.getImageDegree(filePath));
            Bitmap thumbBitmap = AppUtil.scaleImage(tmpBitmap, EdusohoApp.screenW * 0.4f, AppUtil.getImageDegree(filePath));
            compressedFile = AppUtil.convertBitmap2File(resultBitmap,
                    EdusohoApp.getChatCacheFile() + Const.UPLOAD_IMAGE_CACHE_FILE + "/" + System.currentTimeMillis());
            AppUtil.convertBitmap2File(thumbBitmap, EdusohoApp.getChatCacheFile() +
                    Const.UPLOAD_IMAGE_CACHE_THUMB_FILE + "/" + compressedFile.getName());
            if (!tmpBitmap.isRecycled()) {
                tmpBitmap.recycle();
            }
            if (!thumbBitmap.isRecycled()) {
                thumbBitmap.recycle();
            }
            if (!resultBitmap.isRecycled()) {
                resultBitmap.recycle();
            }
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage());
            return null;
        }
        return compressedFile;
    }

    protected Runnable mListViewSelectRunnable = new Runnable() {
        @Override
        public void run() {
            if (lvMessage != null && lvMessage.getAdapter() != null) {
                lvMessage.setSelection(lvMessage.getCount());
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContext.unregisterReceiver(mAudioDownloadReceiver);
    }

    @Override
    public MessageType[] getMsgTypes() {
        String source = this.getClass().getSimpleName();
        return new MessageType[]{new MessageType(Const.ADD_COURSE_DISCUSS_MSG, source), new MessageType(Const.CLEAN_RECORD, source)};
    }

    @Override
    public void invoke(WidgetMessage message) {

    }

    protected UtilFactory getUtilFactory() {
        return FactoryManager.getInstance().create(UtilFactory.class);
    }

    protected AppSettingProvider getAppSettingProvider() {
        return FactoryManager.getInstance().create(AppSettingProvider.class);
    }

    private class UpYunUploadCallback implements NormalCallback<UpYunUploadResult>
    {
        private MessageBody messageBody;

        public UpYunUploadCallback(MessageBody messageBody)
        {
            this.messageBody = messageBody;
        }

        @Override
        public void success(UpYunUploadResult result) {
            final Chat chat = new Chat(messageBody);
            if (result != null) {
                IMUploadEntity uploadEntity = IMClient.getClient().getMessageManager().getUploadEntity(messageBody.getMessageId());
                File file = new File(uploadEntity.getSource());
                String body = result.getUrl;
                if (PushUtil.ChatMsgType.AUDIO.equals(messageBody.getType())) {
                    body = wrapAudioMessageContent(result.getUrl, getAudioDuration(file.getAbsolutePath()));
                }
                messageBody.setBody(body);

                uploadUnYunMedia(result.putUrl, file, result.getHeaders(), messageBody);
                saveUploadResult(result.putUrl, result.getUrl, mCourseId);
            } else {
                updateSendMsgToListView(PushUtil.MsgDeliveryType.FAILED, chat);
            }
        }
    }

    protected NotificationProvider getNotificationProvider() {
        return FactoryManager.getInstance().create(NotificationProvider.class);
    }
}
