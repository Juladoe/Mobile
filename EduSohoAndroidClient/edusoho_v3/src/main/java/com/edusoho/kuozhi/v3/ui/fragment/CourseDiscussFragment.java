package com.edusoho.kuozhi.v3.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.VolleyError;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.adapter.discuss.CourseDiscussAdapter;
import com.edusoho.kuozhi.v3.core.CoreEngine;
import com.edusoho.kuozhi.v3.core.MessageEngine;
import com.edusoho.kuozhi.v3.entity.course.DiscussDetail;
import com.edusoho.kuozhi.v3.handler.CourseStateCallback;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.model.provider.CourseDiscussProvider;
import com.edusoho.kuozhi.v3.model.sys.MessageType;
import com.edusoho.kuozhi.v3.model.sys.WidgetMessage;
import com.edusoho.kuozhi.v3.ui.BaseStudyDetailActivity;
import com.edusoho.kuozhi.v3.ui.DiscussDetailActivity;
import com.edusoho.kuozhi.v3.ui.WebViewActivity;
import com.edusoho.kuozhi.v3.ui.chat.AbstractIMChatActivity;
import com.edusoho.kuozhi.v3.ui.course.CourseStudyDetailActivity;
import com.edusoho.kuozhi.v3.ui.course.ICourseStateListener;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Created by DF on 2017/1/4.
 */

public class CourseDiscussFragment extends Fragment implements
        MessageEngine.MessageCallback, SwipeRefreshLayout.OnRefreshListener, ICourseStateListener, BaseStudyDetailActivity.WidgtState {

    private View mLoadView;
    private CourseDiscussAdapter catalogueAdapter;
    private Queue<WidgetMessage> mUIMessageQueue;
    private int mRunStatus;
    private int mCourseId;
    private RecyclerView mRvDiscuss;
    private View mEmpty;
    private boolean isJoin;
    private boolean isFirst = true;
    private boolean isHave = true;
    private View mUnLoginView;
    private int i = 0;
    private int start = 20;
    private CourseStateCallback mCourseStateCallback;
    private SwipeRefreshLayout mSwipe;

    public CourseDiscussFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUIMessageQueue = new ArrayDeque<>();
        ((EdusohoApp) getActivity().getApplication()).registMsgSource(this);
        mCourseId = getArguments().getInt(getActivity() instanceof CourseStudyDetailActivity ? Const.COURSE_ID : Const.CLASSROOM_ID);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCourseStateCallback = (CourseStateCallback) activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discuss, null);
        initView(view);
        return view;
    }

    protected void initView(View view) {
        mUnLoginView = view.findViewById(R.id.ll_no_login);
        mRvDiscuss = (RecyclerView) view.findViewById(R.id.lv_discuss);
        mLoadView = view.findViewById(R.id.ll_frame_load);
        mEmpty = view.findViewById(R.id.ll_discuss_empty);
        mSwipe = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        mSwipe.setColorSchemeResources(R.color.primary_color);
        mSwipe.setOnRefreshListener(this);
        catalogueAdapter = new CourseDiscussAdapter(getActivity());
        mRvDiscuss.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRvDiscuss.setAdapter(catalogueAdapter);
        setRecyclerViewListener();
        if (TextUtils.isEmpty(((EdusohoApp) getActivity().getApplication()).token)) {
            mUnLoginView.setVisibility(View.VISIBLE);
            catalogueAdapter.changeMoreStatus(CourseDiscussAdapter.NO_LOAD_MORE);
            mSwipe.setEnabled(false);
        } else {
            initData();
        }

    }

    private void setRecyclerViewListener() {
        mRvDiscuss.setOnScrollListener(new RecyclerView.OnScrollListener() {
            int lastVisibleItem;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem == catalogueAdapter.getItemCount() - 1) {
                    catalogueAdapter.changeMoreStatus(CourseDiscussAdapter.LOADING_MORE);
                    //设置正在加载更多
                    if (!isHave && !mEmpty.isShown()) {
                        if (isFirst) {
                            isFirst = false;
                            CommonUtil.shortCenterToast(getContext(), getString(R.string.discuss_load_data_finish));
                        }
                        catalogueAdapter.changeMoreStatus(CourseDiscussAdapter.NO_LOAD_MORE);
                        return;
                    }
                    new CourseDiscussProvider(getContext()).getCourseDiscuss(getActivity() instanceof CourseStudyDetailActivity, mCourseId, start)
                            .success(new NormalCallback<DiscussDetail>() {
                                @Override
                                public void success(DiscussDetail discussDetail) {
                                    if (getActivity() == null || getActivity().isFinishing() || !isAdded()) {
                                        return;
                                    }
                                    start += 20;
                                    if (discussDetail.getResources().size() < 20) {
                                        isHave = false;
                                    } else {
                                        isHave = true;
                                    }
                                    catalogueAdapter.setStatus(CourseDiscussAdapter.NO_LOAD_MORE);
                                    catalogueAdapter.AddFooterItem(discussDetail.getResources());
                                }
                            }).fail(new NormalCallback<VolleyError>() {
                        @Override
                        public void success(VolleyError obj) {
                            catalogueAdapter.changeMoreStatus(CourseDiscussAdapter.NO_LOAD_MORE);
                        }
                    });
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                //最后一个可见的ITEM
                lastVisibleItem = layoutManager.findLastVisibleItemPosition();
            }
        });

        catalogueAdapter.setOnItemClickListener(new CourseDiscussAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, DiscussDetail.ResourcesBean resourcesBean) {
                if (mCourseStateCallback.isExpired()) {
                    mCourseStateCallback.handlerCourseExpired();
                    return;
                }
                startThreadActivity(resourcesBean);
            }
        });
    }

    private void initData() {
        mLoadView.setVisibility(View.VISIBLE);
        mSwipe.setEnabled(true);
        mUnLoginView.setVisibility(View.GONE);
        new CourseDiscussProvider(getContext()).getCourseDiscuss(getActivity() instanceof CourseStudyDetailActivity, mCourseId, 0)
                .success(new NormalCallback<DiscussDetail>() {
                    @Override
                    public void success(DiscussDetail discussDetail) {
                        if (getActivity() == null || getActivity().isFinishing() || !isAdded()) {
                            return;
                        }
                        if (discussDetail.getResources() != null && discussDetail.getResources().size() != 0) {
                            initDiscuss(discussDetail);
                        } else {
                            initDiscuss(null);
                        }
                    }
                }).fail(new NormalCallback<VolleyError>() {
            @Override
            public void success(VolleyError obj) {
                mLoadView.setVisibility(View.GONE);
                setLessonEmptyViewVisibility(View.VISIBLE);
            }
        });
    }

    private void initDiscuss(final DiscussDetail discussDetail) {
        mLoadView.setVisibility(View.GONE);
        if (discussDetail == null) {
            isHave = false;
            mLoadView.setVisibility(View.GONE);
            mEmpty.setVisibility(View.VISIBLE);
            catalogueAdapter.changeMoreStatus(CourseDiscussAdapter.NO_LOAD_MORE);
            return;
        }
        if (discussDetail.getResources().size() < 20) {
            isHave = false;
        }
        catalogueAdapter.setStatus(CourseDiscussAdapter.NO_LOAD_MORE);
        catalogueAdapter.setDataAndNotifyData(discussDetail.getResources());

    }

    private void setLessonEmptyViewVisibility(int visibility) {
        mEmpty.setVisibility(visibility);
    }

    @Override
    public void reFreshView(boolean isJoin) {
        this.isJoin = isJoin;
        if (!TextUtils.isEmpty(((EdusohoApp) getActivity().getApplication()).token)) {
            initData();
        }
    }

    @Override
    public MessageType[] getMsgTypes() {
        return new MessageType[]{new MessageType(WebViewActivity.SEND_EVENT)};
    }

    @Override
    public int getMode() {
        return 0;
    }

    public void invoke(WidgetMessage message) {
        if (WebViewActivity.SEND_EVENT.equals(message.type.type)) {
            i = 1;
        }
    }

    private void startThreadActivity(DiscussDetail.ResourcesBean resourcesBean) {
        if (isJoin) {
            Bundle bundle = new Bundle();
            bundle.putString(DiscussDetailActivity.THREAD_TARGET_TYPE, getActivity() instanceof CourseStudyDetailActivity ? "course" : "classroom");
            bundle.putInt(DiscussDetailActivity.THREAD_TARGET_ID, getActivity() instanceof CourseStudyDetailActivity ? Integer.parseInt(resourcesBean.getCourseId())
                    : Integer.parseInt(resourcesBean.getTargetId()));
            bundle.putInt(AbstractIMChatActivity.FROM_ID, Integer.parseInt(resourcesBean.getId()));
            bundle.putString(AbstractIMChatActivity.TARGET_TYPE, resourcesBean.getType());
            CoreEngine.create(getContext()).runNormalPluginWithBundle("DiscussDetailActivity", getActivity(), bundle);
        } else {
            CommonUtil.shortCenterToast(getContext(), getString(R.string.discuss_join_hint));
        }
    }

    public void onRefresh() {
        new CourseDiscussProvider(getContext()).getCourseDiscuss(getActivity() instanceof CourseStudyDetailActivity, mCourseId, 0)
                .success(new NormalCallback<DiscussDetail>() {
                    @Override
                    public void success(DiscussDetail discussDetail) {
                        if (getActivity() == null || getActivity().isFinishing() || !isAdded()) {
                            return;
                        }
                        mSwipe.setRefreshing(false);
                        if (discussDetail.getResources() != null && discussDetail.getResources().size() != 0 && catalogueAdapter != null) {
                            catalogueAdapter.reFreshData(discussDetail.getResources());
                        }
                    }
                }).fail(new NormalCallback<VolleyError>() {
            @Override
            public void success(VolleyError obj) {
                mSwipe.setRefreshing(false);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mRunStatus = MSG_RESUME;
        if (i == 1) {
            initData();
            mEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mRunStatus = MSG_PAUSE;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((EdusohoApp) getActivity().getApplication()).unRegistMsgSource(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        i = 0;
    }

    @Override
    public void setTopViewVisibility(boolean enabled) {
        if (mSwipe != null && mUnLoginView.getVisibility() == View.GONE) {
            mSwipe.setEnabled(enabled);
        }
    }
}
