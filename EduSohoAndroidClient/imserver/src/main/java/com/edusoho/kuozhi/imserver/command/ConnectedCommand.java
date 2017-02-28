package com.edusoho.kuozhi.imserver.command;

import android.util.Log;

import com.edusoho.kuozhi.imserver.ImServer;
import com.edusoho.kuozhi.imserver.entity.MessageEntity;
import com.edusoho.kuozhi.imserver.util.MessageEntityBuildr;

import org.json.JSONObject;

/**
 * Created by su on 2016/3/18.
 */
public class ConnectedCommand extends BaseCommand {

    public ConnectedCommand(ImServer imServer)
    {
        super(imServer);
    }

    @Override
    public void invoke(JSONObject params) {
        Log.d("ConnectedCommand", "invoke");
        //mImServer.requestOfflineMsg();
        mImServer.getHeartManager().start();

        params.remove("cmd");
        MessageEntity messageEntity =
                MessageEntityBuildr.getBuilder()
                        .addMsg(params.toString())
                        .addCmd("connected")
                        .addStatus(MessageEntity.StatusType.NONE)
                        .builder();
        mImServer.onReceiveSignal(messageEntity);
    }

}
