package com.edusoho.kuozhi.imserver.command;

import com.edusoho.kuozhi.imserver.ImServer;
import com.edusoho.kuozhi.imserver.service.IHeartManager;
import org.json.JSONObject;


/**
 * Created by su on 2016/3/18.
 */
public class AckCommand extends BaseCommand {

    public AckCommand(ImServer imServer)
    {
        super(imServer);
    }

    @Override
    public void invoke(JSONObject params) {
        super.invoke(params);
        String cmd = params.optString("cmd");
        if ("pong".equals(cmd)) {
            mImServer.getHeartManager().setPongResult(IHeartManager.PONG_SUCCESS);
        } else {
            mImServer.getHeartManager().setPongResult(IHeartManager.PONG_FAIL);
        }
    }
}
