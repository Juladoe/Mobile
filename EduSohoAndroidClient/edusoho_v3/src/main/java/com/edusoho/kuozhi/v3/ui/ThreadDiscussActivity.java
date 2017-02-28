package com.edusoho.kuozhi.v3.ui;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Intent;
import android.support.annotation.LayoutRes;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.adapter.ChatAdapter;
import com.edusoho.kuozhi.v3.adapter.ThreadDiscussAdapter;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.listener.PluginRunCallback;
import com.edusoho.kuozhi.v3.listener.PromiseCallback;
import com.edusoho.kuozhi.v3.model.bal.UserRole;
import com.edusoho.kuozhi.v3.model.bal.push.Chat;
import com.edusoho.kuozhi.v3.model.bal.push.CourseThreadPostResult;
import com.edusoho.kuozhi.v3.model.bal.push.UpYunUploadResult;
import com.edusoho.kuozhi.v3.model.bal.push.V2CustomContent;
import com.edusoho.kuozhi.v3.model.bal.thread.CourseThreadEntity;
import com.edusoho.kuozhi.v3.model.bal.thread.CourseThreadPostEntity;
import com.edusoho.kuozhi.v3.model.bal.thread.PostThreadResult;
import com.edusoho.kuozhi.v3.model.provider.ThreadProvider;
import com.edusoho.kuozhi.v3.model.sys.AudioCacheEntity;
import com.edusoho.kuozhi.v3.model.sys.MessageType;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.model.sys.WidgetMessage;
import com.edusoho.kuozhi.v3.ui.base.BaseChatActivity;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.AudioCacheUtil;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.NotificationUtil;
import com.edusoho.kuozhi.v3.util.Promise;
import com.edusoho.kuozhi.v3.util.PushUtil;
import com.edusoho.kuozhi.v3.util.RequestUtil;
import com.edusoho.kuozhi.v3.util.sql.CourseThreadDataSource;
import com.edusoho.kuozhi.v3.util.sql.CourseThreadPostDataSource;
import com.edusoho.kuozhi.v3.util.sql.SqliteChatUtil;
import com.edusoho.kuozhi.v3.view.EduSohoAnimWrap;
import com.edusoho.kuozhi.v3.view.dialog.LoadDialog;
import com.nostra13.universalimageloader.core.ImageLoader;
import org.json.JSONObject;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

import static com.edusoho.kuozhi.v3.adapter.ThreadDiscussAdapter.ThreadDiscussEntity;

/**
 * Created by JesseHuang on 15/12/23.
 */
public class ThreadDiscussActivity extends BaseChatActivity implements ChatAdapter.ImageErrorClick {
    public static final String TAG = "ThreadDiscussActivity";
    public static final String DEFAULT_TITLE = "请描述你的问题";
    public static final String THREAD_ERROR_HINT = "问题不存在";
    public static final String ACTIVITY_TYPE = "activity_type";
    public static final String THREAD_ID = "thread_id";
    public static final String TARGET_ID = "target_id";
    public static final String TARGET_TYPE = "target_type";
    public static final String LESSON_ID = "lesson_id";
    public static final String IMAGE_FORMAT = "<img alt=\"\" src=\"%s\" />";
    public static final String AUDIO_FORMAT = "%s";
    public static int CurrentThreadId = 0;

    /**
     * ask,answer
     */
    private String mActivityType;
    private String mRoleType;
    private int mToUserId;
    private int mThreadId;
    private int mTargetId;
    private int mLessonId;
    private String mTargetType;

    private CourseThreadEntity mThreadModel;
    private List<CourseThreadPostEntity> mPosts;
    private CourseThreadDataSource mCourseThreadDataSource;
    private CourseThreadPostDataSource mCourseThreadPostDataSource;
    private ThreadDiscussAdapter mAdapter;
    private LoadDialog mLoadDialog;
    private View mHeaderView;
    private LinearLayout mContentLayout;

    protected ThreadProvider mThreadProvider;

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        mHeaderView = LayoutInflater.from(mContext).inflate(R.layout.activity_thread_dicuss_head_layout, null);
        View rootView = LayoutInflater.from(mContext).inflate(layoutResID, null);
        mContentLayout = new LinearLayout(mContext);
        mContentLayout.setOrientation(LinearLayout.VERTICAL);

        mContentLayout.addView(mHeaderView);
        mContentLayout.addView(rootView);

        setContentView(mContentLayout);
    }

    protected void setThreadInfo() {
        if (TextUtils.isEmpty(mTargetType)) {
            return;
        }

        if ("course".equals(mTargetType)) {
            fillThreadInfoByCourse();
            return;
        }

        if ("classroom".equals(mTargetType)) {
            fillThreadInfoByClassRoom();
        }
    }

    protected void fillThreadInfoByClassRoom() {
        mThreadProvider.getClassRoomThreadInfo(mThreadId).success(new NormalCallback<LinkedHashMap>() {
            @Override
            public void success(LinkedHashMap threadInfo) {
                if (threadInfo == null) {
                    return;
                }
                fillThreaLabelData(threadInfo);
                LinkedHashMap<String, String> course = (LinkedHashMap<String, String>) threadInfo.get("target");
                TextView fromCourseView = (TextView) findViewById(R.id.tdh_from_course);
                fromCourseView.setText(String.format("来自班级:《%s》", course.get("title")));
                fromCourseView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String url = String.format(
                                Const.MOBILE_APP_URL,
                                EdusohoApp.app.schoolHost,
                                String.format(Const.MOBILE_WEB_COURSE, mTargetId)
                        );
                        mActivity.app.mEngine.runNormalPlugin("WebViewActivity", mContext, new PluginRunCallback() {
                            @Override
                            public void setIntentDate(Intent startIntent) {
                                startIntent.putExtra(Const.WEB_URL, url);
                            }
                        });
                    }
                });
            }
        });
    }

    private void fillThreaLabelData(LinkedHashMap threadInfo) {
        String type = threadInfo.get("type").toString();
        TextView labelView = (TextView) findViewById(R.id.tdh_label);
        if ("question".equals(type)) {
            labelView.setText("问答");
            labelView.setTextColor(getResources().getColor(R.color.thread_type_question));
            labelView.setBackgroundResource(R.drawable.thread_type_question_label);
        } else {
            labelView.setText("话题");
            labelView.setTextColor(getResources().getColor(R.color.thread_type_discuss));
            labelView.setBackgroundResource(R.drawable.thread_type_discuss_label);
        }
        TextView titleView = (TextView) findViewById(R.id.tdh_title);
        titleView.setText(threadInfo.get("title").toString());
        TextView timeView = (TextView) findViewById(R.id.tdh_time);
        timeView.setText(getFromInfoTime(threadInfo.get("createdTime").toString()));

        TextView contentView = (TextView) findViewById(R.id.tdh_content);
        contentView.setText(AppUtil.coverCourseAbout(threadInfo.get("content").toString()));

        LinkedHashMap<String, String> user = (LinkedHashMap<String, String>) threadInfo.get("user");
        TextView nicknameView = (TextView) findViewById(R.id.tdh_nickname);
        nicknameView.setText(user.get("nickname"));
        ImageView userAvatar = (ImageView) findViewById(R.id.tdh_avatar);
        ImageLoader.getInstance().displayImage(user.get("avatar"), userAvatar);
    }

    protected void fillThreadInfoByCourse() {
        mThreadProvider.getCourseThreadInfo(mThreadId, mTargetId).success(new NormalCallback<LinkedHashMap>() {
            @Override
            public void success(LinkedHashMap threadInfo) {
                if (threadInfo == null) {
                    return;
                }

                fillThreaLabelData(threadInfo);
                LinkedHashMap<String, String> course = (LinkedHashMap<String, String>) threadInfo.get("course");
                TextView fromCourseView = (TextView) findViewById(R.id.tdh_from_course);
                fromCourseView.setText(String.format("来自课程:《%s》", course.get("title")));
                fromCourseView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String url = String.format(
                                Const.MOBILE_APP_URL,
                                EdusohoApp.app.schoolHost,
                                String.format(Const.MOBILE_WEB_COURSE, mTargetId)
                        );
                        mActivity.app.mEngine.runNormalPlugin("WebViewActivity", mContext, new PluginRunCallback() {
                            @Override
                            public void setIntentDate(Intent startIntent) {
                                startIntent.putExtra(Const.WEB_URL, url);
                            }
                        });
                    }
                });
            }
        });
    }

    private String getFromInfoTime(String time) {
        try {
            Date timeDate = null;
            time = time.replace("T", " ");
            timeDate = new SimpleDateFormat("yyyy-MM-dd").parse(time);
            SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy/MM/dd");
            return timeFormat.format(timeDate);
        } catch (Exception e) {
        }

        return "";
    }

    @Override
    public void initView() {
        super.initView();
        mLoadDialog = LoadDialog.create(mContext);
        mPtrFrame.setPtrHandler(new PtrHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                mPtrFrame.refreshComplete();
            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                boolean canDoRefresh = PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
                return canDoRefresh;
            }
        });
        btnVoice.setOnClickListener(mAskClickListener);
        ivAddMedia.setOnClickListener(mAskClickListener);
    }

    @Override
    public void initData() {
        Intent intent = getIntent();
        mActivityType = intent.getStringExtra(ACTIVITY_TYPE);
        mTargetId = intent.getIntExtra(TARGET_ID, 0);
        mTargetType = intent.getStringExtra(TARGET_TYPE);
        mThreadId = intent.getIntExtra(THREAD_ID, 0);
        mLessonId = intent.getIntExtra(LESSON_ID, 0);
        CurrentThreadId = mThreadId;
        if (TextUtils.isEmpty(mRoleType)) {
            String[] roles = new String[app.loginUser.roles.length];
            for (int i = 0; i < app.loginUser.roles.length; i++) {
                roles[i] = app.loginUser.roles[i].toString();
            }
            if (CommonUtil.inArray(UserRole.ROLE_TEACHER.name(), roles)) {
                mRoleType = PushUtil.ChatUserType.TEACHER;
            } else {
                mRoleType = PushUtil.ChatUserType.FRIEND;
            }
        }

        mThreadProvider = new ThreadProvider(mContext);
        setThreadInfo();
        mCourseThreadDataSource = new CourseThreadDataSource(SqliteChatUtil.getSqliteChatUtil(mContext, app.domain));
        mCourseThreadPostDataSource = new CourseThreadPostDataSource(SqliteChatUtil.getSqliteChatUtil(mContext, app.domain));
        if (PushUtil.ThreadMsgType.THREAD_POST.equals(mActivityType)) {
            if (mThreadId == 0) {
                CommonUtil.shortToast(mContext, THREAD_ERROR_HINT);
                finish();
            } else {
                getLists(mThreadId, new NormalCallback<Boolean>() {
                    @Override
                    public void success(Boolean tag) {
                        if (tag) {
                            setBackMode(BACK, mThreadModel.title);
                            mAdapter = new ThreadDiscussAdapter(mPosts, mThreadModel, mContext);
                            mAdapter.setSendImageClickListener(ThreadDiscussActivity.this);
                            lvMessage.setAdapter(mAdapter);
                            mAudioDownloadReceiver.setAdapter(mAdapter);
                            lvMessage.postDelayed(mListViewSelectRunnable, 500);
                        }
                    }
                });
            }
        } else {
            setBackMode(BACK, DEFAULT_TITLE);
            mAdapter = new ThreadDiscussAdapter(mContext);
            lvMessage.setAdapter(mAdapter);
            mAdapter.setSendImageClickListener(this);
            mAudioDownloadReceiver.setAdapter(mAdapter);
        }
        NotificationUtil.cancelById(mThreadId);
    }

    protected Runnable mListViewSelectRunnable = new Runnable() {
        @Override
        public void run() {
            lvMessage.setSelection(mAdapter.getCount());
        }
    };

    @Override
    public void sendMsg(final String content) {
        CourseThreadPostEntity postModel = createCoursePostThreadByCurrentUser(content, PushUtil.ChatMsgType.TEXT, PushUtil.MsgDeliveryType.UPLOADING);
        postModel.pid = (int) mCourseThreadPostDataSource.create(postModel);
        ThreadDiscussEntity discussModel = convertThreadDiscuss(postModel);
        addItem2ListView(discussModel);
        handleSendPost(postModel);
    }

    @Override
    public void sendMsgAgain(final Chat model) {
        final CourseThreadPostEntity postModel = mCourseThreadPostDataSource.getPost(model.id);
        mAdapter.updateItemState(model.id, PushUtil.MsgDeliveryType.UPLOADING);
        handleSendPost(postModel);
    }

    // region 多媒体资源上传

    public void uploadMedia(final File file, final String type, String strType) {
        if (file == null || !file.exists()) {
            CommonUtil.shortToast(mContext, String.format("%s不存在", strType));
            return;
        }
        try {
            final CourseThreadPostEntity postModel = createCoursePostThreadByCurrentUser(file.getPath(), type, PushUtil.MsgDeliveryType.UPLOADING);
            postModel.pid = (int) mCourseThreadPostDataSource.create(postModel);
            ThreadDiscussEntity discussModel = convertThreadDiscuss(postModel);
            addItem2ListView(discussModel);
            getUpYunUploadInfo(file, app.loginUser.id, new NormalCallback<UpYunUploadResult>() {
                @Override
                public void success(final UpYunUploadResult result) {
                    if (result != null) {
                        postModel.upyunMediaPutUrl = result.putUrl;
                        postModel.upyunMediaGetUrl = result.getUrl;
                        postModel.headers = result.getHeaders();
                        AudioCacheUtil.getInstance().create(postModel.content, postModel.upyunMediaGetUrl);
                        uploadUnYunMedia(file, postModel);
                        ThreadDiscussActivity.super.saveUploadResult(result.putUrl, result.getUrl, mThreadId);
                        postModel.delivery = PushUtil.MsgDeliveryType.SUCCESS;
                        mCourseThreadPostDataSource.update(postModel);
                        mAdapter.updateItemState(postModel.pid, PushUtil.MsgDeliveryType.SUCCESS);
                    } else {
                        postModel.delivery = PushUtil.MsgDeliveryType.FAILED;
                        mCourseThreadPostDataSource.update(postModel);
                        AudioCacheUtil.getInstance().create(postModel.content, postModel.upyunMediaGetUrl);
                        mAdapter.updateItemState(postModel.pid, PushUtil.MsgDeliveryType.FAILED);
                    }
                }
            });
            viewMediaLayout.setVisibility(View.GONE);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void uploadUnYunMedia(final File file, final CourseThreadPostEntity model) {
        RequestUrl putUrl = new RequestUrl(model.upyunMediaPutUrl);
        putUrl.setHeads(model.headers);
        putUrl.setMuiltParams(new Object[]{"file", file});
        ajaxPostMultiUrl(putUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "success");
                handleSendPost(model);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CommonUtil.longToast(mActivity, getString(R.string.request_fail_text));
                Log.d(TAG, "upload media res to upyun failed");
            }
        }, Request.Method.PUT);
    }

    @Override
    public void uploadMediaAgain(final File file, final Chat model, final String type, String strType) {
        try {
            final CourseThreadPostEntity postModel = mCourseThreadPostDataSource.getPost(model.id);
            getUpYunUploadInfo(file, app.loginUser.id, new NormalCallback<UpYunUploadResult>() {
                @Override
                public void success(final UpYunUploadResult result) {
                    if (result != null) {
                        postModel.upyunMediaPutUrl = result.putUrl;
                        postModel.upyunMediaGetUrl = result.getUrl;
                        postModel.headers = result.getHeaders();
                        uploadUnYunMedia(file, postModel);
                        ThreadDiscussActivity.super.saveUploadResult(result.putUrl, result.getUrl, mThreadId);
                        postModel.delivery = PushUtil.MsgDeliveryType.SUCCESS;
                        mCourseThreadPostDataSource.update(postModel);
                        mAdapter.updateItemState(postModel.pid, PushUtil.MsgDeliveryType.SUCCESS);
                    } else {
                        handleNetError("图片上传失败");
                        postModel.delivery = PushUtil.MsgDeliveryType.FAILED;
                        mCourseThreadPostDataSource.update(postModel);
                        mAdapter.updateItemState(postModel.pid, PushUtil.MsgDeliveryType.FAILED);
                    }
                }
            });
            viewMediaLayout.setVisibility(View.GONE);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
    // endregion

    /**
     * 处理用户的提问
     *
     * @param content     内容
     * @param contentType 类型
     */
    private void handleSendThread(final String content, final String contentType) {
        if (!PushUtil.ChatMsgType.TEXT.equals(contentType)) {
            CommonUtil.shortToast(mContext, "描述的问题不能图片或语音");
            return;
        }
        RequestUrl requestUrl = app.bindNewApiUrl(Const.CREATE_THREAD, true);
        Map<String, String> params = requestUrl.getParams();
        params.put("threadType", "course".equals(mTargetType) ? "course" : "common");
        params.put("courseId", mTargetId + "");

        if (mLessonId != 0) {
            params.put("lessonid", String.valueOf(mLessonId));
        }
        params.put("type", "question");
        params.put("title", content);
        params.put("content", content);
        ajaxPost(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.contains("threadId")) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        mThreadId = jsonObject.getInt("threadId");
                        CurrentThreadId = mThreadId;
                        CourseThreadEntity model = createCourseThreadByCurrentUser(content);
                        ThreadDiscussEntity discussModel = convertThreadDiscuss(model);
                        mCourseThreadDataSource.create(model);
                        addItem2ListView(discussModel);
                    } catch (Exception ex) {
                        Log.e(TAG, ex.toString());
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handleNetError("问题提交失败");
            }
        });
    }

    /**
     * 处理用户的回复
     *
     * @param postModel 回复对象
     */
    private void handleSendPost(final CourseThreadPostEntity postModel) {
        String threadType = "course".equals(mTargetType) ? "course" : "common";
        String content = formatContent(postModel, postModel.type);
        mThreadProvider.createThreadPost(mTargetType, mTargetId, threadType, mThreadId, content)
                .success(new NormalCallback<PostThreadResult>() {
                    @Override
                    public void success(PostThreadResult threadResult) {
                        postModel.postId = threadResult.id;
                        postModel.isElite = threadResult.isElite;
                        postModel.createdTime = threadResult.createdTime;
                        postModel.delivery = PushUtil.MsgDeliveryType.SUCCESS;
                        mCourseThreadPostDataSource.update(postModel);
                        mAdapter.updateItemState(postModel.pid, PushUtil.MsgDeliveryType.SUCCESS);
                    }
                }).fail(new NormalCallback<VolleyError>() {
            @Override
            public void success(VolleyError error) {
                String errorMessage = RequestUtil.handleRequestHttpError(new String(error.networkResponse.data));
                if (TextUtils.isEmpty(errorMessage)) {
                    handleNetError(getString(R.string.network_does_not_work));
                } else {
                    handleNetError(errorMessage);
                }
                postModel.delivery = PushUtil.MsgDeliveryType.FAILED;
                mCourseThreadPostDataSource.update(postModel);
                mAdapter.updateItemState(postModel.pid, PushUtil.MsgDeliveryType.FAILED);
            }
        });
    }

    private void initThreadList(final int threadId, final NormalCallback<Boolean> normalCallback) {
        mThreadProvider.getThreadPost(mTargetType, threadId).success(new NormalCallback<CourseThreadPostResult>() {
            @Override
            public void success(CourseThreadPostResult threadPostResult) {
                if (threadPostResult != null && threadPostResult.resources != null) {
                    mPosts = threadPostResult.resources;
                    Collections.reverse(mPosts);
                    filterPostThreads(mPosts);
                    mCourseThreadPostDataSource.deleteByThreadId(threadId);
                    mCourseThreadPostDataSource.create(mPosts);
                    normalCallback.success(true);
                    mLoadDialog.dismiss();
                }
            }
        }).fail(new NormalCallback<VolleyError>() {
            @Override
            public void success(VolleyError obj) {
                mLoadDialog.dismiss();
                handleNetError("问题详情获取失败");
                finish();
            }
        });
    }

    private void getLists(final int threadId, final NormalCallback<Boolean> normalCallback) {
        if (app.getNetIsConnect()) {
            mLoadDialog.show();
            final Promise promise = new Promise();
            promise.then(new PromiseCallback<CourseThreadEntity>() {
                @Override
                public Promise invoke(CourseThreadEntity threadEntity) {
                    if (threadEntity == null) {
                        return null;
                    }
                    mThreadModel = threadEntity;
                    mThreadModel.content = Html.fromHtml(mThreadModel.content).toString();
                    if (mThreadModel.content.length() > 2) {
                        String lastStr = mThreadModel.content.substring(mThreadModel.content.length() - 2, mThreadModel.content.length());
                        if ("\n\n".equals(lastStr)) {
                            mThreadModel.content = mThreadModel.content.substring(0, mThreadModel.content.length() - 2);
                        }
                    }
                    final int threadId = mThreadModel.id;
                    if (mCourseThreadDataSource.get(threadId) == null) {
                        mCourseThreadDataSource.create(mThreadModel);
                    }
                    initThreadList(threadId, normalCallback);
                    return null;
                }
            });
            mThreadProvider.getThread(threadId, "course".equals(mTargetType) ? "course" : "common")
                    .success(new NormalCallback<CourseThreadEntity>() {
                        @Override
                        public void success(CourseThreadEntity threadEntity) {
                            promise.resolve(threadEntity);
                        }
                    });
        } else {
            mThreadModel = mCourseThreadDataSource.get(mThreadId);
            mPosts = mCourseThreadPostDataSource.getPosts(mThreadId);
            Collections.reverse(mPosts);
            normalCallback.success(true);
        }
    }

    private void handleNetError(String msg) {
        CommonUtil.shortToast(mContext, msg);
    }

    private void addItem2ListView(ThreadDiscussEntity model) {
        etSend.setText("");
        etSend.requestFocus();
        mAdapter.addItem(model);
    }

    private String formatContent(CourseThreadPostEntity model, String type) {
        String content;
        switch (type) {
            case PushUtil.ChatMsgType.IMAGE:
                content = String.format(IMAGE_FORMAT, model.upyunMediaGetUrl);
                model.content = model.upyunMediaGetUrl;
                break;
            case PushUtil.ChatMsgType.AUDIO:
                content = String.format(AUDIO_FORMAT, model.upyunMediaGetUrl);
                model.content = model.upyunMediaGetUrl;
                break;
            default:
                content = model.content;
        }
        return content;
    }

    protected View.OnClickListener mAskClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.iv_show_media_layout) {
                //加号，显示多媒体框
                if (mAdapter == null || mAdapter.getCount() == 0) {
                    handleNetError("第一条提问无法发送图片");
                    return;
                }
                if (viewMediaLayout.getVisibility() == View.GONE) {
                    viewMediaLayout.setVisibility(View.VISIBLE);
                    etSend.clearFocus();
                    AppUtil.setSoftKeyBoard(etSend, mActivity, Const.HIDE_KEYBOARD);
                } else {
                    viewMediaLayout.setVisibility(View.GONE);
                }
                lvMessage.post(mListViewSelectRunnable);
            } else if (v.getId() == R.id.btn_voice) {
                //语音
                if (mAdapter == null || mAdapter.getCount() == 0) {
                    handleNetError("第一条提问无法发送语音");
                    return;
                }
                viewMediaLayout.setVisibility(View.GONE);
                btnVoice.setVisibility(View.GONE);
                viewMsgInput.setVisibility(View.GONE);
                btnKeyBoard.setVisibility(View.VISIBLE);
                viewPressToSpeak.setVisibility(View.VISIBLE);
                AppUtil.setSoftKeyBoard(etSend, mActivity, Const.HIDE_KEYBOARD);
            }
        }
    };

    private CourseThreadPostEntity createCoursePostThreadByCurrentUser(String content, String contentType, int deliveryState) {
        CourseThreadPostEntity model = new CourseThreadPostEntity();
        model.courseId = mTargetId;
        model.lessonId = mLessonId;
        model.threadId = mThreadId;
        model.user = new CourseThreadPostEntity.UserEntity();
        model.user.id = app.loginUser.id;
        model.user.nickname = app.loginUser.nickname;
        model.user.mediumAvatar = app.loginUser.mediumAvatar;
        model.content = content;
        model.type = contentType;
        model.delivery = deliveryState;
        model.createdTime = AppUtil.converMillisecond2TimeZone(System.currentTimeMillis());
        return model;
    }

    private CourseThreadEntity createCourseThreadByCurrentUser(String content) {
        CourseThreadEntity model = new CourseThreadEntity();
        model.id = mThreadId;
        model.courseId = mTargetId;
        model.lessonId = mLessonId;
        model.user = new CourseThreadEntity.UserEntity();
        model.user.id = app.loginUser.id;
        model.user.nickname = app.loginUser.nickname;
        model.user.mediumAvatar = app.loginUser.mediumAvatar;
        model.type = PushUtil.ChatMsgType.TEXT;
        model.title = content;
        model.content = content;
        model.createdTime = AppUtil.converMillisecond2TimeZone(System.currentTimeMillis());
        return model;
    }

    private void filterPostThreads(List<CourseThreadPostEntity> posts) {
        try {
            for (CourseThreadPostEntity post : posts) {
                if (post.content.contains("mp3")) {
                    post.type = PushUtil.ChatMsgType.AUDIO;
                    AudioCacheEntity cache = AudioCacheUtil.getInstance().getAudioCacheByPath(post.content);
                    if (cache != null && !TextUtils.isEmpty(cache.localPath)) {
                        post.content = cache.localPath;
                        post.delivery = PushUtil.MsgDeliveryType.SUCCESS;
                    } else {
                        post.delivery = PushUtil.MsgDeliveryType.UPLOADING;
                    }
                } else if (post.content.contains("<img")) {
                    Pattern p = Pattern.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>");
                    Matcher m = p.matcher(post.content);
                    while (m.find()) {
                        post.content = m.group(1);
                        post.type = PushUtil.ChatMsgType.IMAGE;
                        post.delivery = PushUtil.MsgDeliveryType.SUCCESS;
                        break;
                    }
                } else {
                    post.content = Html.fromHtml(post.content).toString();
                    if (post.content.length() > 2) {
                        String lastStr = post.content.substring(post.content.length() - 2, post.content.length());
                        if ("\n\n".equals(lastStr)) {
                            post.content = post.content.substring(0, post.content.length() - 2);
                        }
                    }
                    post.type = PushUtil.ChatMsgType.TEXT;
                    post.delivery = PushUtil.MsgDeliveryType.SUCCESS;
                }
            }
        } catch (Exception ex) {
            Log.d(TAG, "filterPostThreads: " + ex.getMessage());
        }
    }

    // region convert entity

    private ThreadDiscussEntity convertThreadDiscuss(CourseThreadEntity courseThreadEntity) {
        //帖子PostId默认为0，默认发送成功
        return new ThreadDiscussEntity(
                0,
                courseThreadEntity.id,
                courseThreadEntity.courseId,
                courseThreadEntity.lessonId,
                courseThreadEntity.user.id,
                courseThreadEntity.user.nickname,
                courseThreadEntity.user.mediumAvatar,
                courseThreadEntity.content,
                courseThreadEntity.type,
                1,
                courseThreadEntity.createdTime);
    }

    private ThreadDiscussEntity convertThreadDiscuss(CourseThreadPostEntity courseThreadPostEntity) {
        return new ThreadDiscussEntity(
                courseThreadPostEntity.pid,
                courseThreadPostEntity.threadId,
                courseThreadPostEntity.courseId,
                courseThreadPostEntity.lessonId,
                courseThreadPostEntity.user.id,
                courseThreadPostEntity.user.nickname,
                courseThreadPostEntity.user.mediumAvatar,
                courseThreadPostEntity.content,
                courseThreadPostEntity.type,
                courseThreadPostEntity.delivery,
                courseThreadPostEntity.createdTime);
    }

    @Override
    public MessageType[] getMsgTypes() {
        return new MessageType[]{new MessageType(Const.ADD_THREAD_POST, getClass().getSimpleName())};
    }

    @Override
    public void invoke(WidgetMessage message) {

    }

    private void hideHeaderLayout() {
        int headerViewHeight = mHeaderView.getHeight();
        PropertyValuesHolder heightPVH = PropertyValuesHolder.ofInt("height", headerViewHeight, 0);
        ObjectAnimator.ofPropertyValuesHolder(new EduSohoAnimWrap(mHeaderView), heightPVH)
                .setDuration(360).start();
    }

    private void showHeaderLayout() {
        mHeaderView.measure(0, 0);
        int headerViewHeight = mHeaderView.getMeasuredHeight();
        PropertyValuesHolder heightPVH = PropertyValuesHolder.ofInt("height", 0, headerViewHeight);
        PropertyValuesHolder translationYPVH = PropertyValuesHolder.ofFloat("translationY", -headerViewHeight, 0);
        ObjectAnimator.ofPropertyValuesHolder(new EduSohoAnimWrap(mHeaderView), heightPVH, translationYPVH)
                .setDuration(360).start();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mContentLayout.addOnLayoutChangeListener(getOnLayoutChangeListener());
    }

    protected View.OnLayoutChangeListener getOnLayoutChangeListener() {
        return new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                int keyHeight = getWindowManager().getDefaultDisplay().getHeight() / 3;
                if (oldBottom != 0 && bottom != 0 && (oldBottom - bottom > keyHeight)) {
                    hideHeaderLayout();
                } else if (oldBottom != 0 && bottom != 0 && (bottom - oldBottom > keyHeight)) {
                    showHeaderLayout();
                }
            }
        };
    }
}
