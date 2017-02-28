package com.edusoho.kuozhi.v3.model.provider;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.model.sys.School;
import com.edusoho.kuozhi.v3.util.ApiTokenUtil;
import com.edusoho.kuozhi.v3.util.RequestUtil;
import com.edusoho.kuozhi.v3.util.SchoolUtil;
import com.edusoho.kuozhi.v3.util.VolleySingleton;
import com.edusoho.kuozhi.v3.util.volley.BaseVolleyRequest;
import com.edusoho.kuozhi.v3.util.volley.ModelVolleyRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Field;
import java.util.Map;


/**
 * Created by howzhi on 15/8/24.
 */
public abstract class ModelProvider {

    protected VolleySingleton mVolley;
    protected Gson mGson;
    protected Context mContext;
    private static final String TAG = "ModelProvider";

    public ModelProvider(Context context) {
        this.mContext = context;
        this.mGson = new Gson();
        this.mVolley = VolleySingleton.getInstance(context);
    }

    protected String getToken() {
        Map<String, String> tokenMap = ApiTokenUtil.getToken(mContext);
        return tokenMap.containsKey("token") ? tokenMap.get("token") : "";
    }

    protected String getHost() {
        School school = SchoolUtil.getDefaultSchool(mContext);
        return school == null ? "" : school.host;
    }

    protected String getDomain() {
        Uri hostUri = Uri.parse(getHost());
        if (hostUri != null) {
            return hostUri.getHost();
        }

        return "";
    }

    public static <T> T initProvider(Context context, Class<T> targetClass) {
        return (T) ProviderFactory.getFactory().create(targetClass, context);
    }

    public static void init(Context context, Object target) {
        try {
            Field[] fields = target.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Class targetClass = field.getType();
                if (ModelProvider.class.isAssignableFrom(targetClass)) {
                    Object provider = ProviderFactory.getFactory().create(targetClass, context);
                    field.set(target, provider);
                }
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    public <T> RequestOption<T> buildSimpleGetRequest(
            RequestUrl requestUrl, TypeToken<T> typeToken) {
        ProviderListener<T> providerListener = new ProviderListener<T>() {
        };
        BaseVolleyRequest request = getVolleyRequest(Request.Method.GET, requestUrl, typeToken, providerListener, providerListener);
        return new RequestOption<T>(request, providerListener);
    }

    public <T> RequestOption<T> buildSimplePostRequest(
            RequestUrl requestUrl, TypeToken<T> typeToken) {
        ProviderListener<T> providerListener = new ProviderListener<T>() {
        };
        BaseVolleyRequest request = getVolleyRequest(Request.Method.POST, requestUrl, typeToken, providerListener, providerListener);
        return new RequestOption<T>(request, providerListener);
    }

    public class RequestOption<T> {

        private ProviderListener<T> mProviderListener;
        private BaseVolleyRequest mRquest;

        public RequestOption(BaseVolleyRequest request, ProviderListener<T> providerListener) {
            this.mRquest = request;
            this.mProviderListener = providerListener;
        }

        public BaseVolleyRequest getRequest() {
            return mRquest;
        }

        public ProviderListener<T> build() {
            mVolley.addToRequestQueue(mRquest);
            return mProviderListener;
        }
    }

    private <T> BaseVolleyRequest getVolleyRequest(
            int method, final RequestUrl requestUrl, final TypeToken<T> typeToken, Response.Listener<T> responseListener, final Response.ErrorListener errorListener
    ) {
        mVolley.getRequestQueue();
        BaseVolleyRequest request = new ModelVolleyRequest<T>(method, requestUrl, typeToken, responseListener, errorListener);
        request.setTag(requestUrl.url);
        return request;
    }

    public <T> void addRequest(
            RequestUrl requestUrl, final TypeToken<T> typeToken, Response.Listener<T> responseListener, Response.ErrorListener errorListener) {
        Request request = getVolleyRequest(Request.Method.GET, requestUrl, typeToken, responseListener, errorListener);
        mVolley.addToRequestQueue(request);
    }

    public <T> void addPostRequest(
            RequestUrl requestUrl, TypeToken<T> typeToken, Response.Listener<T> responseListener, Response.ErrorListener errorListener) {
        Request request = getVolleyRequest(Request.Method.POST, requestUrl, typeToken, responseListener, errorListener);
        mVolley.addToRequestQueue(request);
    }
}
