package com.edusoho.kuozhi.imserver.util;

/**
 * Created by suju on 16/5/2.
 */
public class IMConnectStatus {

    public static final int CLOSE = 0x01;
    public static final int OPEN = 0x02;
    public static final int END = 0x03;
    public static final int ERROR = 0x04;
    public static final int CONNECTING = 0x05;
    public static final int NO_READY = 0x06;
}
