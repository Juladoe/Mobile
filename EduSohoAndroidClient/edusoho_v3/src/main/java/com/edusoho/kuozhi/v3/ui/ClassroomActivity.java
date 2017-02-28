package com.edusoho.kuozhi.v3.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.View;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.core.CoreEngine;
import com.edusoho.kuozhi.v3.entity.course.ClassroomDetail;
import com.edusoho.kuozhi.v3.entity.lesson.LessonItem;
import com.edusoho.kuozhi.v3.handler.CourseStateCallback;
import com.edusoho.kuozhi.v3.listener.PluginRunCallback;
import com.edusoho.kuozhi.v3.listener.ResponseCallbackListener;
import com.edusoho.kuozhi.v3.model.bal.Classroom;
import com.edusoho.kuozhi.v3.model.bal.Member;
import com.edusoho.kuozhi.v3.model.bal.Teacher;
import com.edusoho.kuozhi.v3.model.bal.course.CourseDetailModel;
import com.edusoho.kuozhi.v3.plugin.ShareTool;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.ClassroomUtil;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.CourseUtil;
import com.edusoho.kuozhi.v3.util.sql.SqliteUtil;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;


/**
 * Created by Zhang on 2016/12/8.
 */
public class ClassroomActivity extends BaseStudyDetailActivity implements View.OnClickListener, CourseStateCallback {
    private int mClassroomId;
    public ClassroomDetail mClassroomDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mClassroomId = intent.getIntExtra(Const.CLASSROOM_ID, 0);
        if (mClassroomId == 0) {
            finish();
            return;
        }
        mMediaViewHeight = AppUtil.px2dp(this, (float) AppUtil.getWidthPx(this) / 4f * 3f);
        initView();
        initData();
    }

    @Override
    protected void initView() {
        getIntent().getExtras().putString("source", mClassroomDetail != null ? mClassroomDetail.getClassRoom().title : null);
        super.initView();
        mCollect.setVisibility(View.GONE);
        mPlayButtonLayout.setVisibility(View.GONE);
        mPlayLayout2.setVisibility(View.GONE);
        mTvAdd.setText(R.string.txt_add_class);
        mIvGrade.setVisibility(View.GONE);
    }

    protected String[] getFragmentArray() {
        return new String[]{
                "ClassroomDetailFragment", "ClassCatalogFragment", "CourseDiscussFragment"
        };
    }

    @Override
    public boolean isExpired() {
        return false;
    }

    @Override
    public void handlerCourseExpired() {
    }

    private void saveClassRoomToCache(Classroom classroom) {
        SqliteUtil sqliteUtil = SqliteUtil.getUtil(getBaseContext());
        sqliteUtil.saveLocalCache(Const.CACHE_COURSE_TYPE, "classroom-" + classroom.id, new Gson().toJson(classroom));
    }

    protected void initData() {
        if (mClassroomId != 0) {
            CourseDetailModel.getClassroomDetail(mClassroomId,
                    new ResponseCallbackListener<ClassroomDetail>() {
                        @Override
                        public void onSuccess(ClassroomDetail data) {
                            mClassroomDetail = data;
                            if (mClassroomDetail.getMember() == null) {
                                refreshFragmentViews(false);
                            } else {
                                refreshFragmentViews(true);
                                tabPage(300);
                            }
                            setBottomLayoutState(mClassroomDetail.getMember() == null);
                            setLoadStatus(View.GONE);
                            refreshView();
                            if (data != null && data.getClassRoom() != null) {
                                saveClassRoomToCache(data.getClassRoom());
                            }
                        }

                        @Override
                        public void onFailure(String code, String message) {
                            if (message != null && message.equals("班级不存在")) {
                                CommonUtil.shortToast(ClassroomActivity.this, "班级不存在");
                                finish();
                            }
                        }
                    });
        }
    }

    @Override
    protected void grade() {
        ((EdusohoApp) getApplication()).mEngine.runNormalPlugin("ReviewActivity", this, new PluginRunCallback() {
            @Override
            public void setIntentDate(Intent startIntent) {
                startIntent.putExtra(ReviewActivity.TYPE, ReviewActivity.TYPE_CLASSROOM);
                startIntent.putExtra(ReviewActivity.ID, mClassroomId);
            }
        });
    }

    @Override
    protected void refreshView() {
        DisplayImageOptions imageOptions = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.default_classroom)
                .showImageOnFail(R.drawable.default_classroom)
                .showImageOnLoading(R.drawable.default_classroom)
                .build();
        ImageLoader.getInstance().displayImage(
                mClassroomDetail.getClassRoom().getLargePicture(),
                mIvBackGraound, imageOptions);
        Member member = mClassroomDetail.getMember();
        if (member == null) {
            mIsMemder = false;
            mAddLayout.setVisibility(View.VISIBLE);
            mIvGrade.setVisibility(View.GONE);
            mTvInclass.setVisibility(View.GONE);
            initViewPager();
        } else {
            mIsMemder = true;
            mAddLayout.setVisibility(View.GONE);
            mTvInclass.setVisibility(View.VISIBLE);
            initViewPager();
        }
        if (((EdusohoApp) getApplication()).loginUser != null && ((EdusohoApp) getApplication()).loginUser.vip != null &&
                ((EdusohoApp) getApplication()).loginUser.vip.levelId >= mClassroomDetail.getClassRoom().vipLevelId
                && mClassroomDetail.getClassRoom().vipLevelId != 0) {
            mTvAdd.setText(R.string.txt_vip_free);
        } else {
            mTvAdd.setText(R.string.txt_add_class);
        }
    }

    @Override
    protected void goClass() {
        CoreEngine.create(this).runNormalPlugin("ClassroomDiscussActivity", this, new PluginRunCallback() {
            @Override
            public void setIntentDate(Intent startIntent) {
                startIntent.putExtra(ClassroomDiscussActivity.FROM_ID, mClassroomId);
                startIntent.putExtra(ClassroomDiscussActivity.FROM_NAME, mClassroomDetail.getClassRoom().title);
            }
        });
    }

    @Override
    protected void consult() {
        if (((EdusohoApp) getApplication()).loginUser == null) {
            CourseUtil.notLogin();
            return;
        }
        CourseDetailModel.getTeacher(mClassroomId, new ResponseCallbackListener<Teacher[]>() {
            @Override
            public void onSuccess(Teacher[] data) {
                if (data.length == 0) {
                    CommonUtil.shortToast(ClassroomActivity.this, "班级目前没有老师");
                } else {
                    startImChat(data[0]);
                }
            }

            @Override
            public void onFailure(String code, String message) {
                CommonUtil.shortToast(ClassroomActivity.this, "获取信息失败");
            }
        });
    }

    private void startImChat(final Teacher teacher) {
        CoreEngine.create(this).runNormalPlugin("ImChatActivity", this, new PluginRunCallback() {
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
        if (mClassroomId != 0) {
            if (!"1".equals(mClassroomDetail.getClassRoom().buyable)) {
                CommonUtil.shortToast(ClassroomActivity.this, getResources()
                        .getString(R.string.add_error_close));
                return;
            }
            showProcessDialog();
            if (((EdusohoApp) getApplication()).loginUser != null && ((EdusohoApp) getApplication()).loginUser.vip != null
                    && ((EdusohoApp) getApplication()).loginUser.vip.levelId >= mClassroomDetail.getClassRoom().vipLevelId
                    && mClassroomDetail.getClassRoom().vipLevelId != 0) {
                ClassroomUtil.addClassroomVip(mClassroomId, new ClassroomUtil.OnAddClassroomListener() {
                    @Override
                    public void onAddClassroomSuccee(String response) {
                        hideProcesDialog();
                        CommonUtil.shortToast(ClassroomActivity.this, getResources()
                                .getString(R.string.success_add_classroom));
                        initData();
                    }

                    @Override
                    public void onAddClassroomError(String response) {
                        hideProcesDialog();
                    }
                });
                return;
            }
            ClassroomUtil.addClassroom(new ClassroomUtil.ClassroomParamsBuilder()
                            .setCouponCode("")
                            .setPayment("")
                            .setPayPassword("")
                            .setTargetId(String.valueOf(mClassroomDetail.getClassRoom().id))
                            .setTargetType("Classroom")
                            .setTotalPrice(String.valueOf(mClassroomDetail.getClassRoom().price))
                    , new ClassroomUtil.OnAddClassroomListener() {
                        @Override
                        public void onAddClassroomSuccee(String response) {
                            hideProcesDialog();
                            CommonUtil.shortToast(ClassroomActivity.this, getResources()
                                    .getString(R.string.success_add_classroom));
                            initData();
                        }

                        @Override
                        public void onAddClassroomError(String error) {
                            hideProcesDialog();
                        }
                    });
            mIsJump = true;
        }
    }

    @Override
    protected void share() {
        if (mClassroomDetail == null) {
            return;
        }
        Classroom classroom = mClassroomDetail.getClassRoom();
        final ShareTool shareTool =
                new ShareTool(this
                        , ((EdusohoApp) getApplication()).host + "/classroom/" + classroom.id
                        , classroom.title
                        , classroom.about.length() > 20 ? classroom.about.substring(0, 20) : classroom.about
                        , classroom.largePicture);
        new Handler((this.getMainLooper())).post(new Runnable() {
            @Override
            public void run() {
                shareTool.shardCourse();
            }
        });
    }

    @Override
    protected void courseStart() {
        super.courseStart();
    }

    @Override
    protected void courseChange(LessonItem lessonItem) {
    }

    @Override
    protected void showThreadCreateView(String type) {
        Bundle bundle = new Bundle();
        bundle.putInt(ThreadCreateActivity.TARGET_ID, mClassroomId);
        bundle.putString(ThreadCreateActivity.TARGET_TYPE, "classroom");
        bundle.putString(ThreadCreateActivity.TYPE, "question".equals(type) ? "question" : "discussion");
        bundle.putString(ThreadCreateActivity.THREAD_TYPE, "common");
        ((EdusohoApp) getApplication()).mEngine.runNormalPluginWithBundle("ThreadCreateActivity", this, bundle);
    }
}
