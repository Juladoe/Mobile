package com.edusoho.kuozhi.v3.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.ui.base.BaseNoTitleActivity;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.CourseUtil;
import com.edusoho.kuozhi.v3.util.SystemBarTintManager;

/**
 * Created by remilia on 2017/1/4.
 */
public class ReviewActivity extends BaseNoTitleActivity {

    private LinearLayout mLayoutStar;

    private EditText mEtReview;
    private View mTvSure;
    private SystemBarTintManager tintManager;

    public static final String TYPE = "type";
    public static final String ID = "id";
    public static final int REVIEW_RESULT = 0x5;
    public static final int TYPE_CLASSROOM = 0;
    public static final int TYPE_COURSE = 1;

    private int mId;
    private int mType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
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
        mId = intent.getIntExtra(ID, 0);
        mType = intent.getIntExtra(TYPE,TYPE_COURSE);
        initView();
        initEvent();
    }

    @Override
    protected void initView() {
        super.initView();
        mLayoutStar = (LinearLayout) findViewById(R.id.layout_star);
        mEtReview = (EditText) findViewById(R.id.et_review);
        mTvSure = findViewById(R.id.tv_sure);
    }

    private int mNum = 0;

    private void initEvent() {
        final int length = mLayoutStar.getChildCount();
        View.OnClickListener starClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean check = true;
                for (int i = 0; i < length; i++) {
                    View star = mLayoutStar.getChildAt(i);
                    if (star != null && star instanceof TextView) {
                        if (check) {
                            ((TextView) star).setTextColor(
                                    getResources().getColor(R.color.secondary2_color));
                        } else {
                            ((TextView) star).setTextColor(
                                    getResources().getColor(R.color.disabled_hint_color));
                        }
                    }
                    if (star.equals(v)) {
                        mNum = i + 1;
                        check = false;
                    }
                }
            }
        };
        for (int i = 0; i < length; i++) {
            View v = mLayoutStar.getChildAt(i);
            if (v != null) {
                v.setOnClickListener(starClickListener);
            }
        }
        mTvSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mType == TYPE_COURSE){
                    CourseUtil.reviewCourse(mId, mNum, mEtReview.getText().toString()
                            , new CourseUtil.OnReviewCourseListener() {
                        @Override
                        public void onReviewCourseSuccess(String response) {
                            CommonUtil.shortToast(ReviewActivity.this,"评价成功");
                            setResult(REVIEW_RESULT);
                            finish();
                        }

                        @Override
                        public void onReviewCourseError(String response) {
                            CommonUtil.shortToast(ReviewActivity.this,"评价失败");
                        }
                    });
                }else{

                }
            }
        });
    }

}
