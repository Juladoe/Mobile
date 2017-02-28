package com.edusoho.kuozhi.v3.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.imserver.ui.listener.InputViewControllerListener;
import com.edusoho.kuozhi.imserver.ui.listener.MessageSendListener;
import com.edusoho.kuozhi.imserver.ui.view.IMessageInputView;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.InputUtils;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by DF on 2017/1/11.
 */

public class NewTextMessageInputView extends FrameLayout implements IMessageInputView {

    private MessageSendListener mMessageSendlistener;
    private InputViewControllerListener mMessageControllerListener;
    private RelativeLayout mRlReplay;
    private RelativeLayout mRlReplayEdit;
    private TextView mTvCancel;
    private TextView mTvIssue;
    private EditText mEtContent;
    private Context mContext;

    public NewTextMessageInputView(Context context) {
        this(context, null);
    }

    public NewTextMessageInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        createView();
    }

    private void createView() {
        LayoutInflater.from(getContext()).inflate(R.layout.discuss_message_input_view, this, true);
        initView();
    }

    private void initView() {
        OnClickListener onClickListener = getVIewOnClickListener();
        mRlReplay = (RelativeLayout) findViewById(R.id.rl_replay);
        mRlReplayEdit = (RelativeLayout) findViewById(R.id.rl_replay_edit);
        mTvCancel = (TextView) findViewById(R.id.tv_cancel);
        mTvIssue = (TextView) findViewById(R.id.tv_issue);
        mEtContent = (EditText) findViewById(R.id.et_content);
        mRlReplay.setOnClickListener(onClickListener);
        mTvCancel.setOnClickListener(onClickListener);
        mTvIssue.setOnClickListener(onClickListener);
        mEtContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mEtContent.length() > 0) {
                    mTvIssue.setTextColor(getResources().getColor(R.color.primary));
                } else {
                    mTvIssue.setTextColor(getResources().getColor(R.color.secondary2_font_color));
                }
            }
        });
    }

    @Override
    public void setMessageSendListener(MessageSendListener listener) {
        this.mMessageSendlistener = listener;
    }

    @Override
    public void setMessageControllerListener(InputViewControllerListener listener) {
        this.mMessageControllerListener = listener;
    }

    public OnClickListener getVIewOnClickListener() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.rl_replay) {
                    mRlReplay.setVisibility(GONE);
                    mEtContent.requestFocus();
                    InputUtils.showKeyBoard(mEtContent, getContext());
                    mRlReplayEdit.setVisibility(VISIBLE);
                } else if (v.getId() == R.id.tv_cancel) {
                    mRlReplayEdit.setVisibility(GONE);
                    mRlReplay.setVisibility(VISIBLE);
                    hideKeyBoard();
                } else if (v.getId() == R.id.tv_issue) {
                    if (mEtContent.getText().length() == 0) {
                        CommonUtil.shortCenterToast(getContext(), "内容不可为空");
                        return;
                    }
                    MobclickAgent.onEvent(mContext, "Q&A_topic_reply");
                    mMessageSendlistener.onSendMessage(mEtContent.getText().toString());
                    mEtContent.setText("");
                    mRlReplayEdit.setVisibility(GONE);
                    mRlReplay.setVisibility(VISIBLE);
                    hideKeyBoard();
                }
            }
        };
    }

    public void hideKeyBoard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getApplicationContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(mEtContent.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
