package com.edusoho.kuozhi.v3.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.EdusohoApp;

public class ExitCoursePopupDialog extends Dialog {

    private RadioGroup popup_select;
    private TextView mTitleView;
    private TextView mContentView;
    private Context mContext;
    private String[] mArray;

    private int mSelectPositon;

    public static final int CANCEL = 0001;
    public static final int OK = 0002;

    private PopupClickListener mClickListener;

    public ExitCoursePopupDialog(Context context, int style, int theme, PopupClickListener clickListener) {
        super(context, theme);
        setContentView(style);
        mContext = context;
        mClickListener = clickListener;
        initView();
    }

    public void setStringArray(int arrayId) {
        popup_select.removeAllViews();
        mArray = mContext.getResources().getStringArray(arrayId);
        int index = 0;
        for (String text : mArray) {
            RadioButton radioButton = (RadioButton) LayoutInflater.from(mContext).inflate(
                    R.layout.select_radiobtn, null);
            radioButton.setText(text);
            popup_select.addView(radioButton, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (index == mSelectPositon) {
                popup_select.check(radioButton.getId());
            }
            index++;
        }
    }

    private void initView() {
        mContentView = (TextView) findViewById(R.id.popup_select_label);
        mTitleView = (TextView) findViewById(R.id.popup_title);
        popup_select = (RadioGroup) findViewById(R.id.popup_select);
        findViewById(R.id.popup_ok_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mClickListener != null) {
                    int selId = 0;
                    int count = popup_select.getChildCount();
                    for (int i = 0; i < count; i++) {
                        RadioButton child = (RadioButton) popup_select.getChildAt(i);
                        if (child.isChecked()) {
                            selId = i;
                            break;
                        }
                    }
                    mClickListener.onClick(OK, selId, mArray[selId]);
                }
                dismiss();
            }
        });
        View cancelBtn = findViewById(R.id.popup_cancel_btn);
        if (cancelBtn != null) {
            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mClickListener != null) {
                        mClickListener.onClick(CANCEL, 0, null);
                    }
                    dismiss();
                }
            });
        }
    }

    public static ExitCoursePopupDialog create(
            Context context, PopupClickListener clickListener) {
        ExitCoursePopupDialog dlg = new ExitCoursePopupDialog(
                context, R.layout.popup_select, R.style.loadDlgTheme, clickListener);
        dlg.setStringArray(R.array.exitcourse_array);
        dlg.setContent("退学原因");
        return dlg;
    }

    public void setContent(String content) {
        mContentView.setText(content);
        if (!TextUtils.isEmpty(content)) {
            mContentView.setVisibility(View.VISIBLE);
        }
    }

    public static ExitCoursePopupDialog createNormal(
            Context context, String title, PopupClickListener clickListener) {
        ExitCoursePopupDialog dlg = new ExitCoursePopupDialog(
                context, R.layout.normal_select_dlg_layout, R.style.loadDlgTheme, clickListener);
        dlg.setTitle(title);
        dlg.setSelectPosition(EdusohoApp.app.config.offlineType);
        dlg.setStringArray(R.array.offline_array);
        return dlg;
    }

    public void setSelectPosition(int position) {
        mSelectPositon = position;
    }

    public void setTitle(String title) {
        mTitleView.setText(title);
    }

    public interface PopupClickListener {
        void onClick(int button, int position, String selStr);
    }
}
