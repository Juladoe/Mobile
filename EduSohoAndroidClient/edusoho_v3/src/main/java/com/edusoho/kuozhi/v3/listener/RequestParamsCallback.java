package com.edusoho.kuozhi.v3.listener;

import java.util.HashMap;

/**
 * Created by JesseHuang on 15/4/23.
 */
public interface RequestParamsCallback {
    void addParams(HashMap<String, String> params);
}
