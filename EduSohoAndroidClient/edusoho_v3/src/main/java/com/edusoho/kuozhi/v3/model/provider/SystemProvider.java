package com.edusoho.kuozhi.v3.model.provider;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.android.volley.VolleyError;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.model.bal.SchoolApp;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.model.sys.School;
import com.edusoho.kuozhi.v3.model.sys.SchoolBanner;
import com.edusoho.kuozhi.v3.util.ApiTokenUtil;
import com.edusoho.kuozhi.v3.util.SchoolUtil;
import com.edusoho.kuozhi.v3.util.volley.BaseVolleyRequest;
import com.google.gson.reflect.TypeToken;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by howzhi on 15/9/24.
 */
public class SystemProvider extends ModelProvider {

    public SystemProvider(Context context) {
        super(context);
    }

    public ProviderListener<SchoolApp> getSchoolApp(RequestUrl requestUrl) {
        RequestOption requestOption = buildSimpleGetRequest(
                requestUrl, new TypeToken<SchoolApp>() {
                });

        requestOption.getRequest().setCacheUseMode(BaseVolleyRequest.ALWAYS_USE_CACHE);
        return requestOption.build();
    }

    public ProviderListener getSchoolBanners(RequestUrl requestUrl) {
        ProviderListener<List<SchoolBanner>> responseListener = new ProviderListener<List<SchoolBanner>>() {
        };
        addRequest(requestUrl, new TypeToken<List<SchoolBanner>>() {
        }, responseListener, responseListener);
        return responseListener;
    }

    public ProviderListener<LinkedHashMap> getIMSetting() {
        Map<String, String> tokenMap = ApiTokenUtil.getToken(mContext);
        String token = tokenMap.get("token");
        School school = SchoolUtil.getDefaultSchool(mContext);
        RequestUrl requestUrl = new RequestUrl(school.host + "/api/setting/app_im");
        requestUrl.getHeads().put("Auth-Token", token);

        RequestOption requestOption = buildSimpleGetRequest(
                requestUrl, new TypeToken<LinkedHashMap>(){});

        return requestOption.build();
    }

    public ProviderListener getImServerHosts(String[] ignoreServers) {
        Map<String, String> tokenMap = ApiTokenUtil.getToken(mContext);
        String token = tokenMap.get("token");
        School school = SchoolUtil.getDefaultSchool(mContext);
        RequestUrl requestUrl = new RequestUrl(school.host + "/api/im/me/login");
        HashMap<String, String> params = getPlatformInfo();
        params.put("tag", "mobile");
        if (ignoreServers != null) {
            params.put("ignoreServers", coverArray2String(ignoreServers));
        }
        requestUrl.setParams(params);
        requestUrl.getHeads().put("Auth-Token", token);

        final ProviderListener<LinkedHashMap> stringResponseListener = new ProviderListener<LinkedHashMap>() {
        };
        ProviderListener<LinkedHashMap> responseListener = new ProviderListener<LinkedHashMap>() {
        };

        responseListener.success(new NormalCallback<LinkedHashMap>() {
            @Override
            public void success(LinkedHashMap hashMap) {
                if (hashMap == null || !hashMap.containsKey("servers")) {
                    return;
                }
                LinkedHashMap hostList = (LinkedHashMap) hashMap.get("servers");
                stringResponseListener.onResponse(hostList);
            }
        }).fail(new NormalCallback<VolleyError>() {
            @Override
            public void success(VolleyError obj) {
                stringResponseListener.onErrorResponse(obj);
            }
        });

        addPostRequest(requestUrl, new TypeToken<LinkedHashMap>() {
        }, responseListener, responseListener);
        return stringResponseListener;
    }

    private String coverArray2String(String[] array) {
        String result = Arrays.toString(array);
        if (TextUtils.isEmpty(result)) {
            return "";
        }

        return result.substring(1, result.length() - 1);
    }

    public HashMap<String, String> getPlatformInfo() {
        HashMap<String, String> params = new HashMap<String, String>();
        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);

        String deviceId = telephonyManager.getDeviceId();
        if (TextUtils.isEmpty(deviceId)) {
            deviceId = Settings.System.getString(mContext.getContentResolver(), Settings.System.ANDROID_ID);
        }
        params.put("deviceToken", deviceId);
        params.put("desiceName", "Android " + Build.MODEL);
        params.put("deviceVersion", Build.VERSION.SDK);
        params.put("deviceKernel", Build.VERSION.RELEASE);

        return params;
    }

}
