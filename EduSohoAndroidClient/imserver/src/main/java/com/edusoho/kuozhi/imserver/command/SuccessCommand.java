package com.edusoho.kuozhi.imserver.command;

import com.edusoho.kuozhi.imserver.ImServer;
import com.edusoho.kuozhi.imserver.entity.MessageEntity;
import com.edusoho.kuozhi.imserver.util.MessageEntityBuildr;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 菊 on 2016/4/27.
 */
public class SuccessCommand extends BaseCommand {

    public SuccessCommand(ImServer imServer)
    {
        super(imServer);
    }

    @Override
    public void invoke(JSONObject params) {
        super.invoke(params);
        String msg = params.optString("msg");
        MessageEntity messageEntity = MessageEntityBuildr.getBuilder()
                .addCmd("success")
                .addMsg(msg)
                .builder();

        try {
            updateMessageStatus(msg);
        } catch (JSONException e) {
        }
        mImServer.onReceiveMessage(messageEntity);
    }

    private void updateMessageStatus(String msg) throws JSONException {
        JSONObject jsonObject = new JSONObject(msg);
        MessageEntity messageEntity = mImServer.getIMsgManager().getMessageByUID(jsonObject.optString("uid"));
        if (messageEntity == null) {
            return;
        }

        messageEntity.setStatus(MessageEntity.StatusType.SUCCESS);
        mImServer.getIMsgManager().updateMessageEntityByUID(messageEntity);
    }
}
