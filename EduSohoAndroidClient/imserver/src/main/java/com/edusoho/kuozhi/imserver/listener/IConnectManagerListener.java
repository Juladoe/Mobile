package com.edusoho.kuozhi.imserver.listener;

/**
 * Created by 菊 on 2016/4/22.
 */
public interface IConnectManagerListener {

    int CLOSE = 0001;
    int OPEN = 0002;
    int END = 0003;
    int ERROR = 0004;
    int CONNECTING = 0005;
    int NONE = 0006;
    int INVALID = 0007;

    void onStatusChange(int status, String error);
}
