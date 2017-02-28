package com.edusoho.kuozhi.v3.model.provider;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.edusoho.kuozhi.v3.listener.NormalCallback;

/**
 * Created by howzhi on 15/8/24.
 */
public abstract class ProviderListener<T> implements Response.Listener<T>, Response.ErrorListener {

    private NormalCallback<T> mCallback;
    private NormalCallback<VolleyError> mFailCallback;
    private T mResponse;

    @Override
    public void onErrorResponse(VolleyError error) {
        if (mFailCallback != null) {
            mFailCallback.success(error);
        }
    }

    @Override
    public void onResponse(T response) {
        if (mCallback == null) {
            mResponse = response;
            return;
        }
        mCallback.success(response);
    }

    public ProviderListener fail(NormalCallback<VolleyError> callback) {
        this.mFailCallback = callback;
        return this;
    }

    public ProviderListener success(NormalCallback<T> callabck) {
        this.mCallback = callabck;
        if (mResponse != null) {
            this.mCallback.success(mResponse);
            mResponse = null;
        }
        return this;
    }
}
