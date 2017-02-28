package com.edusoho.kuozhi.v3.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.ui.base.ActionBarBaseActivity;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by JesseHuang on 16/5/8.
 */
public class ThreadActivity extends ActionBarBaseActivity implements View.OnClickListener {

    private Toolbar toolbar;
    private TextView tvCancel;
    private TextView tvPost;
    private EditText etThreadTitle;
    private EditText etThreadContent;

    private int mCourseId;
    private int mLessonId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread);
        initView();
        initData();
    }

    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        tvCancel = (TextView) findViewById(R.id.tv_cancel);
        tvPost = (TextView) findViewById(R.id.tv_post);
        etThreadTitle = (EditText) findViewById(R.id.et_thread_title);
        etThreadContent = (EditText) findViewById(R.id.et_thread_title);
        setSupportActionBar(toolbar);
        tvCancel.setOnClickListener(this);
        tvPost.setOnClickListener(this);
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            mCourseId = intent.getIntExtra(Const.COURSE_ID, 0);
            mLessonId = intent.getIntExtra(Const.LESSON_ID, 0);
        } else {
            CommonUtil.longToast(mContext, "课程信息获取失败");
        }
    }

    private synchronized void createThread() {
        RequestUrl requestUrl = app.bindNewApiUrl(Const.CREATE_THREAD, true);
        Map<String, String> params = requestUrl.getParams();
        params.put("threadType", "course");
        params.put("courseId", mCourseId + "");
        if (mLessonId != 0) {
            params.put("lessonId", mLessonId + "");
        }
        params.put("type", "question");
        params.put("title", etThreadTitle.getText().toString().trim());
        params.put("content", etThreadContent.getText().toString().trim());
        ajaxPost(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                tvPost.setEnabled(true);
                if (response.contains("threadId")) {
                    CommonUtil.longToast(mContext, "问题提交成功");
                    finish();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                tvPost.setEnabled(true);
                CommonUtil.longToast(mContext, "问题提交失败");
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == tvCancel.getId()) {
            finish();
        } else if (v.getId() == tvPost.getId()) {
            tvPost.setEnabled(false);
            createThread();
        }
    }
}
