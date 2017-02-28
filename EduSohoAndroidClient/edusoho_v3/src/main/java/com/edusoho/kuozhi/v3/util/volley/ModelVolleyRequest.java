package com.edusoho.kuozhi.v3.util.volley;

import android.text.TextUtils;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.util.RequestUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Created by suju on 16/10/28.
 */
public class ModelVolleyRequest<T> extends BaseVolleyRequest {

    private TypeToken<T> mTypeToken;

    public ModelVolleyRequest(int method,
                              RequestUrl requestUrl,
                              TypeToken<T> typeToken,
                              Response.Listener<T> listener,
                              Response.ErrorListener errorListener
    ) {
        super(method, requestUrl, listener, new InnerErrorListener(errorListener));
        this.mTypeToken = typeToken;
    }

    @Override
    protected T getResponseData(NetworkResponse response) {
        try {
            String jsonStr = RequestUtil.handleRequestError(response.data);
            return new Gson().fromJson(jsonStr, mTypeToken.getType());
        } catch (RequestUtil.RequestErrorException e) {
            Log.d("ModelVolleyRequest", new String(response.data));
        }
        return null;
    }

    public static class InnerErrorListener implements Response.ErrorListener {

        private Response.ErrorListener targetListener;

        public InnerErrorListener(Response.ErrorListener errorListener) {
            this.targetListener = errorListener;
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            targetListener.onErrorResponse(error);
            if (error.networkResponse == null) {
                return;
            }
            try {
                if (TextUtils.isEmpty(RequestUtil.handleRequestError(error.networkResponse.data))) {
                    return;
                }
            } catch (RequestUtil.RequestErrorException re) {

            }
            if (targetListener != null) {
                targetListener.onErrorResponse(error);
            }
        }
    };
}
