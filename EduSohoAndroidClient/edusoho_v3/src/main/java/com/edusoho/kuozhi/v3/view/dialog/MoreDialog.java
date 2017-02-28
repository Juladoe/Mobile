package com.edusoho.kuozhi.v3.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.model.bal.Classroom;
import com.edusoho.kuozhi.v3.model.bal.course.Course;

/**
 * Created by remilia on 2017/1/9.
 */
public class MoreDialog extends Dialog {
    public MoreDialog(Context context) {
        super(context, R.style.PopDialogTheme2);
    }

    public MoreDialog(Context context, int theme) {
        super(context, theme);
    }

    private TextView mTvMove;
    private TextView mTvShare;
    private TextView mTvCancel;
    private View mParent;
    private MoreCallBack mMoreCallBack;
    private String mMoreTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_more);
        initView();
        initEvent();
    }

    public MoreDialog init(String moveTxt, MoreCallBack moreCallBack) {
        mMoreTxt = moveTxt;
        mMoreCallBack = moreCallBack;
        return this;
    }


    private void initView() {
        mTvMove = (TextView) findViewById(R.id.tv_more);
        mTvCancel = (TextView) findViewById(R.id.tv_cancel);
        mTvShare = (TextView) findViewById(R.id.tv_more_share);
        mParent = findViewById(R.id.parent);
        mTvMove.setText(mMoreTxt);
    }

    private void initEvent() {
        mParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mTvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMoreCallBack != null) {
                    mMoreCallBack.onCancelClick(v, MoreDialog.this);
                }
            }
        });
        mTvMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMoreCallBack != null) {
                    mMoreCallBack.onMoveClick(v, MoreDialog.this);
                }
            }
        });
        mTvShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMoreCallBack != null) {
                    mMoreCallBack.onShareClick(v, MoreDialog.this);
                }
            }
        });
    }

    public interface MoreCallBack {
        void onMoveClick(View v, Dialog dialog);

        void onShareClick(View v, Dialog dialog);

        void onCancelClick(View v, Dialog dialog);
    }
}
