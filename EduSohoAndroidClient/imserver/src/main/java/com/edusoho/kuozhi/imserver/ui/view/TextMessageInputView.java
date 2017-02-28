package com.edusoho.kuozhi.imserver.ui.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.edusoho.kuozhi.imserver.R;
import com.edusoho.kuozhi.imserver.ui.listener.InputViewControllerListener;
import com.edusoho.kuozhi.imserver.ui.listener.MessageSendListener;
import com.edusoho.kuozhi.imserver.util.SystemUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by suju on 16/10/19.
 */
public class TextMessageInputView extends FrameLayout implements IMessageInputView {

    protected EditText etSend;
    protected Button btnSend;
    protected ImageView ivUnSend;

    private MessageSendListener mMessageSendListener;
    private InputViewControllerListener mMessageControllerListener;

    public TextMessageInputView(Context context) {
        super(context);
        createView();
    }

    public TextMessageInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        createView();
    }

    @Override
    public void setMessageSendListener(MessageSendListener listener) {
        this.mMessageSendListener = listener;
    }

    @Override
    public void setMessageControllerListener(InputViewControllerListener listener) {
        this.mMessageControllerListener = listener;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        etSend.setHint(enabled ? "跟同学聊聊天吧..." : "当前不可发送消息");
        btnSend.setEnabled(enabled);
        btnSend.setVisibility(enabled ? VISIBLE : GONE);
        ivUnSend.setVisibility(enabled ? GONE : VISIBLE);
        etSend.setEnabled(enabled);
    }

    private void createView() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_live_message_tool_layout, this, true);
        initView();
    }

    protected void initView() {
        OnClickListener onClickListener = getViewOnClickListener();
        etSend = (EditText) findViewById(R.id.et_send_content);
        ivUnSend = (ImageView) findViewById(R.id.iv_unsend);
        etSend.addTextChangedListener(mContentTextWatcher);
        etSend.setOnEditorActionListener(getOnEditorActionListener());
        btnSend = (Button) findViewById(R.id.btn_send);
        btnSend.setOnClickListener(onClickListener);
        etSend.setOnFocusChangeListener(getContentOnFocusChangeListener());
        etSend.setOnClickListener(onClickListener);
    }

    protected OnFocusChangeListener getContentOnFocusChangeListener() {
        return new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    SystemUtil.setSoftKeyBoard(etSend, getContext(), SystemUtil.SHOW_KEYBOARD);
                }
            }
        };
    }

    private TextView.OnEditorActionListener getOnEditorActionListener() {
        return new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendClick();
                    return true;
                }
                return false;
            }
        };
    }

    protected TextWatcher mContentTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            btnSend.setEnabled(!TextUtils.isEmpty(s));
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    protected OnClickListener getViewOnClickListener() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.et_send_content) {
                    MobclickAgent.onEvent(getContext(), "liveRoom_inputBox");
                    mMessageControllerListener.onInputViewFocus(true);
                }else if (v.getId() == R.id.btn_send) {
                    MobclickAgent.onEvent(getContext(), "liveRoom_messageSendButton");
                    sendClick();
                }
            }
        };
    }

    private void sendClick() {
        //发送消息
        if (etSend.getText().length() == 0) {
            return;
        }
        mMessageSendListener.onSendMessage(etSend.getText().toString());
        etSend.setText("");
    }
}
