package com.edusoho.kuozhi.v3.ui.fragment;

import android.content.DialogInterface;
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
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.adapter.DownloadingAdapter;
import com.edusoho.kuozhi.v3.adapter.LessonDownloadingAdapter;
import com.edusoho.kuozhi.v3.entity.lesson.LessonItem;
import com.edusoho.kuozhi.v3.model.bal.m3u8.M3U8DbModel;
import com.edusoho.kuozhi.v3.model.sys.MessageType;
import com.edusoho.kuozhi.v3.model.sys.WidgetMessage;
import com.edusoho.kuozhi.v3.service.M3U8DownService;
import com.edusoho.kuozhi.v3.ui.DownloadManagerActivity;
import com.edusoho.kuozhi.v3.ui.base.BaseFragment;
import com.edusoho.kuozhi.v3.ui.base.IDownloadFragmenntListener;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.CourseCacheHelper;
import com.edusoho.kuozhi.v3.util.M3U8Util;
import com.edusoho.kuozhi.v3.view.dialog.ExitCoursePopupDialog;
import com.edusoho.kuozhi.v3.view.dialog.PopupDialog;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

import cn.trinea.android.common.util.ToastUtils;

/**
 * Created by JesseHuang on 15/6/22.
 * 下载中
 */
public class DownloadingFragment extends BaseFragment implements IDownloadFragmenntListener {

    private int mCourseId;
    private ListView mListView;
    private View mToolsLayout;
    private TextView mSelectAllBtn;
    private View mDelBtn;
    private View mEmptyView;
    private LessonDownloadingAdapter mDownloadingAdapter;
    private DownloadManagerActivity mActivityContainer;
    public static final String UPDATE = "update";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContainerView(R.layout.fragment_downloading);
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
            mDownloadingAdapter.setSelectShow(false);
            getActivity().invalidateOptionsMenu();
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mDownloadingAdapter.isSelectedShow()) {
            showBtnLayout();
        } else {
            hideBtnLayout();
        }
    }

    @Override
    protected void initView(View view) {
        mToolsLayout = view.findViewById(R.id.download_tools_layout);
        mEmptyView = view.findViewById(R.id.ll_downloading_empty);
        mSelectAllBtn = (TextView) view.findViewById(R.id.tv_select_all);
        mDelBtn = view.findViewById(R.id.tv_delete);
        mListView = (ListView) view.findViewById(R.id.el_downloading);
        mActivityContainer = (DownloadManagerActivity) getActivity();
        DownloadManagerActivity.LocalCourseModel unFinishModel = mActivityContainer.getLocalCourseList(M3U8Util.UN_FINISH, null, null);

        mDownloadingAdapter = new LessonDownloadingAdapter(mContext, unFinishModel.m3U8DbModels,
                unFinishModel.mLocalLessons.get(mCourseId), DownloadingAdapter.DownloadType.DOWNLOADING, R.layout.item_downloading_manager_lesson_child);
        mListView.setAdapter(mDownloadingAdapter);

        mSelectAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv = (TextView) v;
                if (tv.getText().equals("全选")) {
                    MobclickAgent.onEvent(mContext, "i_cache_seleceAll");
                    tv.setText("取消");
                    mDownloadingAdapter.isSelectAll(true);
                } else {
                    tv.setText("全选");
                    mDownloadingAdapter.isSelectAll(false);
                }
            }
        });

        mDelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mActivityContainer != null) {
                    MobclickAgent.onEvent(mContext, "i_cache_edit_delete");
                    ArrayList<Integer> selectedList = mDownloadingAdapter.getSelectLessonId();
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
                                    delLocalLesson(mDownloadingAdapter.getSelectLessonId());
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
                    processdownloadItemClick(view, position);
                } else {
                    mDownloadingAdapter.setItemDownloadStatus(position);
                }
            }
        });
        setEmptyState(mDownloadingAdapter.getCount() == 0);
    }

    private void delLocalLesson(ArrayList<Integer> ids) {
        new CourseCacheHelper(getContext(), app.domain, app.loginUser.id).clearLocalCache(mDownloadingAdapter.getSelectLessonId());
        DownloadManagerActivity.LocalCourseModel model = mActivityContainer.getLocalCourseList(M3U8Util.UN_FINISH, null, null);
        mDownloadingAdapter.updateLocalData(model.mLocalLessons.get(mCourseId));
        setEmptyState(mDownloadingAdapter.getCount() == 0);
    }

    private void setEmptyState(boolean isEmpty) {
        mEmptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void processdownloadItemClick(View view, int position) {
        if (mDownloadingAdapter.isSelectedShow()) {
            return;
        }
        if (!app.getNetIsConnect()) {
            ToastUtils.show(mActivity, "当前无网络连接!");
            return;
        }
        int offlineType = app.config.offlineType;
        if (offlineType == Const.NET_NONE) {
            showAlertDialog("当前设置视频课时观看、下载为禁止模式!\n模式可以在设置里修改。");
            return;
        }
        if (offlineType == Const.NET_WIFI && !app.getNetIsWiFi()) {
            showAlertDialog("当前设置视频课时观看、下载为WiFi模式!\n模式可以在设置里修改。");
            return;
        }
        LessonItem lessonItem = mDownloadingAdapter.getItem(position);
        M3U8DownService service = M3U8DownService.getService();
        if (service == null) {
            M3U8DownService.startDown(
                    mActivity.getBaseContext(), lessonItem.id, lessonItem.courseId, lessonItem.title);
            return;
        }
        int state = service.getTaskStatus(lessonItem.id);
        if (state == M3U8Util.DOWNING) {
            service.cancelDownloadTask(lessonItem.id);
        } else {
            service.changeTaskState(lessonItem.id, lessonItem.courseId, lessonItem.title);
        }
    }

    private void showAlertDialog(String content) {
        PopupDialog popupDialog = PopupDialog.createMuilt(
                mActivity,
                "播放提示",
                content,
                new PopupDialog.PopupClickListener() {
                    @Override
                    public void onClick(int button) {
                        if (button == PopupDialog.OK) {
                            ExitCoursePopupDialog dialog = ExitCoursePopupDialog.createNormal(
                                    mActivity, "视频课时下载播放", new ExitCoursePopupDialog.PopupClickListener() {
                                        @Override
                                        public void onClick(int button, int position, String selStr) {
                                            if (button == ExitCoursePopupDialog.CANCEL) {
                                                return;
                                            }

                                            EdusohoApp app = EdusohoApp.app;
                                            app.config.offlineType = position;
                                            app.saveConfig();
                                        }
                                    }
                            );
                            dialog.setStringArray(R.array.offline_array);
                            dialog.show();
                        }
                    }
                });
        popupDialog.setOkText("去设置");
        popupDialog.show();
    }

    @Override
    public MessageType[] getMsgTypes() {
        return new MessageType[]{
                new MessageType(UPDATE)
        };
    }

    @Override
    public void invoke(WidgetMessage message) {
        super.invoke(message);
        String type = message.type.type;
        if (UPDATE.equals(type)) {
            updateLocalCourseList(message.data.getInt(Const.LESSON_ID));
        }
    }

    private void updateLocalCourseList(int lessonId) {
        M3U8DbModel m3u8Model = M3U8Util.queryM3U8Model(
                mContext, app.loginUser.id, lessonId, app.domain, M3U8Util.ALL);
        if (m3u8Model != null && m3u8Model.finish == M3U8Util.FINISH) {
            if (mActivityContainer != null) {
                DownloadManagerActivity.LocalCourseModel model = mActivityContainer.getLocalCourseList(M3U8Util.UN_FINISH, null, null);
                mDownloadingAdapter.updateLocalData(model.mLocalLessons.get(mCourseId));
                setEmptyState(mDownloadingAdapter.getCount() == 0);
                Bundle bundle = new Bundle();
                bundle.putInt(Const.LESSON_ID, lessonId);
                app.sendMessage(DownloadedFragment.FINISH, bundle);
            }
        } else {
            mDownloadingAdapter.updateProgress(lessonId, m3u8Model);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.offline_menu_edit, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.offline_menu_edit) {
            if (mDownloadingAdapter.isSelectedShow()) {
                hideBtnLayout();
                item.setTitle("编辑");
                mDownloadingAdapter.setSelectShow(false);
            } else {
                showBtnLayout();
                item.setTitle("取消");
                mDownloadingAdapter.setSelectShow(true);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void showBtnLayout() {
        mToolsLayout.setVisibility(View.VISIBLE);
    }

    private void hideBtnLayout() {
        mToolsLayout.setVisibility(View.GONE);
    }
}
