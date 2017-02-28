package com.edusoho.kuozhi.v3.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.v3.ui.base.ActionBarBaseActivity;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by JesseHuang on 15/5/18.
 */
public class MsgReminderActivity extends ActionBarBaseActivity {
    private CheckBox cbMsgSound;
    private CheckBox cbMsgVibrate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg_reminder);
        setBackMode(BACK, "新消息提醒");
        initView();
        initData();
    }

    private void initView() {
        cbMsgSound = (CheckBox) findViewById(R.id.cb_msg_sound);
        cbMsgVibrate = (CheckBox) findViewById(R.id.cb_msg_vibrate);
        cbMsgSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(mContext, "i_mySetting_newMessageNotification_sound");
                app.config.msgSound = cbMsgSound.isChecked() ? 1 : 0;
                app.saveConfig();
            }
        });
        cbMsgVibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(mContext, "i_mySetting_newMessageNotification_vibration");
                app.config.msgVibrate = cbMsgVibrate.isChecked() ? 2 : 0;
                app.saveConfig();
            }
        });
    }

    private void initData() {
        cbMsgSound.setChecked(app.config.msgSound == 1);
        cbMsgVibrate.setChecked(app.config.msgVibrate == 2);
    }
}
