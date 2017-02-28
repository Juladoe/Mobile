package com.edusoho.longinus.util;

import android.text.TextUtils;
import android.util.Log;

import com.edusoho.kuozhi.imserver.broadcast.IMBroadcastReceiver;
import com.edusoho.kuozhi.imserver.entity.MessageEntity;
import com.edusoho.kuozhi.imserver.listener.IConnectManagerListener;
import com.edusoho.kuozhi.imserver.ui.IMessageListPresenter;
import com.edusoho.longinus.data.LiveMessageBody;
import com.edusoho.longinus.persenter.ILiveChatPresenter;
import com.edusoho.longinus.persenter.ILiveVideoPresenter;

import java.util.List;

/**
 * Created by suju on 16/10/18.
 */
public class LiveIMBroadcastReceiver extends IMBroadcastReceiver {

    private String mConvNo;
    private ILiveChatPresenter mILiveChatMessgeListPresenter;
    private ILiveVideoPresenter mILiveVideoPresenter;
    private IMessageListPresenter mIMessageListPresenter;

    String[] filterArray = {
            "100001",
            "102001",
            "101003",
            "102002",
            "103004",
            "103005",
            "103007",
            "memberJoined",
            "success"
    };

    String[] signalArray = {
            "100001",
            "101003",
            "102002",
            "103004",
            "103005",
            "103007"
    };

    public LiveIMBroadcastReceiver(
            String convNo,
            ILiveVideoPresenter presenter,
            ILiveChatPresenter liveChatMessgeListPresenter,
            IMessageListPresenter MessageListPresenter) {
        this.mConvNo = convNo;
        this.mILiveVideoPresenter = presenter;
        this.mIMessageListPresenter = MessageListPresenter;
        this.mILiveChatMessgeListPresenter = liveChatMessgeListPresenter;
    }

    private boolean messageIsFilter(String type) {
        if (type == null) {
            return false;
        }
        for (String filter : filterArray) {
            if (type.equals(filter)) {
                return true;
            }
        }
        return false;
    }

    private boolean messageIsSignal(String type) {
        if (type == null) {
            return false;
        }
        for (String filter : signalArray) {
            if (type.equals(filter)) {
                return true;
            }
        }
        return false;
    }


    @Override
    protected void invokeReceiverSignal(MessageEntity message) {
        String cmd = message.getCmd();
        if ("connected".equals(cmd)) {
            mILiveChatMessgeListPresenter.joinLiveChatRoom();
            return;
        }
        if ("replace".equals(cmd)) {
            mILiveChatMessgeListPresenter.onReplace();
            return;
        }
        if (TextUtils.isEmpty(mConvNo) || !mConvNo.equals(message.getConvNo())) {
            return;
        }
        switch (cmd) {
            case "102002":
                mILiveVideoPresenter.updateNotice(message);
                break;
            case "103004":
            case "103005":
                mILiveChatMessgeListPresenter.setUserCanChatStatus(message);
                break;
            case "103007":
                mILiveVideoPresenter.onKill(message);
                break;
            case "100001":
                mILiveVideoPresenter.updateLivePlayStatus(message);
        }
    }

    @Override
    protected void invokeReceiver(MessageEntity message) {
        LiveMessageBody liveMessageBody = new LiveMessageBody(message);
        if (liveMessageBody != null && !messageIsFilter(liveMessageBody.getType())) {
            return;
        }
        if (liveMessageBody != null && messageIsSignal(liveMessageBody.getType())) {
            message.setCmd(liveMessageBody.getType());
            invokeReceiverSignal(message);
            return;
        }

        if (!TextUtils.isEmpty(message.getConvNo())
                && (TextUtils.isEmpty(mConvNo) || !mConvNo.equals(message.getConvNo()))) {
            return;
        }

        if (TextUtils.isEmpty(message.getCmd())) {
            return;
        }
        switch (message.getCmd()) {
            case "103004":
            case "103005":
                break;
            case "success":
                mIMessageListPresenter.onMessageSuccess(message);
                break;
            case "102001":
            case "memberJoined":
            case "flashMessage":
            case "message":
                mIMessageListPresenter.onReceiverMessageEntity(message);
                break;
        }
    }

    @Override
    protected void invokeConnectReceiver(int status, boolean isConnected, String[] ignoreNos) {
        Log.d("LiveIMBroadcastReceiver", "status:" + status);
        switch (status) {
            case IConnectManagerListener.INVALID:
                mILiveChatMessgeListPresenter.reConnectChatServer();
                break;
            case IConnectManagerListener.OPEN:
                mIMessageListPresenter.reSendMessageList();

        }
    }

    @Override
    protected void invokeOfflineMsgReceiver(List<MessageEntity> messageEntityList) {
    }
}
