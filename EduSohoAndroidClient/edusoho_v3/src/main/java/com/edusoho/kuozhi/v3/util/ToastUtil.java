package com.edusoho.kuozhi.v3.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by JesseHuang on 16/6/3.
 */
public class ToastUtil {
    private volatile static ToastUtil instance;
    private static Toast mToast;
    private Context mContext;

    private ToastUtil(Context context) {
        mContext = context;
    }

    public static ToastUtil getInstance(Context context) {
        if (instance == null) {
            synchronized (ToastUtil.class) {
                if (instance == null) {
                    instance = new ToastUtil(context);
                }
            }
        }
        return instance;
    }

    public ToastUtil makeText(String msg, int duration) {
        if (mToast == null) {
            mToast = Toast.makeText(mContext, msg, duration);
        }
        return this;
    }

    public void show() {
        if (mToast.getView().getParent() == null) {
            mToast.show();
        }
    }
}
