package com.edusoho.kuozhi.imserver.command;

import com.edusoho.kuozhi.imserver.ImServer;
import com.edusoho.kuozhi.imserver.entity.MessageEntity;
import com.edusoho.kuozhi.imserver.util.MessageEntityBuildr;
import org.json.JSONObject;

/**
 * Created by su on 2016/3/18.
 */
public class ReplaceCommand extends BaseCommand {

    public ReplaceCommand(ImServer imServer)
    {
        super(imServer);
    }

    @Override
    public void invoke(JSONObject params) {
        super.invoke(params);
        int time = (int) System.currentTimeMillis();
        String cmd = params.optString("cmd");

        MessageEntity messageEntity =
                MessageEntityBuildr.getBuilder()
                        .addTime(time)
                        .addCmd(cmd)
                        .addStatus(MessageEntity.StatusType.NONE)
                        .builder();
        mImServer.onReceiveSignal(messageEntity);
        mImServer.stop();
    }
}
