package com.edusoho.kuozhi.imserver.command;

import com.edusoho.kuozhi.imserver.ImServer;

import org.json.JSONObject;

/**
 * Created by su on 2016/3/18.
 */
public abstract class BaseCommand implements ICommand {

    protected ImServer mImServer;

    public BaseCommand(ImServer imServer)
    {
        this.mImServer = imServer;
    }

    @Override
    public void invoke(JSONObject params) {
    }
}
