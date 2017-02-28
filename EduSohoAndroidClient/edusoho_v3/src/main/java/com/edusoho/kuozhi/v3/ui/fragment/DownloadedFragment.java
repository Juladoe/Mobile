package com.edusoho.kuozhi.v3.ui.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.adapter.DownloadingAdapter;
import com.edusoho.kuozhi.v3.adapter.LessonDownloadingAdapter;
import com.edusoho.kuozhi.v3.entity.lesson.LessonItem;
import com.edusoho.kuozhi.v3.listener.PluginRunCallback;
import com.edusoho.kuozhi.v3.model.bal.course.Course;
import com.edusoho.kuozhi.v3.model.bal.course.CourseDetailsResult;
import com.edusoho.kuozhi.v3.model.sys.MessageType;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.model.sys.WidgetMessage;
import com.edusoho.kuozhi.v3.ui.DownloadManagerActivity;
import com.edusoho.kuozhi.v3.ui.LessonActivity;
import com.edusoho.kuozhi.v3.ui.base.BaseFragment;
import com.edusoho.kuozhi.v3.ui.base.IDownloadFragmenntListener;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.CourseCacheHelper;
import com.edusoho.kuozhi.v3.util.M3U8Util;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

import cn.trinea.android.common.util.ToastUtils;


/**
 * Created by JesseHuang on 15/6/22.
 */
public class DownloadedFragment extends BaseFragment implements IDownloadFragmenntListener {

    private int mCourseId;
    private ListView mListView;
    private View mToolsLayout;
    private TextView mSelectAllBtn;
    private View mDelBtn;
    private View mEmptyView;
    private LessonDownloadingAdapter mDownloadedAdapter;
    private DownloadManagerActivity mActivityContainer;
    public static final String FINISH = "finish";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContainerView(R.layout.fragment_downloaded);
        mCourseId = getArguments().getInt(Const.COURSE_ID, 0);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onSelected(boolean isSelected) {
        if (!isSelected) {
            mDownloadedAdapter.setSelectShow(false);
            getActivity().invalidateOptionsMenu();
        }
    }

    @Override
    protected void initView(View view) {
        mEmptyView = view.findViewById(R.id.ll_downloading_empty);
        mToolsLayout = view.findViewById(R.id.download_tools_layout);
        mSelectAllBtn = (TextView) view.findViewById(R.id.tv_select_all);
        mDelBtn = view.findViewById(R.id.tv_delete);
        mListView = (ListView) view.findViewById(R.id.el_downloaded);
        mActivityContainer = (DownloadManagerActivity) getActivity();
        DownloadManagerActivity.LocalCourseModel finishModel = mActivityContainer.getLocalCourseList(M3U8Util.FINISH, null, null);

        mDownloadedAdapter = new LessonDownloadingAdapter(mContext, finishModel.m3U8DbModels, finishModel.mLocalLessons.get(mCourseId),
                DownloadingAdapter.DownloadType.DOWNLOADED, R.layout.item_downloaded_manager_lesson_child);
        mListView.setAdapter(mDownloadedAdapter);
        mSelectAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv = (TextView) v;
                if (tv.getText().equals("全选")) {
                    MobclickAgent.onEvent(mContext, "i_cache_seleceAll");
                    tv.setText("取消");
                    mDownloadedAdapter.isSelectAll(true);
                } else {
                    tv.setText("全选");
                    mDownloadedAdapter.isSelectAll(false);
                }
            }
        });

        mDelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mActivityContainer != null) {
                    MobclickAgent.onEvent(mContext, "i_cache_edit_delete");
                    ArrayList<Integer> selectedList = mDownloadedAdapter.getSelectLessonId();
                    if (selectedList == null || selectedList.isEmpty()) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("提醒")
                            .setMessage("确认删除缓存课时")
                            .setNegativeButton("取消", null)
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    delLocalLesson(mDownloadedAdapter.getSelectLessonId());
                                }
                            })
                            .create()
                            .show();
                }
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mToolsLayout.getVisibility() == View.GONE) {
                    if (mActivityContainer.isExpired()) {
                        ToastUtils.show(getContext(), R.string.download_course_expird);
                        return;
                    }
                    final LessonItem lessonItem = mDownloadedAdapter.getItem(position);
                    app.mEngine.runNormalPlugin(
                            LessonActivity.TAG, mContext, new PluginRunCallback() {
                                @Override
                                public void setIntentDate(Intent startIntent) {
                                    startIntent.putExtra(Const.COURSE_ID, lessonItem.courseId);
                                    startIntent.putExtra(LessonActivity.FROM_CACHE, true);
                                    startIntent.putExtra(Const.FREE, lessonItem.free);
                                    startIntent.putExtra(Const.LESSON_ID, lessonItem.id);
                                    startIntent.putExtra(Const.LESSON_TYPE, lessonItem.type);
                                    startIntent.putExtra(Const.ACTIONBAR_TITLE, lessonItem.title);
                                }
                            }
                    );
                } else {
                    mDownloadedAdapter.setItemDownloadStatus(position);
                }
            }
        });
        setEmptyState(mDownloadedAdapter.getCount() == 0);
    }

    private void setEmptyState(boolean isEmpty) {
        mEmptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void delLocalLesson(ArrayList<Integer> ids) {
        new CourseCacheHelper(getContext(), app.domain, app.loginUser.id).clearLocalCache(ids);
        DownloadManagerActivity.LocalCourseModel model = mActivityContainer.getLocalCourseList(M3U8Util.FINISH, null, null);
        mDownloadedAdapter.updateLocalData(model.mLocalLessons.get(mCourseId));
        setEmptyState(mDownloadedAdapter.getCount() == 0);
    }

    private void filterCourseLocalCache(DownloadManagerActivity.LocalCourseModel localCourseModels) {
        for (final Course course : localCourseModels.mLocalCourses) {
            RequestUrl requestUrl = app.bindUrl(Const.COURSE + "?courseId=" + course.id, true);
            app.getUrl(requestUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    CourseDetailsResult courseDetailsResult = getUtilFactory().getJsonParser().fromJson(response, CourseDetailsResult.class);
                    if (courseDetailsResult == null || courseDetailsResult.member == null) {
                        deleteLocalCacheByCourseId(course.id);
                    }
                    if (courseDetailsResult != null && courseDetailsResult.member != null && courseDetailsResult.member.deadline < 0) {
                        course.courseDeadline = courseDetailsResult.member.deadline;
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
        }
    }

    private synchronized void deleteLocalCacheByCourseId(int courseId) {
        new CourseCacheHelper(getContext(), app.domain, app.loginUser.id).clearLocalCache(mDownloadedAdapter.getSelectLessonId());
        DownloadManagerActivity.LocalCourseModel model = mActivityContainer.getLocalCourseList(M3U8Util.FINISH, null, null);
        mDownloadedAdapter.updateLocalData(model.mLocalLessons.get(mCourseId));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.offline_menu_edit, menu);
    } @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mDownloadedAdapter.isSelectedShow()) {
            showBtnLayout();
        } else {
            hideBtnLayout();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.offline_menu_edit) {
            if (mDownloadedAdapter.isSelectedShow()) {
                hideBtnLayout();
                item.setTitle("编辑");
                mDownloadedAdapter.setSelectShow(false);
            } else {
                MobclickAgent.onEvent(mContext, "i_myCache_edit");
                showBtnLayout();
                item.setTitle("取消");
                mDownloadedAdapter.setSelectShow(true);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showBtnLayout() {
        mToolsLayout.setVisibility(View.VISIBLE);
    }

    private void hideBtnLayout() {
        mToolsLayout.setVisibility(View.GONE);
    }

    @Override
    public MessageType[] getMsgTypes() {
        MessageType[] messageTypes = new MessageType[]{
                new MessageType(FINISH)
        };
        return messageTypes;
    }

    @Override
    public void invoke(WidgetMessage message) {
        super.invoke(message);
        String type = message.type.type;
        if (FINISH.equals(type)) {
            if (mActivityContainer != null) {
                DownloadManagerActivity.LocalCourseModel model = mActivityContainer.getLocalCourseList(M3U8Util.FINISH, null, null);
                if (model.mLocalCourses.isEmpty()) {
                } else {
                    mDownloadedAdapter.updateLocalData(model.mLocalLessons.get(mCourseId));
                    setEmptyState(mDownloadedAdapter.getCount() == 0);
                }
            }
        }
    }
}

