package com.edusoho.kuozhi.imserver.util;

/**
 * Created by 菊 on 2016/5/17.
 */
public class DbUtil {

    public static String makePlaceholders(int[] intArray, String split) {
        StringBuffer stringBuffer = new StringBuffer();
        if (intArray == null || intArray.length == 0) {
            return stringBuffer.toString();
        }
        int length = intArray.length - 1;
        for (int i = 0; i < length; i++) {
            stringBuffer.append('?').append(split);
        }
        stringBuffer.append('?');
        return stringBuffer.toString();
    }

    public static String[] intArrayToStringArray(int[] intArray) {
        String[] stringArray = new String[intArray.length];
        if (intArray == null || intArray.length == 0) {
            return stringArray;
        }
        for (int i=0; i < intArray.length; i++) {
            stringArray[i] = String.valueOf(intArray[i]);
        }

        return stringArray;
    }
}
