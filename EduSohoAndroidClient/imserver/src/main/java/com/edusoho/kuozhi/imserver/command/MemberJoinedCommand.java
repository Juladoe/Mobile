package com.edusoho.kuozhi.imserver.command;

import android.text.TextUtils;

import com.edusoho.kuozhi.imserver.ImServer;
import com.edusoho.kuozhi.imserver.entity.MessageEntity;
import com.edusoho.kuozhi.imserver.entity.message.MessageBody;
import com.edusoho.kuozhi.imserver.ui.entity.PushUtil;
import com.edusoho.kuozhi.imserver.util.MessageEntityBuildr;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by su on 2016/3/18.
 */
public class MemberJoinedCommand extends BaseCommand {

    public MemberJoinedCommand(ImServer imServer) {
        super(imServer);
    }

    @Override
    public void invoke(JSONObject params) {
        super.invoke(params);
        String clientId = params.optString("clientId");
        String clientName = params.optString("clientName");
        String convNo = params.optString("convNo");
        int time = params.optInt("time");
        String msgNo = params.optString("msgNo");
        String cmd = params.optString("cmd");

        if (TextUtils.isEmpty(msgNo)) {
            msgNo = String.format("%s-%s-%d", clientId, clientName, time);
        }
        MessageEntity messageEntity = MessageEntityBuildr.getBuilder()
                        .addMsg(wrapBody(clientId, clientName))
                        .addConvNo(convNo)
                        .addTime(time)
                        .addMsgNo(msgNo)
                        .addCmd(cmd)
                        .addStatus(MessageEntity.StatusType.NONE)
                        .builder();
        mImServer.onReceiveMessage(messageEntity);
    }

    private String wrapBody(String clientId, String clientName) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("v", MessageBody.VERSION);
            jsonObject.put("t", PushUtil.ChatMsgType.LABEL);
            JSONObject body = new JSONObject();
            body.put("clientId", clientId);
            body.put("clientName", clientName);
            body.put("cmd", "memberJoined");
            jsonObject.put("b", body.toString());
        } catch (JSONException e) {
        }

        return jsonObject.toString();
    }
}
