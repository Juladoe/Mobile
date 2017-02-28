package com.edusoho.kuozhi.imserver.command;

import android.text.TextUtils;
import android.util.Log;

import com.edusoho.kuozhi.imserver.ImServer;
import com.edusoho.kuozhi.imserver.entity.MessageEntity;
import com.edusoho.kuozhi.imserver.util.MessageEntityBuildr;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by 菊 on 2016/5/9.
 */
public class OfflineMsgCommand extends BaseCommand {

    public OfflineMsgCommand(ImServer imServer)
    {
        super(imServer);
    }

    @Override
    public void invoke(JSONObject params) {
        super.invoke(params);
        JSONArray msgs = params.optJSONArray("msgs");
        int length = msgs.length();
        Log.d(getClass().getSimpleName(), "OfflineMsgCommand : " + length);
        ArrayList<MessageEntity> array = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            JSONObject msg = msgs.optJSONObject(i);
            array.add(buildMessageEntity(msg));
        }

        mImServer.onReceiveOfflineMsg(array);
    }

    private MessageEntity buildMessageEntity(JSONObject params) {
        String toId = params.optString("toId");
        String fromId = params.optString("fromId");
        String toName = params.optString("toName");
        String fromName = params.optString("fromName");
        String msg = params.optString("msg");
        if(TextUtils.isEmpty(msg)) {
            msg = params.optString("body");
        }
        String convNo = params.optString("convNo");
        int time = params.optInt("time");
        if (time == 0) {
            time = params.optInt("createdTime");
        }
        String msgNo = params.optString("msgNo");

        return MessageEntityBuildr.getBuilder()
                .addToId(toId)
                .addToName(toName)
                .addFromId(fromId)
                .addFromName(fromName)
                .addMsg(msg)
                .addConvNo(convNo)
                .addTime(time)
                .addMsgNo(msgNo)
                .addCmd("offlineMsg")
                .builder();
    }
}
