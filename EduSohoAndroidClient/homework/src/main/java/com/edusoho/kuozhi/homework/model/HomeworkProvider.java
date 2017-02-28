package com.edusoho.kuozhi.homework.model;

import android.content.Context;
import com.edusoho.kuozhi.v3.model.provider.ModelProvider;
import com.edusoho.kuozhi.v3.model.provider.ProviderListener;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.util.volley.BaseVolleyRequest;
import com.google.gson.reflect.TypeToken;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by howzhi on 15/10/15.
 */
public class HomeworkProvider extends ModelProvider {

    public HomeworkProvider(Context context)
    {
        super(context);
    }

    public ProviderListener<HomeWorkModel> getHomeWork(RequestUrl requestUrl) {
        return buildSimpleGetRequest(requestUrl, new TypeToken<HomeWorkModel>(){}).build();
    }

    public ProviderListener<HomeWorkResult> getHomeWorkResult(RequestUrl requestUrl, boolean isCache) {
        RequestOption requestOption = buildSimpleGetRequest(
                requestUrl, new TypeToken<HomeWorkResult>(){});
        if (isCache) {
            requestOption.getRequest().setCacheUseMode(BaseVolleyRequest.ALWAYS_USE_CACHE);
        }
        return requestOption.build();
    }

    public ProviderListener<LinkedHashMap<String, String>> postHomeWorkResult(RequestUrl requestUrl) {
        RequestOption requestOption = buildSimplePostRequest(
                requestUrl, new TypeToken<LinkedHashMap<String, String>>(){});

        return requestOption.build();
    }
}
