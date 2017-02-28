package com.edusoho.kuozhi.v3.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.android.volley.VolleyError;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.entity.course.CourseDetail;
import com.edusoho.kuozhi.v3.entity.lesson.LessonItem;
import com.edusoho.kuozhi.v3.handler.CourseStateCallback;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.listener.PluginFragmentCallback;
import com.edusoho.kuozhi.v3.listener.PluginRunCallback;
import com.edusoho.kuozhi.v3.listener.ResponseCallbackListener;
import com.edusoho.kuozhi.v3.model.bal.Member;
import com.edusoho.kuozhi.v3.model.bal.Teacher;
import com.edusoho.kuozhi.v3.model.bal.course.Course;
import com.edusoho.kuozhi.v3.model.bal.course.CourseDetailModel;
import com.edusoho.kuozhi.v3.model.bal.course.CourseMember;
import com.edusoho.kuozhi.v3.model.provider.CourseProvider;
import com.edusoho.kuozhi.v3.plugin.ShareTool;
import com.edusoho.kuozhi.v3.ui.fragment.CourseCatalogFragment;
import com.edusoho.kuozhi.v3.ui.fragment.CourseDetailFragment;
import com.edusoho.kuozhi.v3.ui.fragment.CourseDiscussFragment;
import com.edusoho.kuozhi.v3.ui.fragment.lesson.LessonAudioPlayerFragment;
import com.edusoho.kuozhi.v3.ui.fragment.video.LessonVideoPlayerFragment;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.CourseUtil;
import com.edusoho.kuozhi.v3.util.sql.SqliteUtil;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * Created by Zhang on 2016/12/8.
 */
public class CourseActivity extends DetailActivity implements View.OnClickListener, CourseStateCallback {

    public static final String SOURCE_ID = "source_id";
    public static final String IS_CHILD_COURSE = "child_course";
    private int mCourseId;
    private boolean mIsFavorite = false;
    public CourseDetail mCourseDetail;
    private LessonItem mContinueLessonItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mCourseId = intent.getIntExtra(Const.COURSE_ID, 0);
        if (mCourseId == 0) {
            finish();
            return;
        }
        mMediaViewHeight = 210;
        initView();
        initEvent();
        initData();

        app.startPlayCacheServer(this);
    }

    @Override
    protected void initView() {
        super.initView();
        mTvAdd.setText(R.string.txt_add_course);
    }


    protected void initFragment(List<Fragment> fragments) {
        Fragment detailfragment = app.mEngine.runPluginWithFragment("CourseDetailFragment", this, new PluginFragmentCallback() {
            @Override
            public void setArguments(Bundle bundle) {
                bundle.putInt("id", mCourseId);
            }
        });
        fragments.add(detailfragment);
        Fragment catafragment = app.mEngine.runPluginWithFragment("CourseCatalogFragment", this, new PluginFragmentCallback() {
            @Override
            public void setArguments(Bundle bundle) {
                bundle.putInt("id", mCourseId);
            }
        });
        fragments.add(catafragment);
        Fragment discussFrament = app.mEngine.runPluginWithFragment("CourseDiscussFragment", this, new PluginFragmentCallback() {
            @Override
            public void setArguments(Bundle bundle) {
                bundle.putInt("id", mCourseId);
            }
        });
        fragments.add(discussFrament);
    }

    @Override
    public boolean isExpired() {
        return mCourseDetail != null && validCourseIsExpird(mCourseDetail.getMember());
    }

    @Override
    public void handlerCourseExpired() {
        showCourseExpireDlg();
    }

    protected void initEvent() {
        super.initEvent();
    }

    private void showCourseExpireDlg() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("提醒")
                .setMessage("课程已过期，是否重新加入?")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        unLearnCourse();
                    }
                })
                .setNegativeButton("取消", null)
                .create()
                .show();
    }

    protected void initData() {
        if (mCourseId == 0) {
            CommonUtil.shortToast(getBaseContext(), "课程不存在");
            finish();
            return;
        }
        CourseDetailModel.getCourseDetail(mCourseId,
                new ResponseCallbackListener<CourseDetail>() {
                    @Override
                    public void onSuccess(CourseDetail data) {
                        mCourseDetail = data;
                        if (mFragments.size() >= 2 && mFragments.get(1) != null
                                && mFragments.get(1) instanceof CourseCatalogFragment) {
                            if (mCourseDetail.getMember() == null) {
                                ((CourseCatalogFragment) mFragments.get(1)).reFreshView(false);
                                ((CourseDiscussFragment) mFragments.get(2)).reFreshView(false);
                                setLoadStatus(View.GONE);
                            } else {
                                ((CourseCatalogFragment) mFragments.get(1)).reFreshView(true);
                                ((CourseDiscussFragment) mFragments.get(2)).reFreshView(true);
                                tabPage(300);
                            }
                        } else {
                            setLoadStatus(View.GONE);
                        }
                        mTitle = mCourseDetail.getCourse().title;
                        refreshView();
                        if (data != null && data.getCourse() != null) {
                            saveCourseToCache(data.getCourse());
                        }
                    }

                    @Override
                    public void onFailure(String code, String message) {
                        if ("课程不存在".equals(message)) {
                            CommonUtil.shortToast(CourseActivity.this, "课程不存在");
                            finish();
                        }
                    }
                });
    }

    private void unLearnCourse() {
        new CourseProvider(getBaseContext()).unLearn(mCourseId)
        .success(new NormalCallback<String>() {
            @Override
            public void success(String response) {
                if (response.equals("true")) {
                    ((CourseCatalogFragment) mFragments.get(1)).reFreshView(false);
                    ((CourseDiscussFragment) mFragments.get(2)).reFreshView(false);
                    mCourseDetail.setMember(null);
                    mIsMemder = false;
                    mAddLayout.setVisibility(View.VISIBLE);
                    mTvInclass.setVisibility(View.GONE);
                    initViewPager();
                    mContentVp.setCurrentItem(0);
                    ((CourseCatalogFragment) mFragments.get(1)).reFreshView(false);
                    ((CourseDiscussFragment) mFragments.get(2)).reFreshView(false);
                } else {
                    CommonUtil.shortToast(mContext, "退出失败");
                }
            }
        }).fail(new NormalCallback<VolleyError>() {
            @Override
            public void success(VolleyError obj) {
                CommonUtil.shortToast(mContext, "退出失败");
            }
        });
    }

    private boolean validCourseIsExpird(Member courseMember) {
        if (courseMember == null) {
            return false;
        }
        return courseMember.deadline < 0;
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
                mIvMediaBackground, imageOptions);
        Member member = mCourseDetail.getMember();
        if (member == null) {
            mIsMemder = false;
            if (getIntent().getBooleanExtra(CourseActivity.IS_CHILD_COURSE, false)) {
                mBottomLayout.setVisibility(View.GONE);
            } else {
                mAddLayout.setVisibility(View.VISIBLE);
                mBottomLayout.setVisibility(View.VISIBLE);
            }
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
        if (app.loginUser != null && app.loginUser.vip != null &&
                app.loginUser.vip.levelId >= mCourseDetail.getCourse().vipLevelId
                && mCourseDetail.getCourse().vipLevelId != 0) {
            mTvAdd.setText(R.string.txt_vip_free);
        } else {
            mTvAdd.setText(R.string.txt_add_course);
        }
    }

    @Override
    protected void goClass() {
        app.mEngine.runNormalPlugin("NewsCourseActivity", mContext, new PluginRunCallback() {
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
        if (app.loginUser == null) {
            CourseUtil.notLogin();
            return;
        }
        Teacher[] teachers = mCourseDetail.getCourse().teachers;
        final Teacher teacher;
        if (teachers.length > 0) {
            teacher = teachers[0];
        } else {
            CommonUtil.shortToast(this, "课程目前没有老师");
            return;
        }
        app.mEngine.runNormalPlugin("ImChatActivity", mContext, new PluginRunCallback() {
            @Override
            public void setIntentDate(Intent startIntent) {
                startIntent.putExtra(ImChatActivity.FROM_NAME, teacher.nickname);
                startIntent.putExtra(ImChatActivity.FROM_ID, teacher.id);
                startIntent.putExtra(ImChatActivity.HEAD_IMAGE_URL, teacher.avatar);
            }
        });
    }

    @Override
    protected void add() {
        if (mCourseId != 0) {
            if (!"1".equals(mCourseDetail.getCourse().buyable)) {
                CommonUtil.shortToast(CourseActivity.this, getResources()
                        .getString(R.string.add_error_close));
                return;
            }
            showProcessDialog();
            if (app.loginUser != null && app.loginUser.vip != null
                    && app.loginUser.vip.levelId >= mCourseDetail.getCourse().vipLevelId
                    && mCourseDetail.getCourse().vipLevelId != 0) {
                CourseUtil.addCourseVip(mCourseId, new CourseUtil.OnAddCourseListener() {
                    @Override
                    public void onAddCourseSuccess(String response) {
                        hideProcesDialog();
                        CommonUtil.shortToast(CourseActivity.this, getResources()
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
                            CommonUtil.shortToast(CourseActivity.this, getResources()
                                    .getString(R.string.success_add_course));
                            initData();
                        }

                        @Override
                        public void onAddCourseError(String error) {
                            hideProcesDialog();
                        }
                    });
//            mIvGrade2.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void collect() {
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
        if (mCourseDetail == null) {
            return;
        }
        final ShareTool shareTool =
                new ShareTool(this
                        , app.host + "/course/" + mCourseDetail.getCourse().id
                        , mCourseDetail.getCourse().title
                        , mCourseDetail.getCourse().about.length() > 20 ?
                        mCourseDetail.getCourse().about.substring(0, 20)
                        : mCourseDetail.getCourse().about
                        , mCourseDetail.getCourse().middlePicture);
        new Handler((mActivity.getMainLooper())).post(new Runnable() {
            @Override
            public void run() {
                shareTool.shardCourse();
            }
        });
    }

    @Override
    protected synchronized void courseChange(LessonItem lessonItem) {
        if (mIsPlay && mContinueLessonItem != null && mContinueLessonItem.id == lessonItem.id) {
            return;
        }
        mContinueLessonItem = lessonItem;
        coursePause();
        courseStart();
    }

    @Override
    protected void grade() {
        app.mEngine.runNormalPluginForResult("ReviewActivity", this, ReviewActivity.REVIEW_RESULT
                , new PluginRunCallback() {
                    @Override
                    public void setIntentDate(Intent startIntent) {
                        startIntent.putExtra(ReviewActivity.TYPE, ReviewActivity.TYPE_COURSE);
                        startIntent.putExtra(ReviewActivity.ID, mCourseId);
                    }
                });
    }

    @Override
    protected void courseHastrial(String state, LessonItem lessonItem) {
        mContinueLessonItem = lessonItem;
        mPlayLastLayout.setVisibility(View.GONE);
        mPlayButtonLayout.setVisibility(View.VISIBLE);
        if (mCourseDetail != null && mCourseDetail.getMember() != null) {
            if ("1".equals(mCourseDetail.getMember().isLearned)) {
                mTvPlay.setText(R.string.txt_study_finish);
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
                    mPlayLayout.setBackgroundResource(R.drawable.shape_play_background2);
                } else {
                    mTvPlay.setText(R.string.txt_study_start);
                    mPlayLayout.setBackgroundResource(R.drawable.shape_play_background);
                }
                break;
            case Const.COURSE_CHANGE_STATE_STARTED:
                mTvPlay.setText(R.string.txt_study_continue);
                mPlayLayout.setBackgroundResource(R.drawable.shape_play_background);
                mPlayLayout.setEnabled(true);
                mPlayLastLayout.setVisibility(View.VISIBLE);
                mTvLastTitle.setText(String.valueOf(lessonItem == null ? null : lessonItem.title));
                break;
        }
    }

    protected void onFragmentsFocusChange(View rootView, boolean hasFocus) {
        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
        for (int i = 0; i < fragmentList.size(); i++) {
            Fragment fragment = fragmentList.get(i);
            if (fragment instanceof View.OnFocusChangeListener) {
                ((View.OnFocusChangeListener) fragment).onFocusChange(null, hasFocus);
            }
        }
    }

    @Override
    protected void coursePause() {
        super.coursePause();
        removePlayFragment();
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

    @Override
    protected void courseStart() {
        if (mContinueLessonItem == null) {
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

        Fragment fragment = mFragments.get(1);
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
    public void finish() {
        super.finish();
        removePlayFragment();
        app.stopPlayCacheServer();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LessonActivity.REQUEST_LEARN) {
            coursePause();
        }
        if (requestCode == ReviewActivity.REVIEW_RESULT) {
            Fragment fragment = mFragments.get(0);
            if (fragment != null && fragment instanceof CourseDetailFragment) {
                ((CourseDetailFragment) fragment).refreshReview();
            }
        }
    }

    @Override
    public MenuPop getMenu() {
        if (mContinueLessonItem != null
                && mCourseDetail.getMember() == null
                && 1 == mContinueLessonItem.free) {
            return null;
        }
        return super.getMenu();
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
        app.mEngine.runNormalPluginWithBundle("ThreadCreateActivity", this, bundle);
    }
}