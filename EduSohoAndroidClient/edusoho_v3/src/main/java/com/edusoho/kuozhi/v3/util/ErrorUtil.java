package com.edusoho.kuozhi.v3.util;

import android.content.Context;

import com.google.gson.Gson;

import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by suju on 16/10/28.
 */
public class ErrorUtil {

    public static void showErrorMessage(Context context, String errorStr) {
        LinkedHashMap errorResult = null;
        try {
            errorResult = new Gson().fromJson(errorStr, LinkedHashMap.class);
        } catch (Exception e) {
        }
        if (errorResult == null) {
            return;
        }

        if (errorResult.containsKey("error")) {
            LinkedHashMap<String, String> errorMap = (LinkedHashMap<String, String>) errorResult.get("error");
            if (errorMap != null && errorMap.containsKey("message")) {
                CommonUtil.longToast(context, errorMap.get("message"));
            }
        }
    }

    /**
     * 绑定手机号错误提示，unicode转汉字
     */
    public static String getStrFromUniCode(String str) {
        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
        Matcher matcher = pattern.matcher(str);
        char ch;
        while (matcher.find()) {
            ch = (char) Integer.parseInt(matcher.group(2), 16);
            str = str.replace(matcher.group(1), ch + "");
        }
        return str;
    }
}
