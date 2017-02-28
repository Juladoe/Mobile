package com.edusoho.kuozhi.v3.handler;


/**
 * Created by howzhi on 14-10-26.
 */
public abstract class ClientVersionHandler {

    public abstract boolean execute(String min, String max, String version);
}
