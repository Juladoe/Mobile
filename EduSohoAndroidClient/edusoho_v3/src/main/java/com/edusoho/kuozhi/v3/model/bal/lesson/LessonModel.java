package com.edusoho.kuozhi.v3.model.bal.lesson;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.entity.lesson.Lesson;
import com.edusoho.kuozhi.v3.listener.ResponseCallbackListener;
import com.edusoho.kuozhi.v3.model.bal.http.ModelDecor;
import com.edusoho.kuozhi.v3.model.base.ApiResponse;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.util.Api;
import com.google.gson.reflect.TypeToken;

import java.util.List;

/**
 * Created by JesseHuang on 16/3/9.
 */
public class LessonModel {
    public static void getLessonByCourseId(String[] conditions, final ResponseCallbackListener<List<Lesson>> callbackListener) {
        String url = String.format(Api.LESSONS);
        RequestUrl requestUrl = EdusohoApp.app.bindNewApiUrl(url, false);
        requestUrl.setGetParams(conditions);
        EdusohoApp.app.getUrl(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ApiResponse<Lesson> apiResponse = ModelDecor.getInstance().decor(response, new TypeToken<ApiResponse<Lesson>>() {
                });
                if (apiResponse == null) {
                    return;
                }
                if (apiResponse.resources != null) {
                    callbackListener.onSuccess(apiResponse.resources);
                } else if (apiResponse.error != null) {
                    callbackListener.onFailure(apiResponse.error.code, apiResponse.error.message);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }
}
