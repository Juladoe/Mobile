package com.edusoho.kuozhi.v3.util.helper;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.core.CoreEngine;
import com.edusoho.kuozhi.v3.core.MessageEngine;
import com.edusoho.kuozhi.v3.entity.lesson.LessonStatus;
import com.edusoho.kuozhi.v3.entity.lesson.PluginViewItem;
import com.edusoho.kuozhi.v3.listener.LessonPluginCallback;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.model.bal.LearnStatus;
import com.edusoho.kuozhi.v3.model.provider.LessonProvider;
import com.edusoho.kuozhi.v3.ui.MenuPop;
import com.edusoho.kuozhi.v3.util.Const;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by suju on 16/12/21.
 */

public class LessonMenuHelper {

    private int mLessonId;
    private int mCourseId;
    private LearnStatus mCurrentLearnState;
    private List<LessonPluginViewItem> mExerciseItemList;
    private Context mContext;
    private MenuPop mMenuPop;

    public LessonMenuHelper(Context context, int lessonId, int courseId) {
        this.mContext = context;
        this.mLessonId = lessonId;
        this.mCourseId = courseId;
    }

    public MenuPop getMenuPop() {
        return mMenuPop;
    }

    public void initMenu(MenuPop menuPop) {
        if(menuPop == null){
            return;
        }
        this.mMenuPop = menuPop;
        mMenuPop.removeAll();
        mMenuPop.addItem("记笔记");
        mExerciseItemList = getExerciseItemList();
        if (mExerciseItemList != null) {
            for (int i = 0; i < mExerciseItemList.size(); i++) {
                LessonPluginViewItem pluginViewItem = mExerciseItemList.get(i);
                pluginViewItem.setPosition(i + 1);
                mMenuPop.addItem(pluginViewItem.title);
            }
        }
        mMenuPop.addItem("学完");
        mMenuPop.setVisibility(true);
        mMenuPop.setOnMenuClickListener(getMenuClickListener());
        mMenuPop.setMenuShowListener(getMenuShowListener());
        loadLessonStatus();
    }

    public void show(View view, int x, int y) {
        mMenuPop.showAsDropDown(view, x, y);
    }

    public void updatePluginItemState() {
        if (mExerciseItemList == null || mExerciseItemList.isEmpty()) {
            return;
        }
        boolean hasNotice = false;
        for (PluginViewItem item : mExerciseItemList) {
            item.callback.initState(item);
            if (item.status == PluginViewItem.NEW) {
                hasNotice = true;
            }
        }

        mMenuPop.setNotice(hasNotice);
    }

    /**
     * 获取课时是否已学状态
     */
    private void loadLessonStatus() {
        new LessonProvider(mContext).getLearnState(mLessonId, mCourseId)
                .success(new NormalCallback<LessonStatus>() {
                    @Override
                    public void success(LessonStatus state) {
                        if (state == null) {
                            return;
                        }
                        setLearnBtnState(state.learnStatus);
                    }
                });
    }

    private MenuPop.IMenuShowListener getMenuShowListener() {
        return new MenuPop.IMenuShowListener() {
            @Override
            public void onShow(boolean isShow) {
                updatePluginItemState();
            }
        };
    }

    private MenuPop.OnMenuClickListener getMenuClickListener() {
        return new MenuPop.OnMenuClickListener() {
            @Override
            public void onClick(View v, int position, String name) {
                handlerMenuClick(v, position);
                mMenuPop.dismiss();
            }
        };
    }

    protected void handlerMenuClick(View v, int position) {
        switch (position) {
            case 0:
                MobclickAgent.onEvent(mContext, "timeToLearn_topThreePoints_takeNotes");
                startNodeActivity();
                break;
            case 1:
                MobclickAgent.onEvent(mContext, "hoursOfStudy_topOfTheThree_operation");
                startExerciseOrHomeWorkActivity(v, 0);
                break;
            case 2:
                MobclickAgent.onEvent(mContext, "timeToLearn_topThreePoints_practice");
                startExerciseOrHomeWorkActivity(v, 1);
                break;
            case 3:
                MobclickAgent.onEvent(mContext, "timeToLearn_topThreePoints_finished");
                changeLessonLearnState(v);
        }
    }

    private synchronized void changeLessonLearnState(final View view) {
        if (mCurrentLearnState == LearnStatus.finished) {
            return;
        }
        view.setEnabled(false);
        new LessonProvider(mContext).startLearnLesson(mLessonId, mCourseId)
                .success(new NormalCallback<LearnStatus>() {
                    @Override
                    public void success(LearnStatus state) {
                        view.setEnabled(true);
                        if (state != null && LearnStatus.finished == state) {
                            MessageEngine.getInstance().sendMsg(Const.LESSON_STATUS_REFRESH, null);
                        }
                        setLearnBtnState(state);
                    }
                });
    }

    private void setLearnBtnState(LearnStatus state) {
        if (state != null && LearnStatus.finished == state) {
            mCurrentLearnState = state;
            MenuPop.Item item = mMenuPop.getItem(3);
            item.setName("已学完");
            item.setColor(mContext.getResources().getColor(R.color.primary_color));
        } else {
            mMenuPop.getItem(3).setName("学完");
        }
    }

    private void startExerciseOrHomeWorkActivity(View v, int index) {
        PluginViewItem item = mExerciseItemList.get(index);
        if (!item.callback.click(v)) {
            return;
        }
        Intent intent = new Intent();
        intent.putExtras(item.bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName(mContext.getPackageName(), item.action);
        mContext.startActivity(intent);
    }

    private void startNodeActivity() {
        Bundle bundle = new Bundle();
        bundle.putInt(Const.COURSE_ID, mCourseId);
        bundle.putInt(Const.LESSON_ID, mLessonId);
        CoreEngine.create(mContext).runNormalPluginWithBundle("NoteActivity", mContext, bundle);
    }

    private List<LessonPluginViewItem> getExerciseItemList() {
        List<LessonPluginViewItem> list = new ArrayList<>();

        Bundle bundle = new Bundle();
        bundle.putInt("lessonId", mLessonId);
        Intent intent = new Intent();
        intent.setPackage(mContext.getPackageName());
        intent.putExtra(Const.LESSON_ID, mLessonId);
        intent.setAction(Const.LESSON_PLUGIN);

        List<ResolveInfo> resolveInfos = mContext.getPackageManager().queryIntentActivities(
                intent, PackageManager.GET_ACTIVITIES);

        for (ResolveInfo resolveInfo : resolveInfos) {
            LessonPluginViewItem item = new LessonPluginViewItem();
            item.iconRes = mContext.getResources().getDrawable(resolveInfo.activityInfo.icon);
            item.title = resolveInfo.loadLabel(mContext.getPackageManager()).toString();
            item.bundle = intent.getExtras();
            item.action = resolveInfo.activityInfo.name;
            try {
                Class lessonPluginCallbackCls = Class.forName(resolveInfo.activityInfo.name + "$Callback");
                item.callback = (LessonPluginCallback) lessonPluginCallbackCls.getConstructor(Context.class).newInstance(mContext);
            } catch (Exception e) {
                e.printStackTrace();
            }
            item.callback.initPlugin(item);
            list.add(item);
        }

        return list;
    }

    class LessonPluginViewItem extends PluginViewItem {

        private int mPosition;

        public void setPosition(int position) {
            this.mPosition = position;
        }

        @Override
        public void setStatus(int status) {
            super.setStatus(status);
            mMenuPop.getItem(mPosition).setHasPoint(status == NEW);
            boolean hasNotice = false;
            for (MenuPop.Item item : mMenuPop.getItems()) {
                if (item.isHasPoint()) {
                    hasNotice = true;
                }
            }

            mMenuPop.setNotice(hasNotice);
        }
    }
}
