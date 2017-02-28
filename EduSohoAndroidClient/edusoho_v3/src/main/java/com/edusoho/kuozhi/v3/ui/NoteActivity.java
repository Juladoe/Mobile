package com.edusoho.kuozhi.v3.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.entity.note.Note;
import com.edusoho.kuozhi.v3.listener.ResponseCallbackListener;
import com.edusoho.kuozhi.v3.model.bal.note.NoteModel;
import com.edusoho.kuozhi.v3.ui.base.ActionBarBaseActivity;
import com.edusoho.kuozhi.v3.util.ActivityUtil;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;

/**
 * Created by JesseHuang on 16/5/9.
 */
public class NoteActivity extends ActionBarBaseActivity implements View.OnClickListener {

    private Toolbar toolbar;
    private TextView tvCancel;
    private TextView tvPost;
    private EditText etNoteContent;
    private Switch swShare;

    private int mCourseId;
    private int mLessonId;

    private NoteModel noteModel = new NoteModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        ActivityUtil.setStatusViewBackgroud(this, getResources().getColor(R.color.textIcons));
        initView();
        initData();
    }

    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        tvCancel = (TextView) findViewById(R.id.tv_cancel);
        tvPost = (TextView) findViewById(R.id.tv_post);
        swShare = (Switch) findViewById(R.id.sw_share);
        etNoteContent = (EditText) findViewById(R.id.et_note_content);
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
            return;
        }

        noteModel.getLessonNote(mCourseId, mLessonId, app.loginUser.id, new ResponseCallbackListener<Note>() {
            @Override
            public void onSuccess(Note note) {
                if (note != null) {
                    etNoteContent.setText(Html.fromHtml(note.content).toString());
                    swShare.setChecked(note.status == 1);
                }
            }

            @Override
            public void onFailure(String code, String message) {
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == tvCancel.getId()) {
            finish();
        } else if (v.getId() == tvPost.getId()) {
            noteModel.postNote(mCourseId, mLessonId, swShare.isChecked() ? 1 : 0, etNoteContent.getText().toString().trim(), new ResponseCallbackListener<Note>() {
                @Override
                public void onSuccess(Note data) {
                    if (data != null) {
                        CommonUtil.longToast(mContext, "笔记保存成功");
                        finish();
                    }
                }

                @Override
                public void onFailure(String code, String message) {
                    CommonUtil.longToast(mContext, "笔记保存失败");
                }
            });
        }
    }
}
