package com.edusoho.kuozhi.imserver.util;

/**
 * Created by 菊 on 2016/5/14.
 */
public class MessageUtil {

    public static int parseInt(String timeStr) {
        try {
            return Integer.parseInt(timeStr);
        } catch (Exception e) {
            return 0;
        }
    }

    public static long parseLong(String timeStr) {
        try {
            return Long.parseLong(timeStr);
        } catch (Exception e) {
            return 0;
        }
    }
}
