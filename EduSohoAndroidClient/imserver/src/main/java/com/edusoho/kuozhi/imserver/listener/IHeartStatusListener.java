package com.edusoho.kuozhi.imserver.listener;

/**
 * Created by 菊 on 2016/4/22.
 */
public interface IHeartStatusListener {

    int TIMEOUT = 0010;

    void onPing();

    void onPong(int status);
}
