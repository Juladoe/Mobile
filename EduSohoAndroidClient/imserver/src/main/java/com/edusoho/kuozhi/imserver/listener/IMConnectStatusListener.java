package com.edusoho.kuozhi.imserver.listener;

/**
 * Created by 菊 on 2016/4/29.
 */
public interface IMConnectStatusListener {

    void onError();

    void onClose();

    void onConnect();

    void onOpen();

    void onInvalid(String[] ig);
}
