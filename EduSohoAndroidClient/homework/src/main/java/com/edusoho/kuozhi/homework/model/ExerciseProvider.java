package com.edusoho.kuozhi.homework.model;

import android.content.Context;

import com.edusoho.kuozhi.v3.model.provider.ModelProvider;
import com.edusoho.kuozhi.v3.model.provider.ProviderListener;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.util.volley.BaseVolleyRequest;
import com.google.gson.reflect.TypeToken;

import java.util.LinkedHashMap;

/**
 * Created by Melomelon on 2015/10/26.
 */
public class ExerciseProvider extends ModelProvider {
    public ExerciseProvider(Context context) {
        super(context);
    }

    public ProviderListener<ExerciseModel> getExercise(RequestUrl requestUrl) {
        return buildSimpleGetRequest(requestUrl, new TypeToken<ExerciseModel>(){}).build();
    }

    public ProviderListener<ExerciseResult> getExerciseResult(RequestUrl requestUrl, boolean isCache) {
        RequestOption requestOption = buildSimpleGetRequest(
                requestUrl, new TypeToken<ExerciseResult>(){});
        if (isCache) {
            requestOption.getRequest().setCacheUseMode(BaseVolleyRequest.ALWAYS_USE_CACHE);
        }
        return requestOption.build();
    }

    public ProviderListener<LinkedHashMap<String, String>> postExerciseResult(RequestUrl requestUrl) {
        RequestOption requestOption = buildSimplePostRequest(
                requestUrl, new TypeToken<LinkedHashMap<String, String>>(){});

        return requestOption.build();
    }
}
