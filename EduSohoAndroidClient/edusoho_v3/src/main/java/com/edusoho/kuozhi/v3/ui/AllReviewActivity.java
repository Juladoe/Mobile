package com.edusoho.kuozhi.v3.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.adapter.ClassroomReviewAdapter;
import com.edusoho.kuozhi.v3.adapter.CourseReviewAdapter;
import com.edusoho.kuozhi.v3.listener.ResponseCallbackListener;
import com.edusoho.kuozhi.v3.model.bal.course.ClassroomReview;
import com.edusoho.kuozhi.v3.model.bal.course.ClassroomReviewDetail;
import com.edusoho.kuozhi.v3.model.bal.course.CourseDetailModel;
import com.edusoho.kuozhi.v3.model.bal.course.CourseReview;
import com.edusoho.kuozhi.v3.model.bal.course.CourseReviewDetail;
import com.edusoho.kuozhi.v3.ui.base.BaseNoTitleActivity;
import com.edusoho.kuozhi.v3.util.SystemBarTintManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by remilia on 2017/1/11.
 */
public class AllReviewActivity extends BaseNoTitleActivity {

    private ListView mLvContent;
    private List<CourseReview> mCourseReviews = new ArrayList<>();
    private List<ClassroomReview> mClassroomReviews = new ArrayList<>();
    private CourseReviewAdapter mCourseReviewAdapter;
    private ClassroomReviewAdapter mClassroomReviewAdapter;
    private int mType;
    public static final String TYPE = "type";
    public static final String ID = "id";
    public static final int TYPE_COURSE = 0;
    public static final int TYPE_CLASSROOM = 1;
    private int mPage;
    private int mId;
    private boolean mCanLoad = false;
    private SystemBarTintManager tintManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_review);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setNavigationBarTintEnabled(true);
            tintManager.setTintColor(Color.parseColor("#00000000"));
        }

        Intent intent = getIntent();
        mType = intent.getIntExtra(TYPE, TYPE_COURSE);
        mId = intent.getIntExtra(ID, -1);

        initView();
        initData();
    }

    private void initData() {
        mPage = 0;
        if (mType == TYPE_COURSE) {
            CourseDetailModel.getCourseReviews(mId, String.valueOf(10)
                    , String.valueOf(0), new ResponseCallbackListener<CourseReviewDetail>() {
                        @Override
                        public void onSuccess(CourseReviewDetail data) {
                            if (data.getData().size() < 10) {
                                mCourseReviewAdapter.setCanLoad(false);
                            } else {
                                mCourseReviewAdapter.setCanLoad(true);
                            }
                            mCourseReviewAdapter.setData(data.getData());
                        }

                        @Override
                        public void onFailure(String code, String message) {

                        }
                    });
        } else {
            CourseDetailModel.getClassroomReviews(mId, String.valueOf(10)
                    , String.valueOf(0), new ResponseCallbackListener<ClassroomReviewDetail>() {
                        @Override
                        public void onSuccess(ClassroomReviewDetail data) {
                            if (data.getData().size() < 10) {
                                mClassroomReviewAdapter.setCanLoad(false);
                            } else {
                                mClassroomReviewAdapter.setCanLoad(true);
                            }
                            mClassroomReviewAdapter.setData(data.getData());
                        }

                        @Override
                        public void onFailure(String code, String message) {

                        }
                    });
        }
    }

    @Override
    protected void initView() {
        super.initView();
        mLvContent = (ListView) findViewById(R.id.lv_content);
        if (mType == TYPE_COURSE) {
            mCourseReviewAdapter = new CourseReviewAdapter(mContext, mId);
            mLvContent.setAdapter(mCourseReviewAdapter);
        } else {
            mClassroomReviewAdapter = new ClassroomReviewAdapter(mContext, mId);
            mLvContent.setAdapter(mClassroomReviewAdapter);
        }
    }

}
