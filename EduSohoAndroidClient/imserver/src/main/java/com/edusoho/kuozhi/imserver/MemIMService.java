package com.edusoho.kuozhi.imserver;

import com.edusoho.kuozhi.imserver.service.Impl.MemMsgManager;

/**
 * Created by su on 2016/3/18.
 */
public class MemIMService extends ImService {

    @Override
    protected ImServer getIMServer() {
        ImServer imServer = new ImServer(getBaseContext());
        imServer.setMsgManager(new MemMsgManager(getBaseContext()));
        return imServer;
    }
}
