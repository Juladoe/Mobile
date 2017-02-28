package com.edusoho.kuozhi.v3.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.imserver.IMClient;
import com.edusoho.kuozhi.imserver.managar.IMBlackListManager;
import com.edusoho.kuozhi.v3.ui.base.ActionBarBaseActivity;
import com.edusoho.kuozhi.v3.view.EduSohoGridView;
import com.makeramen.roundedimageview.RoundedImageView;

/**
 * Created by JesseHuang on 15/12/10.
 */
public class ChatItemBaseDetail extends ActionBarBaseActivity implements View.OnClickListener {

    public static final String CONV_NO = "convNo";

    protected int mFromId;
    protected String mConvNo;

    protected EduSohoGridView gvMemberAvatar;
    protected TextView tvMemberSum;
    protected TextView tvClassroomAnnouncement;
    protected TextView tvEntryClassroom;
    protected TextView tvClearChatRecord;
    protected View vAnnouncement;
    protected View vEntry;
    protected View vClearChatRecord;
    protected CheckBox mReceiveMsgModeCBox;
    protected Button btnDelRecordAndQuit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classroom_detail);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
        initView();
    }

    protected void initView() {
        gvMemberAvatar = (EduSohoGridView) findViewById(R.id.gv_member);
        tvMemberSum = (TextView) findViewById(R.id.tv_all_member);
        tvClassroomAnnouncement = (TextView) findViewById(R.id.tv_classroom_announcement);
        tvEntryClassroom = (TextView) findViewById(R.id.tv_entry_classroom);
        tvClearChatRecord = (TextView) findViewById(R.id.clear_record);
        btnDelRecordAndQuit = (Button) findViewById(R.id.btn_del_and_quit);
        mReceiveMsgModeCBox = (CheckBox) findViewById(R.id.cb_receivemsg_mode);

        findViewById(R.id.rl_announcement).setOnClickListener(this);
        findViewById(R.id.rl_entry).setOnClickListener(this);
        findViewById(R.id.rl_clear_record).setOnClickListener(this);
        btnDelRecordAndQuit.setOnClickListener(this);

        if (!TextUtils.isEmpty(mConvNo) && !"0".equals(mConvNo)) {
            findViewById(R.id.rl_recevie_msg).setVisibility(View.VISIBLE);
            mReceiveMsgModeCBox.setOnCheckedChangeListener(mReceiveMsgModeCBoxChangeListener);
            initReceiveMsgCBoxStatus();
        }
    }

    private void initReceiveMsgCBoxStatus() {
        int status = IMClient.getClient().getIMBlackListManager().getBlackListByConvNo(mConvNo);
        mReceiveMsgModeCBox.setChecked(status == IMBlackListManager.NO_DISTURB);
    }

    private CompoundButton.OnCheckedChangeListener mReceiveMsgModeCBoxChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            IMBlackListManager imBlackListManager = IMClient.getClient().getIMBlackListManager();
            int status = isChecked ? IMBlackListManager.NO_DISTURB : IMBlackListManager.NORMAL;
            if (imBlackListManager.getBlackListByConvNo(mConvNo) == IMBlackListManager.NONE) {
                imBlackListManager.createBlackList(mConvNo, status);
                return;
            }
            IMClient.getClient().getIMBlackListManager().updateByConvNo(mConvNo, status);
        }
    };

    protected void initData() {
    }

    @Override
    public void onClick(View v) {

    }

    public static class ViewHolder {
        public RoundedImageView ivAvatar;
        public TextView tvMemberName;

        public ViewHolder(View view) {
            ivAvatar = (RoundedImageView) view.findViewById(R.id.iv_member_avatar);
            tvMemberName = (TextView) view.findViewById(R.id.tv_member_name);
        }
    }
}
