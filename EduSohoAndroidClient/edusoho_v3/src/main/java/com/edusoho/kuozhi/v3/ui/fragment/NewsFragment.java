package com.edusoho.kuozhi.v3.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.imserver.IMClient;
import com.edusoho.kuozhi.imserver.entity.ConvEntity;
import com.edusoho.kuozhi.imserver.entity.MessageEntity;
import com.edusoho.kuozhi.imserver.entity.ReceiverInfo;
import com.edusoho.kuozhi.imserver.entity.Role;
import com.edusoho.kuozhi.imserver.entity.message.Destination;
import com.edusoho.kuozhi.imserver.entity.message.MessageBody;
import com.edusoho.kuozhi.imserver.listener.IMConnectStatusListener;
import com.edusoho.kuozhi.imserver.listener.IMMessageReceiver;
import com.edusoho.kuozhi.imserver.managar.IMRoleManager;
import com.edusoho.kuozhi.imserver.util.IMConnectStatus;
import com.edusoho.kuozhi.v3.adapter.SwipeAdapter;
import com.edusoho.kuozhi.v3.core.CoreEngine;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.listener.PluginRunCallback;
import com.edusoho.kuozhi.v3.model.bal.course.Course;
import com.edusoho.kuozhi.v3.model.bal.course.CourseResult;
import com.edusoho.kuozhi.v3.model.bal.http.ModelDecor;
import com.edusoho.kuozhi.v3.model.bal.push.New;
import com.edusoho.kuozhi.v3.model.bal.push.TypeBusinessEnum;
import com.edusoho.kuozhi.v3.model.provider.IMProvider;
import com.edusoho.kuozhi.v3.model.sys.MessageType;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.model.sys.WidgetMessage;
import com.edusoho.kuozhi.v3.ui.ClassroomDiscussActivity;
import com.edusoho.kuozhi.v3.ui.DefaultPageActivity;
import com.edusoho.kuozhi.v3.ui.ImChatActivity;
import com.edusoho.kuozhi.v3.ui.NewsCourseActivity;
import com.edusoho.kuozhi.v3.ui.ServiceProviderActivity;
import com.edusoho.kuozhi.v3.ui.base.BaseFragment;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.PushUtil;
import com.edusoho.kuozhi.v3.view.swipemenulistview.SwipeMenu;
import com.edusoho.kuozhi.v3.view.swipemenulistview.SwipeMenuCreator;
import com.edusoho.kuozhi.v3.view.swipemenulistview.SwipeMenuItem;
import com.edusoho.kuozhi.v3.view.swipemenulistview.SwipeMenuListView;
import com.google.gson.reflect.TypeToken;
import com.umeng.analytics.MobclickAgent;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by JesseHuang on 15/4/26.
 * 动态列表
 */
public class NewsFragment extends BaseFragment {
    public static final String TAG = "NewsFragment";
    public static final int UPDATE_UNREAD_MSG = 17;
    public static final int UPDATE_UNREAD_BULLETIN = 18;
    public static final int UPDATE_UNREAD_NEWS_COURSE = 19;
    public static final int UPDATE_UNREAD_ARTICLE_CREATE = 20;

    public static final int SHOW = 60;
    public static final int DISMISS = 61;

    private List<ConvEntity> mConvEntityList;
    private SwipeMenuListView lvNewsList;
    private SwipeAdapter mSwipeAdapter;
    private View mEmptyView;
    private TextView tvEmptyText;
    private TextView mHeaderView;
    private View mLoadProgressBar;
    private LoadingHandler mLoadingHandler;
    private boolean mIsNeedRefresh;
    private DefaultPageActivity mParentActivity;
    private IMMessageReceiver mIMMessageReceiver;
    private IMConnectStatusListener mIMConnectStatusListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContainerView(R.layout.fragment_news);
        mLoadingHandler = new LoadingHandler(this);
        syncIMData();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mParentActivity.getCurrentFragment().equals(getClass().getSimpleName())) {
            getRestCourse();
        } else {
            mIsNeedRefresh = true;
        }
        registIMMessageReceiver();
        mIMConnectStatusListener = getIMConnectStatusListener();
        IMClient.getClient().addConnectStatusListener(mIMConnectStatusListener);
        updateIMConnectStatus(IMClient.getClient().getIMConnectStatus());
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mIMMessageReceiver != null) {
            IMClient.getClient().removeReceiver(mIMMessageReceiver);
            mIMMessageReceiver = null;
        }
        if (mIMConnectStatusListener != null) {
            IMClient.getClient().removeConnectStatusListener(mIMConnectStatusListener);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    private void registIMMessageReceiver() {
        if (mIMMessageReceiver != null) {
            return;
        }

        mIMMessageReceiver = getIMMessageListener();
        IMClient.getClient().addMessageReceiver(mIMMessageReceiver);
    }

    protected IMMessageReceiver getIMMessageListener() {
        return new IMMessageReceiver() {

            private boolean filterMessageEntity(MessageEntity messageEntity) {
                if (Destination.LESSON.equals(messageEntity.getConvNo())) {
                    return false;
                }
                return true;
            }

            @Override
            public boolean onReceiver(MessageEntity msg) {
                if (filterMessageEntity(msg)) {
                    handleMessage(msg);
                }
                return false;
            }

            @Override
            public boolean onOfflineMsgReceiver(List<MessageEntity> messageEntities) {
                handleOfflineMessage(messageEntities);
                return false;
            }

            @Override
            public void onSuccess(MessageEntity extr) {
            }

            @Override
            public ReceiverInfo getType() {
                return new ReceiverInfo(Destination.LIST, "0");
            }
        };
    }

    protected void handleOfflineMessage(List<MessageEntity> list) {
        for (MessageEntity messageEntity : list) {
            handleMessage(messageEntity);
        }
    }

    protected void handleMessage(MessageEntity messageEntity) {
        New newItem = findItemInList(messageEntity.getConvNo());
        if (newItem == null) {
            newItem = new New(messageEntity);
            Role role = IMClient.getClient().getRoleManager().getRole(newItem.type, newItem.fromId);
            if (role.getRid() != 0) {
                newItem.setImgUrl(role.getAvatar());
                newItem.setTitle(role.getNickname());
            }
            newItem.setUnread(1);
            mSwipeAdapter.addItem(newItem);
            setListVisibility(mSwipeAdapter.getCount() == 0);
            return;
        }

        newItem.setUnread(++newItem.unread);
        MessageBody messageBody = new MessageBody(messageEntity);
        newItem.setContent(messageBody);
        mSwipeAdapter.updateItem(newItem);
        mSwipeAdapter.setItemToTop(newItem);
    }

    private New findItemInList(String convNo) {
        int count = mSwipeAdapter.getCount();
        for (int i = 0; i < count; i++) {
            New item = mSwipeAdapter.getItem(i);
            if (convNo.equals(item.convNo)) {
                return item;
            }
        }

        return null;
    }

    private void updateIMConnectStatus(int status) {
        switch (status) {
            case IMConnectStatus.CLOSE:
            case IMConnectStatus.END:
            case IMConnectStatus.ERROR:
                if (!getAppSettingProvider().getAppConfig().isEnableIMChat) {
                    mLoadingHandler.sendEmptyMessage(DISMISS);
                    updateNetWorkStatusHeader("聊天功能已关闭, 请联系管理员");
                    return;
                }
                mLoadingHandler.sendEmptyMessage(SHOW);
                //updateNetWorkStatusHeader("消息服务器连接失败，请重试");
                updateNetWorkStatusHeader("");
                break;
            case IMConnectStatus.CONNECTING:
                mLoadingHandler.sendEmptyMessage(SHOW);
                //updateNetWorkStatusHeader("正在连接...");
                updateNetWorkStatusHeader("");
                break;
            case IMConnectStatus.NO_READY:
                mLoadingHandler.sendEmptyMessage(DISMISS);
                if (!getAppSettingProvider().getAppConfig().isEnableIMChat) {
                    updateNetWorkStatusHeader("聊天功能已关闭, 请联系管理员");
                    return;
                }
                updateNetWorkStatusHeader("消息服务未连接，请重试");
                break;
            case IMConnectStatus.OPEN:
            default:
                mLoadingHandler.sendEmptyMessage(DISMISS);
                updateNetWorkStatusHeader("");
        }
    }

    private IMConnectStatusListener getIMConnectStatusListener() {
        return new IMConnectStatusListener() {
            @Override
            public void onError() {
                updateIMConnectStatus(IMConnectStatus.ERROR);
            }

            @Override
            public void onClose() {
                updateIMConnectStatus(IMConnectStatus.CLOSE);
            }

            @Override
            public void onConnect() {
                updateIMConnectStatus(IMConnectStatus.CONNECTING);
            }

            @Override
            public void onOpen() {
                hideNetWorkStatusHeader();
            }

            @Override
            public void onInvalid(String[] ig) {
            }
        };
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden && mIsNeedRefresh) {
            getRestCourse();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.news_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.news_search) {
            MobclickAgent.onEvent(mContext, "dynamic_sweepButton");
            app.mEngine.runNormalPlugin("QrSearchActivity", mContext, null);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void initView(View view) {
        mParentActivity = (DefaultPageActivity) getActivity();
        mIsNeedRefresh = true;
        mLoadProgressBar = view.findViewById(R.id.news_progressbar);
        lvNewsList = (SwipeMenuListView) view.findViewById(R.id.lv_news_list);
        mEmptyView = view.findViewById(R.id.view_empty);
        tvEmptyText = (TextView) view.findViewById(R.id.tv_empty_text);
        tvEmptyText.setText(getResources().getString(R.string.news_empty_text));
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        mContext);
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                deleteItem.setWidth(AppUtil.dp2px(mContext, 65));
                deleteItem.setIcon(R.drawable.ic_delete);
                menu.addMenuItem(deleteItem);
            }
        };
        addHeadView();
        mSwipeAdapter = new SwipeAdapter(mContext, R.layout.news_item);
        lvNewsList.setAdapter(mSwipeAdapter);
        lvNewsList.setMenuCreator(creator);
        lvNewsList.setOnMenuItemClickListener(mMenuItemClickListener);
        lvNewsList.setOnItemClickListener(mItemClickListener);
        lvNewsList.setOnSwipeListener(getOnSwipeListener());
    }

    private SwipeMenuListView.OnSwipeListener getOnSwipeListener() {
        return new SwipeMenuListView.OnSwipeListener() {
            @Override
            public void onSwipeStart(int position) {
            }

            @Override
            public void onSwipeEnd(int position) {
            }

            @Override
            public boolean canSwipe(int position) {
                New item = mSwipeAdapter.getItem(position);
                if (item == null) {
                    return false;
                }
                return !Destination.NOTIFY.equals(item.getType());
            }
        };
    }

    private void initData() {
        if (app.loginUser == null) {
            return;
        }
        mConvEntityList = IMClient.getClient().getConvManager().getConvList();
        mSwipeAdapter.update(coverConvListToNewList(mConvEntityList));
        setListVisibility(mSwipeAdapter.getCount() == 0);
        mLoadProgressBar.setVisibility(View.GONE);
    }

    private List<New> coverConvListToNewList(List<ConvEntity> convEntityList) {
        List<New> newList = new ArrayList<>();
        IMRoleManager roleManager = IMClient.getClient().getRoleManager();
        for (ConvEntity convEntity : convEntityList) {
            New newItem = new New(convEntity);
            Role role = roleManager.getRole(convEntity.getType(), convEntity.getTargetId());
            if (role.getRid() != 0) {
                newItem.setImgUrl(role.getAvatar());
            }
            newList.add(newItem);
        }

        return newList;
    }

    private void syncIMData() {
        new IMProvider(mContext).syncIMRoleData();
    }

    private SwipeMenuListView.OnMenuItemClickListener mMenuItemClickListener = new SwipeMenuListView.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
            New newModel = mSwipeAdapter.getItem(position);
            IMClient.getClient().getMessageManager().deleteByConvNo(newModel.convNo);
            IMClient.getClient().getConvManager().deleteConv(newModel.convNo);

            mSwipeAdapter.removeItem(position);
            setListVisibility(mSwipeAdapter.getCount() == 0);
            return false;
        }
    };

    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mHeaderView != null && position == 0) {
                return;
            }
            final New newItem = (New) parent.getItemAtPosition(position);
            TypeBusinessEnum.getName(newItem.type);
            switch (newItem.type) {
                case Destination.NOTIFY:
                    CoreEngine.create(mContext).runNormalPlugin("NotifyActivity", mContext, null);
                    break;
                case Destination.USER:
                    if (!getAppSettingProvider().getAppConfig().isEnableIMChat) {
                        CommonUtil.longToast(mContext, "聊天功能已关闭");
                        return;
                    }
                    app.mEngine.runNormalPlugin("ImChatActivity", mContext, new PluginRunCallback() {
                        @Override
                        public void setIntentDate(Intent startIntent) {
                            startIntent.putExtra(ImChatActivity.FROM_ID, newItem.fromId);
                            startIntent.putExtra(ImChatActivity.FROM_NAME, newItem.title);
                            startIntent.putExtra(Const.NEWS_TYPE, newItem.type);
                            startIntent.putExtra(ImChatActivity.CONV_NO, newItem.convNo);
                            startIntent.putExtra(ImChatActivity.HEAD_IMAGE_URL, newItem.imgUrl);
                        }
                    });
                    break;
                case Destination.CLASSROOM:
                    if (!getAppSettingProvider().getAppConfig().isEnableIMChat) {
                        CommonUtil.longToast(mContext, "聊天功能已关闭");
                        return;
                    }
                    app.mEngine.runNormalPlugin("ClassroomDiscussActivity", mContext, new PluginRunCallback() {
                        @Override
                        public void setIntentDate(Intent startIntent) {
                            startIntent.putExtra(ClassroomDiscussActivity.FROM_ID, newItem.fromId);
                            startIntent.putExtra(ClassroomDiscussActivity.FROM_NAME, newItem.title);
                            startIntent.putExtra(ClassroomDiscussActivity.CONV_NO, newItem.convNo);
                        }
                    });
                    break;
                case PushUtil.BulletinType.TYPE:
                    app.mEngine.runNormalPlugin("BulletinActivity", mContext, new PluginRunCallback() {
                        @Override
                        public void setIntentDate(Intent startIntent) {
                            startIntent.putExtra("", newItem.convNo);
                        }
                    });
                    break;
                case Destination.COURSE:
                    app.mEngine.runNormalPlugin("NewsCourseActivity", mContext, new PluginRunCallback() {
                        @Override
                        public void setIntentDate(Intent startIntent) {
                            startIntent.putExtra(NewsCourseActivity.COURSE_ID, newItem.fromId);
                            startIntent.putExtra(NewsCourseActivity.FROM_NAME, newItem.title);
                            startIntent.putExtra(NewsCourseActivity.CONV_NO, newItem.convNo);
                            startIntent.putExtra(
                                    NewsCourseActivity.SHOW_TYPE,
                                    newItem.unread > 0 ? NewsCourseActivity.DISCUSS_TYPE : NewsCourseActivity.LEARN_TYPE
                            );
                        }
                    });
                    break;
                case Destination.ARTICLE:
                    app.mEngine.runNormalPlugin("ServiceProviderActivity", mContext, new PluginRunCallback() {
                        @Override
                        public void setIntentDate(Intent startIntent) {
                            startIntent.putExtra(ServiceProviderActivity.SERVICE_TYPE, PushUtil.ArticleType.TYPE);
                            startIntent.putExtra(ServiceProviderActivity.SERVICE_ID, newItem.fromId);
                            startIntent.putExtra(ServiceProviderActivity.CONV_NO, newItem.convNo);
                            startIntent.putExtra(Const.ACTIONBAR_TITLE, "资讯");
                        }
                    });
                    break;
            }
        }
    };

    @Override
    public void invoke(WidgetMessage message) {
        switch (message.type.code) {
            case Const.REFRESH_LIST:
                initData();
        }
    }

    @Override
    public MessageType[] getMsgTypes() {
        String source = this.getClass().getSimpleName();
        return new MessageType[]{
                new MessageType(Const.ADD_MSG, source),
                new MessageType(Const.ADD_BULLETIN_MSG, source),
                new MessageType(Const.ADD_ARTICLE_CREATE_MAG, source),
                new MessageType(Const.LOGIN_SUCCESS),
                new MessageType(UPDATE_UNREAD_MSG, source),
                new MessageType(UPDATE_UNREAD_BULLETIN, source),
                new MessageType(UPDATE_UNREAD_NEWS_COURSE, source),
                new MessageType(Const.REFRESH_LIST, source),
                new MessageType(Const.ADD_THREAD_POST, source)};
    }

    /**
     * 设置空数据背景ICON
     *
     * @param visibility 是否空数据
     */

    private void setListVisibility(boolean visibility) {
        lvNewsList.setVisibility(visibility ? View.GONE : View.VISIBLE);
        mEmptyView.setVisibility(visibility ? View.VISIBLE : View.GONE);
    }

    private void hideNetWorkStatusHeader() {
        updateNetWorkStatusHeader("");
    }

    private void addHeadView() {
        View headerRootView = LayoutInflater.from(mContext).inflate(R.layout.view_new_header_layout, lvNewsList, false);
        mHeaderView = (TextView) headerRootView.findViewById(R.id.header_title);
        lvNewsList.addHeaderView(headerRootView);
    }

    public void updateNetWorkStatusHeader(String statusText) {
        mHeaderView.setText(statusText);
        ViewGroup.LayoutParams lp = mHeaderView.getLayoutParams();
        if (lp == null) {
            lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mHeaderView.setLayoutParams(lp);
        }
        lp.height = TextUtils.isEmpty(statusText) ? 0 : 64;
        mHeaderView.setVisibility(TextUtils.isEmpty(statusText) ? View.GONE : View.VISIBLE);
        mHeaderView.setLayoutParams(lp);
    }

    private void getLearnCourses(final NormalCallback<CourseResult> normalCallback) {
        RequestUrl requestUrl = app.bindNewApiUrl(Const.MY_COURSES + "relation=learn&limit=1000", true);
        mActivity.ajaxGet(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                CourseResult courseResult = ModelDecor.getInstance().decor(response, new TypeToken<CourseResult>() {
                });
                if (courseResult.resources != null) {
                    normalCallback.success(courseResult);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mLoadingHandler.sendEmptyMessage(DISMISS);
            }
        });
    }

    private void getTeachingCourses(final NormalCallback<CourseResult> normalCallback) {
        RequestUrl requestUrl = app.bindNewApiUrl(Const.MY_COURSES + "relation=teaching&limit=1000", true);
        mActivity.ajaxGet(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                CourseResult courseResult = ModelDecor.getInstance().decor(response, new TypeToken<CourseResult>() {
                });
                if (courseResult.resources != null) {
                    normalCallback.success(courseResult);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mLoadingHandler.sendEmptyMessage(DISMISS);
            }
        });
    }

    private void getRestCourse() {
        try {
            mIsNeedRefresh = false;
            mLoadingHandler.sendEmptyMessage(SHOW);
            getLearnCourses(new NormalCallback<CourseResult>() {
                @Override
                public void success(final CourseResult learnCourses) {
                    if (PushUtil.ChatUserType.TEACHER.equals(app.getCurrentUserRole())) {
                        getTeachingCourses(new NormalCallback<CourseResult>() {
                            @Override
                            public void success(CourseResult teachingCourses) {
                                Course[] courses = CommonUtil.concatArray(learnCourses.resources, teachingCourses.resources);
                                filterMyCourses(courses);

                            }
                        });
                    } else {
                        filterMyCourses(learnCourses.resources);
                    }
                    mLoadingHandler.sendEmptyMessage(DISMISS);
                }
            });
        } catch (Exception ex) {
            mLoadingHandler.sendEmptyMessage(DISMISS);
        }
    }

    private void filterMyCourses(Course[] courses) {
        List<ConvEntity> convEntityList = IMClient.getClient().getConvManager().getConvListByType(Destination.COURSE);
        //本地已经存在的course ids
        SparseArray<ConvEntity> existCourseIds = new SparseArray<>();
        for (ConvEntity convEntity : convEntityList) {
            existCourseIds.put(convEntity.getTargetId(), convEntity);
        }

        //服务器端最新的course ids
        SparseArray<ConvEntity> newConvEntityList = new SparseArray<>();
        ConvEntity convEntity = null;
        for (Course course : courses) {
            if (existCourseIds.indexOfKey(course.id) >= 0) {
                convEntity = existCourseIds.get(course.id);
                convEntity.setAvatar(course.middlePicture);
                convEntity.setTargetName(course.title);
                IMClient.getClient().getConvManager().updateConvByConvNo(convEntity);
                newConvEntityList.put(course.id, convEntity);
                existCourseIds.remove(course.id);
                continue;
            }

            if (TextUtils.isEmpty(course.convNo) || "0".equals(course.convNo)) {
                continue;
            }
            long resultId = IMClient.getClient().getConvManager().createConv(createConvFromCourse(course));
            if (resultId > 0) {
                newConvEntityList.put(course.id, convEntity);
            }
        }

        new IMProvider(mContext).updateRolesByCourse(Destination.COURSE, Arrays.asList(courses));
        int existCourseIdsCount = existCourseIds.size();
        for (int i = 0; i < existCourseIdsCount; i++) {
            convEntity = existCourseIds.valueAt(i);
            IMClient.getClient().getConvManager().deleteById(convEntity.getId());
        }

        initData();
    }

    private ConvEntity createConvFromCourse(Course course) {
        ConvEntity convEntity = new ConvEntity();
        convEntity.setUid(app.loginUser.id);
        convEntity.setTargetId(course.id);
        convEntity.setTargetName(course.title);
        convEntity.setConvNo(course.convNo);
        convEntity.setAvatar(course.middlePicture);
        convEntity.setType(Destination.COURSE);
        convEntity.setCreatedTime(System.currentTimeMillis());

        return convEntity;
    }

    private static class LoadingHandler extends Handler {
        private final WeakReference<NewsFragment> mFragment;

        public LoadingHandler(NewsFragment fragment) {
            mFragment = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            NewsFragment fragment = mFragment.get();
            if (fragment != null) {
                try {
                    switch (msg.what) {
                        case SHOW:
                            fragment.mParentActivity.setTitleLoading(true);
                            break;
                        case DISMISS:
                            fragment.mParentActivity.setTitleLoading(false);
                            break;
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "handleMessage: " + ex.getMessage());
                }
            }
        }
    }
}
