package com.edusoho.kuozhi.v3.ui.fragment.video;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;

import com.android.volley.VolleyError;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.core.MessageEngine;
import com.edusoho.kuozhi.v3.entity.lesson.LessonItem;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.listener.ResponseCallbackListener;
import com.edusoho.kuozhi.v3.model.bal.course.CourseDetailModel;
import com.edusoho.kuozhi.v3.model.provider.LessonProvider;
import com.edusoho.kuozhi.v3.ui.BaseStudyDetailActivity;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.helper.LessonMenuHelper;
import com.edusoho.kuozhi.v3.util.sql.SqliteUtil;
import com.edusoho.videoplayer.ui.VideoPlayerFragment;
import com.google.gson.reflect.TypeToken;

import org.videolan.libvlc.MediaPlayer;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by suju on 16/12/16.
 */

public class LessonVideoPlayerFragment extends VideoPlayerFragment implements View.OnFocusChangeListener {

    private int mLessonId;
    private int mCourseId;
    private int mPlayTime;
    private int mTotalTime;
    private long mSaveSeekTime;
    private boolean mIsContinue;
    private boolean mIsPlay;
    private Timer mTimer;
    private BaseStudyDetailActivity mMenuCallback;
    private LessonMenuHelper mLessonMenuHelper;
    private SharedPreferences mSeekPositionSetting;
    private static final String SEEK_POSITION = "seek_position";
    private String mRemainTime;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLessonId = getArguments().getInt(Const.LESSON_ID);
        mCourseId = getArguments().getInt(Const.COURSE_ID);
        mRemainTime = getArguments().getString(Const.REMAINT_TIME);
        mSeekPositionSetting = getContext().getSharedPreferences(SEEK_POSITION, Context.MODE_PRIVATE);
        mSaveSeekTime = mSeekPositionSetting.getLong(String.format("%d-%d", mCourseId, mLessonId), 0);

        setSeekPosition(mSaveSeekTime);
        if (mRemainTime != null) {
            startReturnData();
            startTiming();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof BaseStudyDetailActivity) {
            mMenuCallback = (BaseStudyDetailActivity) activity;
        }
    }

    @Override
    protected void requestMediaUri() {
        loadPlayUrl();
    }

    private void loadPlayUrl() {
        LessonItem cachedLesson = getCachedLesson();
        if (cachedLesson != null) {
            cachedLesson.mediaUri = String.format("http://%s:8800/playlist/%d.m3u8", "localhost", mLessonId);
            playVideo(cachedLesson.mediaUri);
            return;
        }
        new LessonProvider(getContext()).getLesson(mLessonId)
                .success(new NormalCallback<LessonItem>() {
                    @Override
                    public void success(LessonItem lessonItem) {
                        changeHeaderViewStatus(false);
                        if (lessonItem == null || TextUtils.isEmpty(lessonItem.mediaUri)) {
                            CommonUtil.shortToast(getContext(), "媒体资源不存在");
                            ((ViewGroup)getView()).removeAllViews();
                            return;
                        }
                        Uri mediaUri = Uri.parse(lessonItem.mediaUri);
                        playVideo(String.format("%s://%s%s", mediaUri.getScheme(), mediaUri.getHost(), mediaUri.getPath()));
                    }
                }).fail(new NormalCallback<VolleyError>() {
            @Override
            public void success(VolleyError obj) {
            }
        });
    }

    private LessonItem getCachedLesson() {
        SqliteUtil sqliteUtil = SqliteUtil.getUtil(getContext());
        return sqliteUtil.queryForObj(
                new TypeToken<LessonItem>(){},
                "where type=? and key=?",
                Const.CACHE_LESSON_TYPE,
                "lesson-" + mLessonId
        );
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            play();
        } else {
            pause();
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mMenuCallback != null && mMenuCallback.getMenu() != null) {
            mLessonMenuHelper = new LessonMenuHelper(getContext(), mLessonId, mCourseId);
            mLessonMenuHelper.initMenu(mMenuCallback.getMenu());
        }
        loadPlayUrl();
    }

    @Override
    protected void changeScreenLayout(final int orientation) {
        if (orientation == getResources().getConfiguration().orientation) {
            return;
        }
        View playView = getView();
        ViewParent viewParent = playView.getParent();
        if (viewParent == null) {
            return;
        }
        ViewGroup parent = (ViewGroup) viewParent.getParent();
        MessageEngine.getInstance().sendMsg(Const.FULL_SCREEN, null);

        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        ViewGroup.LayoutParams lp = parent.getLayoutParams();
        lp.height = orientation == Configuration.ORIENTATION_LANDSCAPE ?
                wm.getDefaultDisplay().getHeight() : getContext().getResources().getDimensionPixelOffset(com.edusoho.videoplayer.R.dimen.video_height);
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        parent.setLayoutParams(lp);
    }

    @Override
    protected void changeHeaderViewStatus(boolean isShow) {
        String changeBarEvent = isShow ?
                Const.COURSE_SHOW_BAR : Const.COURSE_HIDE_BAR;
        MessageEngine.getInstance().sendMsg(changeBarEvent, null);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMenuCallback != null && mMenuCallback.getMenu() != null) {
            mMenuCallback.getMenu().dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMenuCallback != null && mMenuCallback.getMenu() != null) {
            mMenuCallback.getMenu().setVisibility(false);
        }
        if (mTimer != null) {
            if (mIsContinue) {
                CourseDetailModel.sendTime(mLessonId, mPlayTime, null);
            }
            mTimer.cancel();
            mIsContinue = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mLessonMenuHelper != null) {
            mLessonMenuHelper.updatePluginItemState();
        }
    }

    @Override
    protected void savePosition(long seekTime) {
        super.savePosition(seekTime);
        SharedPreferences.Editor editor = mSeekPositionSetting.edit();
        editor.putLong(String.format("%d-%d", mCourseId, mLessonId), seekTime);
        editor.commit();
    }

    @Override
    public void onMediaPlayerEvent(MediaPlayer.Event event) {
        super.onMediaPlayerEvent(event);
        if (event.type == MediaPlayer.Event.Playing) {
            mIsPlay = true;
        } else if (event.type == MediaPlayer.Event.Stopped){
            mIsPlay = false;
        }
    }

    @Override
    public void play() {
        if (mRemainTime != null && mTotalTime >= Integer.parseInt(mRemainTime) && mMenuCallback != null) {
            CommonUtil.shortCenterToast(mMenuCallback, getResources().getString(R.string.lesson_had_reached_hint));
            return;
        }
        super.play();
    }

    private void startTiming() {
        mIsContinue = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mIsContinue) {
                    try {
                        Thread.sleep(1000);
                        if (mIsPlay) {
                            mPlayTime++;
                            mTotalTime++;
                            if (mTotalTime >= Integer.parseInt(mRemainTime)) {
                                mIsContinue = false;
                                mTimer.cancel();
                                if (mMenuCallback != null) {
                                    mMenuCallback.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mTimer.cancel();
                                            pause();
                                            CourseDetailModel.sendTime(mLessonId, mPlayTime, null);
                                            CommonUtil.shortCenterToast(mMenuCallback, getResources().getString(R.string.lesson_had_reached_hint));
                                        }
                                    });
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void startReturnData() {
        mTimer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                CourseDetailModel.sendTime(mLessonId, mPlayTime, new ResponseCallbackListener<String>() {
                    @Override
                    public void onSuccess(String data) {
                        mPlayTime = 0;
                    }
                    @Override
                    public void onFailure(String code, String message) {}
                });
            }
        };
        mTimer.schedule(timerTask, 120000, 120000);
    }
}
