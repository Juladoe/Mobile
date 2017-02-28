package com.edusoho.kuozhi.v3.ui.course;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.android.volley.VolleyError;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.core.CoreEngine;
import com.edusoho.kuozhi.v3.entity.course.CourseDetail;
import com.edusoho.kuozhi.v3.entity.lesson.LessonItem;
import com.edusoho.kuozhi.v3.factory.FactoryManager;
import com.edusoho.kuozhi.v3.factory.provider.AppSettingProvider;
import com.edusoho.kuozhi.v3.handler.CourseStateCallback;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.listener.PluginRunCallback;
import com.edusoho.kuozhi.v3.listener.ResponseCallbackListener;
import com.edusoho.kuozhi.v3.model.bal.Member;
import com.edusoho.kuozhi.v3.model.bal.Teacher;
import com.edusoho.kuozhi.v3.model.bal.User;
import com.edusoho.kuozhi.v3.model.bal.course.Course;
import com.edusoho.kuozhi.v3.model.bal.course.CourseDetailModel;
import com.edusoho.kuozhi.v3.model.bal.course.CourseMember;
import com.edusoho.kuozhi.v3.model.provider.CourseProvider;
import com.edusoho.kuozhi.v3.model.sys.School;
import com.edusoho.kuozhi.v3.plugin.ShareTool;
import com.edusoho.kuozhi.v3.ui.BaseStudyDetailActivity;
import com.edusoho.kuozhi.v3.ui.ImChatActivity;
import com.edusoho.kuozhi.v3.ui.LessonActivity;
import com.edusoho.kuozhi.v3.ui.NewsCourseActivity;
import com.edusoho.kuozhi.v3.ui.ReviewActivity;
import com.edusoho.kuozhi.v3.ui.ThreadCreateActivity;
import com.edusoho.kuozhi.v3.ui.fragment.CourseCatalogFragment;
import com.edusoho.kuozhi.v3.ui.fragment.CourseDetailFragment;
import com.edusoho.kuozhi.v3.ui.fragment.CourseDiscussFragment;
import com.edusoho.kuozhi.v3.ui.fragment.lesson.LessonAudioPlayerFragment;
import com.edusoho.kuozhi.v3.ui.fragment.video.LessonVideoPlayerFragment;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.CourseUtil;
import com.edusoho.kuozhi.v3.util.server.CacheServerFactory;
import com.edusoho.kuozhi.v3.util.sql.SqliteUtil;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by suju on 17/2/7.
 */
public class CourseStudyDetailActivity extends BaseStudyDetailActivity implements CourseStateCallback, BaseStudyDetailActivity.WidgtState {
    public CourseDetail mCourseDetail;
    private int mCourseId;
    private boolean mIsFavorite = false;
    private LessonItem mContinueLessonItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCourseId = getIntent().getIntExtra(Const.COURSE_ID, 0);
        initView();
        initData();
    }

    @Override
    protected void initView() {
        super.initView();
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mTabLayout.setViewPager(mViewPager);
        mPlayLayout2.setVisibility(View.GONE);
        mTvAdd.setText(R.string.txt_add_course);
    }

    @Override
    protected void initData() {
        if (mCourseId == 0) {
            CommonUtil.shortToast(getBaseContext(), getResources().getString(R.string.lesson_unexit));
            finish();
            return;
        }
        CourseDetailModel.getCourseDetail(mCourseId,
                new ResponseCallbackListener<CourseDetail>() {
                    @Override
                    public void onSuccess(CourseDetail data) {
                        mCourseDetail = data;
                        mIsClassroomCourse = data.getCourse().parentId != 0;
                        if (mCourseDetail.getMember() == null) {
                            refreshFragmentViews(false);
                            setLoadStatus(View.GONE);
                        } else {
                            refreshFragmentViews(true);
                            tabPage(300);
                        }
                        setBottomLayoutState(mCourseDetail.getMember() == null);
                        mTitle = mCourseDetail.getCourse().title;
                        refreshView();
                        if (data != null && data.getCourse() != null) {
                            saveCourseToCache(data.getCourse());
                        }
                    }

                    @Override
                    public void onFailure(String code, String message) {
                        if (message.contains("课程不存在") || message.contains("课程未发布")) {
                            CommonUtil.shortToast(CourseStudyDetailActivity.this, message);
                            finish();
                        }
                    }
                });
    }

    @Override
    protected void goClass() {
        CoreEngine.create(getBaseContext()).runNormalPlugin("NewsCourseActivity", ((EdusohoApp) getApplication()).mContext, new PluginRunCallback() {
            @Override
            public void setIntentDate(Intent startIntent) {
                startIntent.putExtra(NewsCourseActivity.COURSE_ID, mCourseId);
                startIntent.putExtra(NewsCourseActivity.SHOW_TYPE, NewsCourseActivity.DISCUSS_TYPE);
                startIntent.putExtra(NewsCourseActivity.FROM_NAME, mCourseDetail.getCourse().title);
            }
        });
    }

    @Override
    protected void consult() {
        MobclickAgent.onEvent(this, "courseDetailsPage_consultation");
        if (((EdusohoApp) getApplication()).loginUser == null) {
            CourseUtil.notLogin();
            return;
        }
        Teacher[] teachers = mCourseDetail.getCourse().teachers;
        final Teacher teacher;
        if (teachers.length > 0) {
            teacher = teachers[0];
        } else {
            CommonUtil.shortToast(this, getResources().getString(R.string.lesson_no_teacher));
            return;
        }
        CoreEngine.create(getBaseContext()).runNormalPlugin("ImChatActivity", ((EdusohoApp) getApplication()).mContext, new PluginRunCallback() {
            @Override
            public void setIntentDate(Intent startIntent) {
                startIntent.putExtra(ImChatActivity.FROM_NAME, teacher.nickname);
                startIntent.putExtra(ImChatActivity.FROM_ID, teacher.id);
                startIntent.putExtra(ImChatActivity.HEAD_IMAGE_URL, teacher.avatar);
            }
        });
    }

    @Override
    protected void grade() {
        MobclickAgent.onEvent(this, "courseDetailsPage_evaluation");
        CoreEngine.create(getBaseContext()).runNormalPluginForResult("ReviewActivity", this, ReviewActivity.REVIEW_RESULT
                , new PluginRunCallback() {
                    @Override
                    public void setIntentDate(Intent startIntent) {
                        startIntent.putExtra(ReviewActivity.TYPE, ReviewActivity.TYPE_COURSE);
                        startIntent.putExtra(ReviewActivity.ID, mCourseId);
                    }
                });
    }

    @Override
    protected void add() {
        MobclickAgent.onEvent(this, "courseDetailsPage_joinTheCourse");
        if (mCourseId != 0) {
            if (!"1".equals(mCourseDetail.getCourse().buyable)) {
                CommonUtil.shortToast(CourseStudyDetailActivity.this, getResources()
                        .getString(R.string.add_error_close));
                return;
            }
            showProcessDialog();
            if (((EdusohoApp) getApplication()).loginUser != null && ((EdusohoApp) getApplication()).loginUser.vip != null
                    && ((EdusohoApp) getApplication()).loginUser.vip.levelId >= mCourseDetail.getCourse().vipLevelId
                    && mCourseDetail.getCourse().vipLevelId != 0) {
                CourseUtil.addCourseVip(mCourseId, new CourseUtil.OnAddCourseListener() {
                    @Override
                    public void onAddCourseSuccess(String response) {
                        hideProcesDialog();
                        CommonUtil.shortToast(CourseStudyDetailActivity.this, getResources()
                                .getString(R.string.success_add_course));
                        initData();
                    }

                    @Override
                    public void onAddCourseError(String response) {
                        hideProcesDialog();
                    }
                });
                return;
            }
            CourseUtil.addCourse(new CourseUtil.CourseParamsBuilder()
                            .setCouponCode("")
                            .setPayment("")
                            .setPayPassword("")
                            .setTargetId(String.valueOf(mCourseDetail.getCourse().id))
                            .setTargetType("course")
                            .setTotalPrice(String.valueOf(mCourseDetail.getCourse().price))
                    , new CourseUtil.OnAddCourseListener() {
                        @Override
                        public void onAddCourseSuccess(String response) {
                            hideProcesDialog();
                            CommonUtil.shortToast(CourseStudyDetailActivity.this, getResources()
                                    .getString(R.string.success_add_course));
                            initData();
                        }

                        @Override
                        public void onAddCourseError(String error) {
                            hideProcesDialog();
                        }
                    });
            mIsJump = true;
        }
    }

    @Override
    protected void collect() {
        MobclickAgent.onEvent(this, "courseDetailsPage_collection");
        if (mIsFavorite) {
            CourseUtil.uncollectCourse(mCourseId, new CourseUtil.OnCollectSuccessListener() {
                @Override
                public void onCollectSuccess() {
                    mIsFavorite = false;
                    mTvCollect.setText(getResources().getString(R.string.new_font_collect));
                    mTvCollect.setTextColor(getResources().getColor(R.color.secondary_font_color));
                    mTvCollectTxt.setTextColor(getResources().getColor(R.color.secondary_font_color));
                }
            });
        } else {
            CourseUtil.collectCourse(mCourseId, new CourseUtil.OnCollectSuccessListener() {
                @Override
                public void onCollectSuccess() {
                    mIsFavorite = true;
                    mTvCollect.setText(getResources().getString(R.string.new_font_collected));
                    mTvCollect.setTextColor(getResources().getColor(R.color.primary_color));
                    mTvCollectTxt.setTextColor(getResources().getColor(R.color.primary_color));
                }
            });
        }
    }

    @Override
    protected void share() {
        MobclickAgent.onEvent(this, "courseDetailsPage_share");
        if (mCourseDetail == null) {
            return;
        }
        Course course = mCourseDetail.getCourse();
        final ShareTool shareTool =
                new ShareTool(this
                        , ((EdusohoApp) getApplication()).host + "/course/" + course.id
                        , course.title
                        , course.about.length() > 20 ? course.about.substring(0, 20) : course.about
                        , course.middlePicture);
        new Handler((((EdusohoApp) getApplication()).mContext.getMainLooper())).post(new Runnable() {
            @Override
            public void run() {
                shareTool.shardCourse();
            }
        });
    }

    private void saveCourseToCache(Course course) {
        course.setSourceName(getIntent().getStringExtra(Const.SOURCE));
        SqliteUtil sqliteUtil = SqliteUtil.getUtil(getBaseContext());
        sqliteUtil.saveLocalCache(
                Const.CACHE_COURSE_TYPE,
                String.format("course-%d", course.id),
                new Gson().toJson(course)
        );
    }

    private void startCacheServer() {
        User user = getAppSettingProvider().getCurrentUser();
        School school = getAppSettingProvider().getCurrentSchool();
        if (user == null || school == null) {
            return;
        }
        CacheServerFactory.getInstance().start(getBaseContext(), school.host, user.id);
    }

    @Override
    protected void refreshView() {
        mIsFavorite = mCourseDetail.isUserFavorited();
        if (mIsFavorite) {
            mTvCollect.setText(getResources().getString(R.string.new_font_collected));
            mTvCollect.setTextColor(getResources().getColor(R.color.primary_color));
            mTvCollectTxt.setTextColor(getResources().getColor(R.color.primary_color));
        } else {
            mTvCollect.setText(getResources().getString(R.string.new_font_collect));
            mTvCollect.setTextColor(getResources().getColor(R.color.secondary_font_color));
            mTvCollectTxt.setTextColor(getResources().getColor(R.color.secondary_font_color));
        }
        DisplayImageOptions imageOptions = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.default_course)
                .showImageOnFail(R.drawable.default_course)
                .showImageOnLoading(R.drawable.default_course)
                .build();
        ImageLoader.getInstance().displayImage(
                mCourseDetail.getCourse().largePicture,
                mIvBackGraound, imageOptions);
        Member member = mCourseDetail.getMember();
        if (member == null) {
            mIsMemder = false;
            setBottomVisible(mIsClassroomCourse);
            mTvInclass.setVisibility(View.GONE);
            initViewPager();
            mIvGrade.setVisibility(View.GONE);
        } else {
            mIsMemder = true;
            mAddLayout.setVisibility(View.GONE);
            mBottomLayout.setVisibility(View.GONE);
            initViewPager();
            mIvGrade.setVisibility(View.VISIBLE);
        }
        if (((EdusohoApp) getApplication()).loginUser != null && ((EdusohoApp) getApplication()).loginUser.vip != null &&
                ((EdusohoApp) getApplication()).loginUser.vip.levelId >= mCourseDetail.getCourse().vipLevelId
                && mCourseDetail.getCourse().vipLevelId != 0) {
            mTvAdd.setText(R.string.txt_vip_free);
        } else {
            mTvAdd.setText(R.string.txt_add_course);
        }
    }

    private void setBottomVisible(boolean isClassroomCourse) {
        if (isClassroomCourse) {
            mBottomLayout.setVisibility(View.GONE);
        } else {
            mAddLayout.setVisibility(View.VISIBLE);
            mBottomLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        startCacheServer();
    }

    @Override
    public void finish() {
        super.finish();
        removePlayFragment();
        CacheServerFactory.getInstance().stop();
    }

    @Override
    protected void courseChange(final LessonItem lessonItem) {
        mContinueLessonItem = lessonItem;
        coursePause();
        courseStart();
    }

    @Override
    protected void coursePause() {
        super.coursePause();
        removePlayFragment();
    }

    @Override
    protected void courseStart() {
        if (mContinueLessonItem != null && "video".equals(mContinueLessonItem.type) && Integer.parseInt(mContinueLessonItem.remainTime) <= 0) {
            CommonUtil.shortCenterToast(getApplicationContext(), getResources().getString(R.string.lesson_had_reached_hint));
            return;
        }
        super.courseStart();
        String type = mContinueLessonItem.type;
        if ("self".equals(mContinueLessonItem.mediaSource)) {
            if (mCourseDetail != null && validCourseIsExpird(mCourseDetail.getMember())) {
                showCourseExpireDlg();
                return;
            }
            switch (type) {
                case "audio":
                    playAudioLesson(mContinueLessonItem);
                    return;
                case "video":
                    playVideoLesson(mContinueLessonItem);
                    return;
            }
        }

        Fragment fragment = mSectionsPagerAdapter.getItem(1);
        if (fragment != null && fragment instanceof CourseCatalogFragment) {
            if (mContinueLessonItem == null) {
                return;
            }
            int memberState = CourseMember.NONE;
            if (mCourseDetail != null && mCourseDetail.getMember() != null) {
                memberState = CourseMember.MEMBER;
                if (mCourseDetail.getMember().deadline <= 0) {
                    memberState = CourseMember.EXPIRE;
                }
            }

            ((CourseCatalogFragment) fragment).startLessonActivity(
                    mContinueLessonItem.type,
                    mContinueLessonItem.id,
                    mContinueLessonItem.courseId,
                    memberState);
        }
    }

    private void playVideoLesson(LessonItem lessonItem) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        LessonVideoPlayerFragment fragment = new LessonVideoPlayerFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Const.COURSE_ID, mCourseId);
        bundle.putInt(Const.LESSON_ID, lessonItem.id);
        bundle.putString(Const.REMAINT_TIME, lessonItem.remainTime);
        fragment.setArguments(bundle);
        transaction.replace(R.id.fl_header_container, fragment);
        transaction.commitAllowingStateLoss();
    }

    private void playAudioLesson(LessonItem lessonItem) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        LessonAudioPlayerFragment fragment = new LessonAudioPlayerFragment();
        Bundle bundle = new Bundle();
        bundle.putString(LessonAudioPlayerFragment.COVER, mCourseDetail.getCourse().largePicture);
        bundle.putInt(Const.COURSE_ID, mCourseId);
        bundle.putInt(Const.LESSON_ID, lessonItem.id);
        fragment.setArguments(bundle);
        transaction.replace(R.id.fl_header_container, fragment);
        transaction.commitAllowingStateLoss();
    }


    @Override
    protected void courseHastrial(String state, LessonItem lessonItem) {
        mContinueLessonItem = lessonItem;
        mPlayLastLayout.setVisibility(View.GONE);
        mPlayButtonLayout.setVisibility(View.VISIBLE);
        if (mCourseDetail != null && mCourseDetail.getMember() != null) {
            if ("2".equals(state)) {
                mTvPlay.setText(R.string.txt_study_finish);
                mTvPlay2.setText(R.string.txt_study_finish);
                mPlayLayout.setBackgroundResource(R.drawable.shape_play_background);
                mPlayLayout.setEnabled(false);
                return;
            }
        }
        switch (state) {
            case Const.COURSE_CHANGE_STATE_NONE:
                mPlayLayout.setEnabled(true);
                if (mCourseDetail == null || mCourseDetail.getMember() == null) {
                    mTvPlay.setText(R.string.txt_study_try);
                    mTvPlay2.setText(R.string.txt_study_try);
                    mPlayLayout.setBackgroundResource(R.drawable.shape_play_background2);
                } else {
                    mTvPlay.setText(R.string.txt_study_start);
                    mTvPlay2.setText(R.string.txt_study_start);
                    mPlayLayout.setBackgroundResource(R.drawable.shape_play_background);
                }
                break;
            case Const.COURSE_CHANGE_STATE_STARTED:
                mTvPlay.setText(R.string.txt_study_continue);
                mTvPlay2.setText(R.string.txt_study_continue);
                mPlayLayout.setBackgroundResource(R.drawable.shape_play_background);
                mPlayLayout.setEnabled(true);
                mPlayLastLayout.setVisibility(View.VISIBLE);
                mTvLast.setText(String.valueOf(lessonItem == null ? null : lessonItem.title));
                break;
        }
    }

    private void removePlayFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fl_header_container);
        if (fragment == null) {
            return;
        }
        if (fragment instanceof LessonAudioPlayerFragment) {
            ((LessonAudioPlayerFragment) fragment).destoryService();
        }

        transaction.remove(fragment).commitAllowingStateLoss();
    }

    protected String[] getFragmentArray() {
        return new String[]{
                "CourseDetailFragment", "CourseCatalogFragment", "CourseDiscussFragment"
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LessonActivity.REQUEST_LEARN) {
            coursePause();
        }
        if (requestCode == ReviewActivity.REVIEW_RESULT) {
            Fragment fragment = mSectionsPagerAdapter.getItem(0);
            if (fragment != null && fragment instanceof CourseDetailFragment) {
                ((CourseDetailFragment) fragment).refreshReview();
            }
        }
    }

    private void showCourseExpireDlg() {
        AlertDialog.Builder builder = new AlertDialog.Builder(((EdusohoApp) getApplication()).mContext);
        builder.setTitle(R.string.lesson_join_hint)
                .setMessage(R.string.lesson_has_past_hint)
                .setPositiveButton(R.string.lesson_join_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        unLearnCourse();
                    }
                })
                .setNegativeButton(R.string.lesson_join_cancel, null)
                .create()
                .show();
    }

    private void unLearnCourse() {
        new CourseProvider(getBaseContext()).unLearn(mCourseId)
                .success(new NormalCallback<String>() {
                    @Override
                    public void success(String response) {
                        if (response.equals("true")) {
                            ((CourseCatalogFragment) mSectionsPagerAdapter.getItem(1)).reFreshView(false);
                            ((CourseDiscussFragment) mSectionsPagerAdapter.getItem(2)).reFreshView(false);
                            mCourseDetail.setMember(null);
                            mIsMemder = false;
                            mAddLayout.setVisibility(View.VISIBLE);
                            mTvInclass.setVisibility(View.GONE);
                            initViewPager();
                            mViewPager.setCurrentItem(0);
                            ((CourseCatalogFragment) mSectionsPagerAdapter.getItem(1)).reFreshView(false);
                            ((CourseDiscussFragment) mSectionsPagerAdapter.getItem(2)).reFreshView(false);
                        } else {
                            CommonUtil.shortToast(((EdusohoApp) getApplication()).mContext, getResources().getString(R.string.lesson_quit_fail));
                        }
                    }
                }).fail(new NormalCallback<VolleyError>() {
            @Override
            public void success(VolleyError obj) {
                CommonUtil.shortToast(getBaseContext(), getResources().getString(R.string.lesson_quit_fail));
            }
        });
    }

    @Override
    public boolean isExpired() {
        return mCourseDetail != null && validCourseIsExpird(mCourseDetail.getMember());
    }

    private boolean validCourseIsExpird(Member courseMember) {
        return courseMember != null && courseMember.deadline < 0;
    }

    @Override
    protected void showThreadCreateView(String type) {
        if (mCourseDetail != null && validCourseIsExpird(mCourseDetail.getMember())) {
            showCourseExpireDlg();
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putInt(ThreadCreateActivity.TARGET_ID, mCourseId);
        bundle.putString(ThreadCreateActivity.TARGET_TYPE, "");
        bundle.putString(ThreadCreateActivity.TYPE, "question".equals(type) ? "question" : "discussion");
        bundle.putString(ThreadCreateActivity.THREAD_TYPE, "course");
        ((EdusohoApp) getApplication()).mEngine.runNormalPluginWithBundle("ThreadCreateActivity", this, bundle);
    }

    @Override
    public void handlerCourseExpired() {
    }

    protected AppSettingProvider getAppSettingProvider() {
        return FactoryManager.getInstance().create(AppSettingProvider.class);
    }

    @Override
    public void setTopViewVisibility(boolean isTop) {
        if (isTop) {
            mIvGrade.setVisibility(View.GONE);
            mPlayLayout2.setVisibility(View.VISIBLE);
        } else {
            mIvGrade.setVisibility(mIsPlay ? View.GONE : View.VISIBLE);
            mPlayLayout2.setVisibility(View.GONE);
        }
    }
}
