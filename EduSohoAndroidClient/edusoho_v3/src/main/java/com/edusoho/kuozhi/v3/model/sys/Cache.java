package com.edusoho.kuozhi.v3.model.sys;

/**
 * Created by JesseHuang on 15/4/23.
 */
public class Cache {

    public String value;
    public String key;

    public Cache(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String get() {
        return value;
    }
}
