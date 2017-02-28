package com.edusoho.kuozhi.v3.ui;


import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.imserver.IMClient;
import com.edusoho.kuozhi.imserver.entity.ConvEntity;
import com.edusoho.kuozhi.imserver.entity.IMUploadEntity;
import com.edusoho.kuozhi.imserver.entity.MessageEntity;
import com.edusoho.kuozhi.imserver.entity.Role;
import com.edusoho.kuozhi.imserver.entity.message.Destination;
import com.edusoho.kuozhi.imserver.entity.message.MessageBody;
import com.edusoho.kuozhi.imserver.entity.message.Source;
import com.edusoho.kuozhi.imserver.managar.IMConvManager;
import com.edusoho.kuozhi.imserver.managar.IMRoleManager;
import com.edusoho.kuozhi.imserver.ui.IMessageListPresenter;
import com.edusoho.kuozhi.imserver.ui.IMessageListView;
import com.edusoho.kuozhi.imserver.ui.MessageListFragment;
import com.edusoho.kuozhi.imserver.ui.MessageListPresenterImpl;
import com.edusoho.kuozhi.imserver.ui.adapter.MessageRecyclerListAdapter;
import com.edusoho.kuozhi.imserver.ui.data.IMessageDataProvider;
import com.edusoho.kuozhi.imserver.ui.entity.AudioBody;
import com.edusoho.kuozhi.imserver.ui.entity.PushUtil;
import com.edusoho.kuozhi.imserver.ui.helper.MessageResourceHelper;
import com.edusoho.kuozhi.imserver.ui.util.AudioUtil;
import com.edusoho.kuozhi.imserver.util.MessageEntityBuildr;
import com.edusoho.kuozhi.v3.core.MessageEngine;
import com.edusoho.kuozhi.v3.entity.lesson.QuestionAnswerAdapter;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.model.bal.User;
import com.edusoho.kuozhi.v3.model.bal.push.CourseThreadPostResult;
import com.edusoho.kuozhi.v3.model.bal.thread.CourseThreadPostEntity;
import com.edusoho.kuozhi.v3.model.bal.thread.PostThreadResult;
import com.edusoho.kuozhi.v3.model.provider.ThreadProvider;
import com.edusoho.kuozhi.v3.model.provider.UserProvider;
import com.edusoho.kuozhi.v3.model.sys.School;
import com.edusoho.kuozhi.v3.ui.chat.AbstractIMChatActivity;
import com.edusoho.kuozhi.v3.ui.fragment.DiscussDetailMessageListFragment;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Promise;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.trinea.android.common.util.ToastUtils;

/**
 * Created by DF on 2017/1/8.
 */

public class DiscussDetailActivity extends AbstractIMChatActivity implements IMessageDataProvider {

    private static final String CONV_NO_FORMAT = "%s-%s";
    public static final String LESSON_ID = "lesson_id";
    public static final String THREAD_TYPE = "threadType";
    public static final String THREAD_TARGET_TYPE = "thread_target_type";
    public static final String THREAD_TARGET_ID = "thread_target_id";
    public static final String IMAGE_FORMAT = "<img alt=\"\" src=\"%s\" />";

    protected ThreadProvider mThreadProvider;
    /**
     * ask,answer
     */

    //讨论组问答类型:question, discuss
    private String mThreadType;

    //讨论组目标id:课程或者班级id
    private int mThreadTargetId;
    private int mLessonId;
    private int mHeaderViewHeight;
    public View mHeaderView;
    private View mContentLayout;
    private int mStart;

    //讨论组目标类型:course 或classroom
    private String mThreadTargetType;
    private LinkedHashMap mThreadInfo;
    private List<MessageEntity> mMessageEntityList;
    private TextView mTvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMessageEntityList = new ArrayList<>();
        mThreadProvider = new ThreadProvider(mContext);
    }

    @Override
    protected View createView() {
        mContentLayout = LayoutInflater.from(mContext).inflate(R.layout.activity_discuss_detail, null);
        mContentLayout.findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DiscussDetailActivity.this.finish();
            }
        });
        return mContentLayout;
    }

    @Override
    public void onAttachedToWindow() {
        if (mThreadInfo == null) {
            getThreadInfo(new NormalCallback<LinkedHashMap>() {
                @Override
                public void success(LinkedHashMap linkedHashMap) {
                    if (linkedHashMap == null
                            || !linkedHashMap.containsKey("id")
                            || !linkedHashMap.containsKey("type")) {
                        CommonUtil.shortToast(mContext, "获取讨论组信息失败");
                        return;
                    }
                    mThreadInfo = linkedHashMap;

                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    Fragment fragment = getSupportFragmentManager().findFragmentByTag("im_container");
                    if (fragment != null) {
                        mMessageListFragment = (MessageListFragment) fragment;
                    } else {
                        mMessageListFragment = createFragment();
                        fragmentTransaction.add(R.id.chat_content, mMessageListFragment, "im_container");
                        fragmentTransaction.commitAllowingStateLoss();
                    }
                    mIMessageListPresenter = createProsenter();
                    mIMessageListPresenter.addMessageControllerListener(getMessageControllerListener());
                    mContentLayout.addOnLayoutChangeListener(getOnLayoutChangeListener());
                    setBackMode(BACK, mThreadInfo.get("title").toString());
                    initThreadPostList();
                }
            });
            return;
        }
        initThreadPostList();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachMessageListFragment() {
        Log.d(TAG, "attachMessageListFragment");

    }

    @Override
    protected IMessageListPresenter createProsenter() {
        Bundle bundle = new Bundle();
        bundle.putString(MessageListFragment.CONV_NO, mConversationNo);
        bundle.putInt(MessageListFragment.TARGET_ID, mTargetId);
        bundle.putString(MessageListFragment.TARGET_TYPE, getTargetType());

        return new ThreadChatMessageListPresenterImpl(
                bundle,
                new MockConvManager(mContext),
                IMClient.getClient().getRoleManager(),
                IMClient.getClient().getResourceHelper(),
                this,
                mMessageListFragment);
    }

    protected View.OnLayoutChangeListener getOnLayoutChangeListener() {
        return new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            }
        };
    }

    @Override
    protected String getTargetType() {
        return Destination.GROUP;
    }

    protected void initParams() {
        super.initParams();
        Intent dataIntent = getIntent();

        mTargetType = dataIntent.getStringExtra(THREAD_TYPE);
        mThreadTargetType = dataIntent.getStringExtra(THREAD_TARGET_TYPE);
        mThreadTargetId = dataIntent.getIntExtra(THREAD_TARGET_ID, 0);
        mLessonId = dataIntent.getIntExtra(LESSON_ID, 0);
    }

    @Override
    protected void createTargetRole(String type, int rid, final MessageListPresenterImpl.RoleUpdateCallback callback) {
        if (Destination.USER.equals(type)) {
            new UserProvider(mContext).getUserInfo(rid)
                    .success(new NormalCallback<User>() {
                        @Override
                        public void success(User user) {
                            Role role = new Role();
                            role.setRid(user.id);
                            role.setAvatar(user.mediumAvatar);
                            role.setType(Destination.USER);
                            role.setNickname(user.nickname);
                            callback.onCreateRole(role);
                        }
                    });
            return;
        }
        if (mThreadInfo != null) {
            Role role = new Role();
            role.setRid(AppUtil.parseInt(mThreadInfo.get("id").toString()));
            role.setType(getTargetType());
            role.setNickname(mThreadInfo.get("title").toString());
            callback.onCreateRole(role);
            return;
        }
        getThreadInfo(new NormalCallback<LinkedHashMap>() {
            @Override
            public void success(LinkedHashMap linkedHashMap) {
                if (linkedHashMap == null) {
                    return;
                }
                mThreadInfo = linkedHashMap;
                Role role = new Role();
                role.setRid(AppUtil.parseInt(linkedHashMap.get("id").toString()));
                role.setType(getTargetType());
                role.setNickname(linkedHashMap.get("title").toString());
                callback.onCreateRole(role);
            }
        });
    }

    @Override
    protected Promise createChatConvNo() {
        final Promise promise = new Promise();
        User currentUser = getAppSettingProvider().getCurrentUser();
        if (currentUser == null || currentUser.id == 0) {
            ToastUtils.show(getBaseContext(), "用户未登录");
            promise.resolve(null);
            return promise;
        }

        getThreadInfo(new NormalCallback<LinkedHashMap>() {
            @Override
            public void success(LinkedHashMap linkedHashMap) {
                if (linkedHashMap == null
                        || !linkedHashMap.containsKey("id")
                        || !linkedHashMap.containsKey("type")) {
                    CommonUtil.shortToast(mContext, "获取讨论组信息失败");
                    return;
                }

                mThreadInfo = linkedHashMap;
                promise.resolve(String.format(CONV_NO_FORMAT, linkedHashMap.get("id"), linkedHashMap.get("type")));
            }
        });

        return promise;
    }

    private void getThreadInfo(NormalCallback<LinkedHashMap> normalCallback) {
        if ("course".equals(mThreadTargetType)) {
            mThreadProvider.getCourseThreadInfo(mTargetId, mThreadTargetId)
                    .success(normalCallback)
                    .fail(new NormalCallback<VolleyError>() {
                        @Override
                        public void success(VolleyError obj) {
                            CommonUtil.longToast(mContext, "获取问答内容失败");
                        }
                    });
            return;
        }

        if ("classroom".equals(mThreadTargetType)) {
            mThreadProvider.getClassRoomThreadInfo(mTargetId)
                    .success(normalCallback)
                    .fail(new NormalCallback<VolleyError>() {
                        @Override
                        public void success(VolleyError obj) {
                            CommonUtil.longToast(mContext, "获取问答内容失败");
                        }
                    });
            return;
        }
        normalCallback.success(null);
    }

    private List<MessageEntity> coverPostListToMessageEntity(List<CourseThreadPostEntity> postList) {
        List<MessageEntity> messageEntityList = new ArrayList<>();
        for (int i = 0; i < postList.size(); i++) {
            MessageEntity messageEntity = createMessageEntityBy(i, postList.get(i));
            messageEntityList.add(messageEntity);
        }

        return messageEntityList;
    }

    @Override
    public MessageEntity createMessageEntity(MessageBody messageBody) {
        MessageEntity messageEntity = new MessageEntityBuildr()
                .addUID(messageBody.getMessageId())
                .addConvNo(messageBody.getConvNo())
                .addToId(String.valueOf(messageBody.getDestination().getId()))
                .addToName(messageBody.getDestination().getNickname())
                .addFromId(String.valueOf(messageBody.getSource().getId()))
                .addFromName(messageBody.getSource().getNickname())
                .addCmd("message")
                .addStatus(MessageEntity.StatusType.FAILED)
                .addMsg(messageBody.toJson())
                .addTime((int) (messageBody.getCreatedTime() / 1000))
                .builder();

        messageEntity.setId(mMessageEntityList.size());
        messageBody.setMid(mMessageEntityList.size());
        mMessageEntityList.add(messageEntity);
        return messageEntity;
    }

    @Override
    public void sendMessage(String convNo, MessageBody messageBody) {
        sendMessageToServer(messageBody.getMid(), messageBody.getBody());
    }

    @Override
    public List<MessageEntity> getMessageList(String convNo, int start) {
        if (mStart > 0) {
            return new ArrayList<>();
        }
        mStart += mMessageEntityList.size();
        return mMessageEntityList;
    }

    private void sendMessageToServer(final int position, String content) {
        final String threadType = "course".equals(mThreadTargetType) ? "course" : "common";
        mThreadProvider.createThreadPost(mThreadTargetType, mThreadTargetId, threadType, mTargetId, content)
                .success(new NormalCallback<PostThreadResult>() {
                    @Override
                    public void success(PostThreadResult threadResult) {
                        if (threadResult != null) {
                            MessageEntity messageEntity = mMessageEntityList.get(position);
                            if (messageEntity != null) {
                                messageEntity.setTime((int) AppUtil.convertUTCTimeToMilliSecond(threadResult.createdTime));
                                messageEntity.setStatus(MessageEntity.StatusType.SUCCESS);
                                mMessageListFragment.updateListByEntity(messageEntity);
                            }
                        }
                        MessageEngine.getInstance().sendMsg(WebViewActivity.SEND_EVENT, null);
                    }
                }).fail(new NormalCallback<VolleyError>() {
                    @Override
                    public void success(VolleyError error) {
                    }
        });
    }

    private String parseBodyType(String body) {
        if (body.contains("<img")) {
            return PushUtil.ChatMsgType.IMAGE;
        }

        AudioBody audioBody = AudioUtil.getAudioBody(body);
        if (audioBody != null && !TextUtils.isEmpty(audioBody.getFile())) {
            return PushUtil.ChatMsgType.AUDIO;
        }

        return PushUtil.ChatMsgType.TEXT;
    }

    private String getBodyContent(String body) {
        if (body.contains("<img")) {
            Pattern p = Pattern.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>");
            Matcher m = p.matcher(body);
            while (m.find()) {
                String path = m.group(1);
                if (path != null && !path.startsWith("http://")) {
                    School school = getAppSettingProvider().getCurrentSchool();
                    return school.host + path;
                }
                return path;
            }
        }

        return AppUtil.coverCourseAbout(body);
    }

    private MessageEntity createMessageEntityBy(int position, CourseThreadPostEntity postEntity) {
        MessageEntity messageEntity = new MessageEntity();
        messageEntity.setId(position);
        CourseThreadPostEntity.UserEntity user = postEntity.user;
        messageEntity.setFromId(String.valueOf(user.id));
        messageEntity.setFromName(user.nickname);
        messageEntity.setToId("all");
        messageEntity.setToName(mThreadInfo.get("title").toString());

        String messageType = parseBodyType(postEntity.content);
        MessageBody messageBody = new MessageBody(MessageBody.VERSION, messageType, getBodyContent(postEntity.content));
        messageBody.setDestination(new Destination(
                AppUtil.parseInt(mThreadInfo.get("id").toString()), getTargetType()));
        messageBody.setSource(new Source(user.id, Destination.USER));
        messageEntity.setMsg(messageBody.toJson());
        messageEntity.setTime((int) AppUtil.convertUTCTimeToMilliSecond(postEntity.createdTime));

        messageEntity.setStatus(MessageEntity.StatusType.SUCCESS);
        return messageEntity;
    }

    private void initThreadPostList() {
        mThreadProvider.getThreadPost(mThreadTargetType, mTargetId).success(new NormalCallback<CourseThreadPostResult>() {
            @Override
            public void success(CourseThreadPostResult threadPostResult) {
                if (threadPostResult != null && threadPostResult.resources != null) {
                    List<CourseThreadPostEntity> posts = threadPostResult.resources;
                    List<MessageEntity> messageEntityList = coverPostListToMessageEntity(posts);
                    Collections.sort(messageEntityList, new Comparator<MessageEntity>() {
                        @Override
                        public int compare(MessageEntity t1, MessageEntity t2) {
                            return t2.getTime() - t1.getTime();
                        }
                    });
                    mMessageEntityList.addAll(messageEntityList);
                    mIMessageListPresenter.refresh();
                }
            }
        }).fail(new NormalCallback<VolleyError>() {
            @Override
            public void success(VolleyError obj) {
                CommonUtil.shortToast(mContext, "获取讨论组回复列表失败");
                finish();
            }
        });
    }


    @Override
    public void sendMessage(MessageEntity messageEntity) {
    }

    @Override
    public MessageEntity insertMessageEntity(MessageEntity messageEntity) {
        return null;
    }

    @Override
    public int deleteMessageById(int msgId) {
        return 0;
    }

    @Override
    public IMUploadEntity getUploadEntity(String muid) {
        return null;
    }

    @Override
    public long saveUploadEntity(String muid, String type, String source) {
        return 0;
    }

    @Override
    public MessageEntity getMessageByUID(String uid) {
        return null;
    }

    @Override
    public MessageEntity getMessage(int msgId) {
        return mMessageEntityList.get(msgId);
    }

    @Override
    public int updateMessageFieldByMsgNo(String msgNo, ContentValues cv) {
        return 0;
    }

    @Override
    public int updateMessageFieldByUid(String uid, ContentValues cv) {
        return 0;
    }

    class MockConvManager extends IMConvManager {

        public MockConvManager(Context context) {
            super(context);
        }

        @Override
        public long createConv(ConvEntity convEntity) {
            return 0;
        }

        @Override
        public ConvEntity getConvByConvNo(String convNo) {
            return null;
        }

        @Override
        public ConvEntity getConvByTypeAndId(String type, int targetId) {
            return null;
        }

        @Override
        public int updateConvByConvNo(ConvEntity convEntity) {
            return 0;
        }

        @Override
        public int clearReadCount(String convNo) {
            return 0;
        }
    }

    class ThreadChatMessageListPresenterImpl extends ChatMessageListPresenterImpl {

        public ThreadChatMessageListPresenterImpl(Bundle params,
                                                  IMConvManager convManager,
                                                  IMRoleManager roleManager,
                                                  MessageResourceHelper messageResourceHelper,
                                                  IMessageDataProvider mIMessageDataProvider,
                                                  IMessageListView messageListView) {
            super(params, convManager, roleManager, messageResourceHelper, mIMessageDataProvider, messageListView);
        }

        @Override
        public boolean canRefresh() {
            return false;
        }

        @Override
        public void processResourceDownload(int resId, String resUri) {
            super.processResourceDownload(resId, resUri);
            MessageEntity messageEntity = mMessageEntityList.get(resId);
            if (messageEntity != null) {
                messageEntity.setStatus(MessageEntity.StatusType.SUCCESS);
                mMessageListFragment.updateListByEntity(messageEntity);
            }
        }
    }

    @Override
    protected MessageListFragment createFragment() {
        DiscussDetailMessageListFragment discussDetailMessageListFragment = (DiscussDetailMessageListFragment) Fragment.instantiate(mContext, DiscussDetailMessageListFragment.class.getName());
        Bundle bundle = new Bundle();
        bundle.putSerializable("info", mThreadInfo);
        bundle.putString("kind", mThreadTargetType);
        discussDetailMessageListFragment.setArguments(bundle);
        MessageRecyclerListAdapter messageRecyclerListAdapter = new QuestionAnswerAdapter(mContext);
        discussDetailMessageListFragment.setAdapter(messageRecyclerListAdapter);
        return discussDetailMessageListFragment;
    }

    @Override
    public void setBackMode(String backTitle, String title) {
    }

}
