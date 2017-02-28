package com.edusoho.kuozhi.v3.model.provider;

import android.content.Context;

import com.edusoho.kuozhi.v3.model.bal.thread.MyThreadEntity;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.google.gson.reflect.TypeToken;

/**
 * Created by melomelon on 16/3/1.
 */
public class MyThreadProvider extends ModelProvider {

    public MyThreadProvider(Context context) {
        super(context);
    }

    public ProviderListener getMyCreatedThread(RequestUrl requestUrl) {
        ProviderListener<MyThreadEntity[]> providerListener = new ProviderListener() {
        };
        addRequest(requestUrl, new TypeToken<MyThreadEntity[]>() {
        }, providerListener, providerListener);
        return providerListener;
    }

    public ProviderListener getMyPostedThread(RequestUrl requestUrl) {
        ProviderListener<MyThreadEntity[]> providerListener = new ProviderListener() {
        };
        addRequest(requestUrl, new TypeToken<MyThreadEntity[]>() {
        }, providerListener, providerListener);
        return providerListener;
    }
}
