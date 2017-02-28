package com.edusoho.kuozhi.v3.model.bal.courseDynamics;

import android.content.Context;

import com.edusoho.kuozhi.v3.model.provider.ModelProvider;
import com.edusoho.kuozhi.v3.model.provider.ProviderListener;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * Created by melomelon on 16/2/19.
 */
public class DynamicsProvider extends ModelProvider {
    public DynamicsProvider(Context context) {
        super(context);
    }

    public ProviderListener getDynamics(RequestUrl requestUrl){
        ProviderListener<ArrayList<CourseDynamicsItem>> providerListener = new ProviderListener<ArrayList<CourseDynamicsItem>>(){
        };
        addRequest(requestUrl,new TypeToken<ArrayList<CourseDynamicsItem>>(){
        },providerListener,providerListener);

        return providerListener;
    }
}
