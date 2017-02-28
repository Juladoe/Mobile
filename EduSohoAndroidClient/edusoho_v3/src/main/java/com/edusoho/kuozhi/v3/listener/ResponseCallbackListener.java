package com.edusoho.kuozhi.v3.listener;

/**
 * Created by JesseHuang on 16/3/6.
 */
public interface ResponseCallbackListener<T> {
    void onSuccess(T data);

    void onFailure(String code, String message);
}
