package com.edusoho.kuozhi.v3.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.edusoho.kuozhi.R;

/**
 * Created by remilia on 2017/1/9.
 */
public class SureDialog extends Dialog {
    public SureDialog(Context context) {
        super(context, R.style.PopDialogTheme3);
    }

    public SureDialog(Context context, int theme) {
        super(context, theme);
    }

    private View mTvCancel;
    private View mTvSure;
    private TextView mTvTxt;
    private View mParent;
    private CallBack mCallBack;
    private String mTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_sure);
        initView();
        initEvent();
    }

    public SureDialog init(String txt, CallBack callBack) {
        mTxt = txt;
        mCallBack = callBack;
        return this;
    }

    private void initView() {
        mTvSure = findViewById(R.id.tv_sure);
        mTvCancel = findViewById(R.id.tv_cancel);
        mTvTxt = (TextView) findViewById(R.id.tv_txt);
        mParent = findViewById(R.id.parent);
        mTvTxt.setText(mTxt);
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
                if (mCallBack != null) {
                    mCallBack.onCancelClick(v, SureDialog.this);
                }
            }
        });
        mTvSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCallBack != null) {
                    mCallBack.onSureClick(v, SureDialog.this);
                }
            }
        });
    }

    public interface CallBack {
        void onSureClick(View v, Dialog dialog);

        void onCancelClick(View v, Dialog dialog);
    }
}
