package com.edusoho.kuozhi.v3.util;

import java.util.regex.Pattern;

/**
 * Created by JesseHuang on 2016/11/28.
 */

public class Validator {

    public static final String REGEX_EMAIL = "^\\w+(\\.\\w+)*@[a-zA-Z0-9]+(\\.[a-zA-Z0-9]+)*(\\.[a-zA-Z]{2,})$";

    public static final String REGEX_MOBILE = "^1\\d{10}$";

    public static boolean isMail(String email) {
        return Pattern.matches(REGEX_EMAIL, email);
    }

    public static boolean isPhone(String mobile) {
        return Pattern.matches(REGEX_MOBILE, mobile);
    }
}
