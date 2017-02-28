package com.edusoho.kuozhi.v3.model.bal.site;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.entity.discovery.DiscoveryColumn;
import com.edusoho.kuozhi.v3.entity.site.Site;
import com.edusoho.kuozhi.v3.listener.ResponseCallbackListener;
import com.edusoho.kuozhi.v3.model.bal.http.ModelDecor;
import com.edusoho.kuozhi.v3.model.base.ApiResponse;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.util.Api;
import com.google.gson.reflect.TypeToken;

import java.util.List;

/**
 * Created by DEL on 2016/11/29.
 */

public class SiteModel {

    public static final String HOST =
            "https://open.qiqiuyun.net";

    public static void getSite(String searchKey, final ResponseCallbackListener<List<Site>> callbackListener) {
        RequestUrl requestUrl = EdusohoApp.app.bindNewHostUrl(HOST + Api.SCHOOLS
                + "?keyword=" + searchKey, true);
        EdusohoApp.app.getUrl(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ApiResponse<Site> apiResponse = ModelDecor.getInstance().decor(response, new TypeToken<ApiResponse<Site>>() {
                });
                if (apiResponse.resources != null) {
                    callbackListener.onSuccess(apiResponse.resources);
                } else if (apiResponse.error != null) {
                    callbackListener.onFailure(apiResponse.error.code, apiResponse.error.message);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callbackListener.onFailure("Error", error.getMessage());
            }
        });
    }
}
