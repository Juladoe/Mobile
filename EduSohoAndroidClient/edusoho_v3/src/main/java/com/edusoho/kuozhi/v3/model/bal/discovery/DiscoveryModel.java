package com.edusoho.kuozhi.v3.model.bal.discovery;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.entity.discovery.DiscoveryClassroom;
import com.edusoho.kuozhi.v3.entity.discovery.DiscoveryColumn;
import com.edusoho.kuozhi.v3.entity.discovery.DiscoveryCourse;
import com.edusoho.kuozhi.v3.listener.ResponseCallbackListener;
import com.edusoho.kuozhi.v3.model.bal.http.ModelDecor;
import com.edusoho.kuozhi.v3.model.base.ApiResponse;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.util.Api;
import com.google.gson.reflect.TypeToken;

import java.util.List;

/**
 * Created by JesseHuang on 16/3/6.
 */
public class DiscoveryModel {

    public void getDiscoveryColumns(final ResponseCallbackListener<List<DiscoveryColumn>> callbackListener) {
        RequestUrl requestUrl = EdusohoApp.app.bindNewApiUrl(Api.DISCOVERY_COLUMNS, true);
        EdusohoApp.app.getUrl(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ApiResponse<DiscoveryColumn> apiResponse = ModelDecor.getInstance().decor(response, new TypeToken<ApiResponse<DiscoveryColumn>>() {
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

            }
        });
    }

    public void getDiscoveryEmptyColumns(final ResponseCallbackListener<List<DiscoveryCourse>> callbackListener) {
        RequestUrl requestUrl = EdusohoApp.app.bindNewApiUrl(Api.DISCOVERY_COURSES_COLUMNS_EMPTY_COLUMNS, true);
        EdusohoApp.app.getUrl(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ApiResponse<DiscoveryCourse> apiResponse = ModelDecor.getInstance().decor(response, new TypeToken<ApiResponse<DiscoveryCourse>>() {
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

            }
        });
    }

    public void getDiscoveryCourseByColumn(DiscoveryColumn discoveryColumn, final ResponseCallbackListener<List<DiscoveryCourse>> callbackListener) {
        String url = String.format(Api.DISCOVERY_COURSES_COLUMNS, discoveryColumn.orderType, discoveryColumn.categoryId + "",
                discoveryColumn.showCount + "", discoveryColumn.type);
        RequestUrl requestUrl = EdusohoApp.app.bindNewApiUrl(url, true);
        EdusohoApp.app.getUrl(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ApiResponse<DiscoveryCourse> apiResponse = ModelDecor.getInstance().decor(response, new TypeToken<ApiResponse<DiscoveryCourse>>() {
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

            }
        });
    }

    public void getDiscoveryClassroomByColumn(DiscoveryColumn discoveryColumn, final ResponseCallbackListener<List<DiscoveryClassroom>> callbackListener) {
        String url = String.format(Api.DISCOVERY_CLASSROOMS_COLUMNS, discoveryColumn.orderType, discoveryColumn.categoryId + "",
                discoveryColumn.showCount + "");
        RequestUrl requestUrl = EdusohoApp.app.bindNewApiUrl(url, true);
        EdusohoApp.app.getUrl(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ApiResponse<DiscoveryClassroom> apiResponse = ModelDecor.getInstance().decor(response, new TypeToken<ApiResponse<DiscoveryClassroom>>() {
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

            }
        });
    }
}
