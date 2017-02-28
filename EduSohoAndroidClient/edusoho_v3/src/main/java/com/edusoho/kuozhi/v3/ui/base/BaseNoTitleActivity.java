package com.edusoho.kuozhi.v3.ui.base;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.core.MessageEngine;
import com.edusoho.kuozhi.v3.model.sys.MessageType;
import com.edusoho.kuozhi.v3.model.sys.WidgetMessage;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Created by DEL on 2016/11/24.
 */

public class BaseNoTitleActivity extends BaseActivity implements MessageEngine.MessageCallback {

    protected int mRunStatus;
    private Queue<WidgetMessage> mUIMessageQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        mUIMessageQueue = new ArrayDeque<>();
        app.registMsgSource(this);
    }

    protected void initView() {
        hideActionBar();
        View view = findViewById(R.id.back);
        if (view != null) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }

    protected void saveMessage(WidgetMessage message) {
        mUIMessageQueue.add(message);
    }

    protected void invokeUIMessage() {
        WidgetMessage message;
        while ((message = mUIMessageQueue.poll()) != null) {
            invoke(message);
        }
    }

    @Override
    public void invoke(WidgetMessage message) {
    }

    @Override
    public MessageType[] getMsgTypes() {
        return new MessageType[0];
    }

    @Override
    public int getMode() {
        return REGIST_CLASS;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRunStatus = MSG_RESUME;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRunStatus = MSG_PAUSE;
    }

    @Override
    public void finish() {
        super.finish();
        app.unRegistMsgSource(this);
    }
}
