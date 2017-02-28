package com.edusoho.kuozhi.imserver.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import com.edusoho.kuozhi.imserver.IMClient;
import com.edusoho.kuozhi.imserver.entity.MessageEntity;
import com.edusoho.kuozhi.imserver.listener.IConnectManagerListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by su on 2016/3/22.
 */
public class IMBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION = "action";
    public static final String ACTION_NAME = "com.edusoho.kuozhi.push.action.IM_MESSAGE";
    public static final int RECEIVER = 0;
    public static final int STATUS_CHANGE = 1;
    public static final int OFFLINE_MSG = 2;
    public static final int SIGNAL = 3;

    @Override
    public void onReceive(Context context, final Intent intent) {
        Log.d(getClass().getSimpleName(), "onReceive");
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                int action = intent.getIntExtra(ACTION, RECEIVER);
                if (action == RECEIVER) {
                    MessageEntity message = intent.getParcelableExtra("message");
                    invokeReceiver(message);
                } else if (action == OFFLINE_MSG) {
                    ArrayList<MessageEntity> messageEntityArrayList = intent.getParcelableArrayListExtra("message");
                    invokeOfflineMsgReceiver(messageEntityArrayList);
                } else if (action == STATUS_CHANGE) {
                    int status = intent.getIntExtra("status", IConnectManagerListener.OPEN);
                    boolean isConnected = intent.getBooleanExtra("isConnected", false);
                    String[] ignoreNos = intent.getStringArrayExtra("ignoreNos");
                    invokeConnectReceiver(status, isConnected, ignoreNos);
                } else if (action == SIGNAL) {
                    MessageEntity message = intent.getParcelableExtra("message");
                    invokeReceiverSignal(message);
                }
            }
        });
    }

    protected void invokeReceiverSignal(MessageEntity message) {
    }

    protected void invokeReceiver(MessageEntity message) {
        IMClient.getClient().invokeReceiver(message);
    }

    protected void invokeOfflineMsgReceiver(List<MessageEntity> messageEntityList) {
        IMClient.getClient().invokeOfflineMsgReceiver(messageEntityList);
    }

    protected void invokeConnectReceiver(int status, boolean isConnected, String[] ignoreNos) {
        IMClient.getClient().invokeConnectReceiver(status, isConnected, ignoreNos);
    }
}
