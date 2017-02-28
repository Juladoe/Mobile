package com.edusoho.kuozhi.v3;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.edusoho.kuozhi.R;
import com.edusoho.kuozhi.imserver.IMClient;
import com.edusoho.kuozhi.v3.core.CoreEngine;
import com.edusoho.kuozhi.v3.core.MessageEngine;
import com.edusoho.kuozhi.v3.factory.FactoryManager;
import com.edusoho.kuozhi.v3.factory.provider.AppSettingProvider;
import com.edusoho.kuozhi.v3.listener.CoreEngineMsgCallback;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.listener.RequestParamsCallback;
import com.edusoho.kuozhi.v3.model.bal.User;
import com.edusoho.kuozhi.v3.model.bal.UserRole;
import com.edusoho.kuozhi.v3.model.provider.IMServiceProvider;
import com.edusoho.kuozhi.v3.model.result.UserResult;
import com.edusoho.kuozhi.v3.model.sys.AppConfig;
import com.edusoho.kuozhi.v3.model.sys.MessageType;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.model.sys.School;
import com.edusoho.kuozhi.v3.model.sys.Token;
import com.edusoho.kuozhi.v3.service.DownLoadService;
import com.edusoho.kuozhi.v3.service.EdusohoMainService;
import com.edusoho.kuozhi.v3.service.M3U8DownService;
import com.edusoho.kuozhi.v3.ui.base.BaseActivity;
import com.edusoho.kuozhi.v3.util.ApiTokenUtil;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.CommonUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.MultipartRequest;
import com.edusoho.kuozhi.v3.util.PushUtil;
import com.edusoho.kuozhi.v3.util.RequestUtil;
import com.edusoho.kuozhi.v3.util.SchoolUtil;
import com.edusoho.kuozhi.v3.util.VolleySingleton;
import com.edusoho.kuozhi.v3.util.json.GsonEnumTypeAdapter;
import com.edusoho.kuozhi.v3.util.server.CacheServer;
import com.edusoho.kuozhi.v3.util.sql.SqliteUtil;
import com.edusoho.kuozhi.v3.util.volley.StringVolleyRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 全局
 */
public class EdusohoApp extends Application {

    public AppConfig config;
    public String host;
    public String domain;
    public Gson gson;
    public School defaultSchool;//默认学校
    public User loginUser;
    public String apiVersion;
    public String schoolVersion;
    public String schoolHost = "";
    public CoreEngine mEngine;

    public Activity mActivity;
    public Context mContext;

    public String token;
    /**
     * school token
     */
    public String apiToken;

    private HashMap<String, Bundle> notifyMap;
    public static HashMap<String, Activity> runTask;
    private static final String TAG = "EdusohoApp";

    public static int screenW;
    public static int screenH;

    public static EdusohoApp app;
    public static boolean debug = true;
    public static final String PLUGIN_CONFIG = "plugin_config";
    public static final String INSTALL_PLUGIN = "install_plugin";

    private ImageLoaderConfiguration mImageLoaderConfiguration;
    public DisplayImageOptions mOptions;
    public DisplayImageOptions mAvatarOptions;

    //cache 缓存服务器
    private CacheServer mResouceCacheServer;
    private CacheServer mPlayCacheServer;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "create application");
        init();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    public static void log(String msg) {
        Log.d(TAG, msg);
    }

    public EdusohoMainService getService() {
        return EdusohoMainService.getService();
    }

    /**
     * @param requestUrl
     * @param responseListener
     * @param errorListener
     * @return
     */
    public Request<String> postMultiUrl(final RequestUrl requestUrl, Response.Listener<String> responseListener, Response.ErrorListener errorListener, int method) {
        VolleySingleton.getInstance(this).getRequestQueue();
        MultipartRequest multipartRequest = new MultipartRequest(method, requestUrl, responseListener, errorListener) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return requestUrl.getHeads();
            }
        };
        multipartRequest.setTag(requestUrl.url);
        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(Const.TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        return VolleySingleton.getInstance(this).addToRequestQueue(multipartRequest);
    }

    private StringVolleyRequest processorStringVolleyRequest(
            final RequestUrl requestUrl,
            final Response.Listener<String> responseListener,
            final Response.ErrorListener errorListener,
            int method
    ) {
        return new StringVolleyRequest(method, requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    response = RequestUtil.handleRequestError(response);
                } catch (RequestUtil.RequestErrorException re) {
                }
                responseListener.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof NoConnectionError) {
                    errorListener.onErrorResponse(error);
                    return;
                }
                if (error instanceof TimeoutError) {
                    errorListener.onErrorResponse(error);
                    return;
                }
                if (error.networkResponse == null) {
                    return;
                }
                try {
                    if (TextUtils.isEmpty(RequestUtil.handleRequestError(error.networkResponse.data))) {
                        return;
                    }
                } catch (RequestUtil.RequestErrorException re) {
                }

                if (errorListener == null) {
                    return;
                }
                errorListener.onErrorResponse(error);
            }
        });
    }

    public Request<String> postUrl(final RequestUrl requestUrl, final Response.Listener<String> responseListener, final Response.ErrorListener errorListener) {
        VolleySingleton.getInstance(this).getRequestQueue();
        StringVolleyRequest request = processorStringVolleyRequest(requestUrl, responseListener, errorListener, Request.Method.POST);
        request.setCacheMode(StringVolleyRequest.CACHE_AUTO);
        request.setTag(requestUrl.url);
        return VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    /**
     * volley get 请求
     *
     * @param requestUrl       url、参数、header等信息
     * @param responseListener 返回response信息
     * @param errorListener    错误信息
     */
    public void getUrl(final RequestUrl requestUrl, Response.Listener<String> responseListener, Response.ErrorListener errorListener) {
        VolleySingleton.getInstance(this).getRequestQueue();
        StringVolleyRequest request = processorStringVolleyRequest(requestUrl, responseListener, errorListener, Request.Method.GET);
        request.setCacheMode(StringVolleyRequest.CACHE_AUTO);
        request.setTag(requestUrl.url);
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    public void getUrlWithCache(final RequestUrl requestUrl, Response.Listener<String> responseListener, Response.ErrorListener errorListener) {
        VolleySingleton.getInstance(this).getRequestQueue();
        StringVolleyRequest request = processorStringVolleyRequest(requestUrl, responseListener, errorListener, Request.Method.GET);
        request.setCacheMode(StringVolleyRequest.CACHE_ALWAYS);
        request.setTag(requestUrl.url);
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    public void addMessageListener(String msgId, CoreEngineMsgCallback callback) {
        mEngine.receiveMsg(msgId, callback);
    }

    public void registMsgSource(MessageEngine.MessageCallback messageCallback) {
        mEngine.registMsgSrc(messageCallback);
    }

    public void unRegistMsgSource(MessageEngine.MessageCallback messageCallback) {
        mEngine.unRegistMessageSource(messageCallback);
    }

    public void unRegistPubMsg(MessageType messageType, MessageEngine.MessageCallback messageCallback) {
        mEngine.unRegistPubMessage(messageType, messageCallback);
    }

    public Map<String, MessageEngine.MessageCallback> getSourceMap() {
        return mEngine.getMessageEngine().getSourceMap();
    }

    public void delMessageListener(String msgId) {
        mEngine.removeMsg(msgId);
    }

    public void sendMessage(String msgId, Bundle bundle) {
        mEngine.getMessageEngine().sendMsg(msgId, bundle);
    }

    public void sendMsgToTarget(int msgType, Bundle body, Object target) {
        mEngine.getMessageEngine().sendMsgToTaget(msgType, body, target);
    }

    public void sendMsgToTargetForCallback(
            int msgType, Bundle body, Class target, NormalCallback callback) {
        mEngine.getMessageEngine().sendMsgToTagetForCallback(msgType, body, target, callback);
    }

    public void exit() {
        stopService(DownLoadService.getIntent(this));
        notifyMap.clear();
        if (mResouceCacheServer != null) {
            mResouceCacheServer.close();
            mResouceCacheServer = null;
        }

        stopPlayCacheServer();

        M3U8DownService m3U8DownService = M3U8DownService.getService();
        if (m3U8DownService != null) {
            m3U8DownService.cancelAllDownloadTask();
        }

        SqliteUtil.getUtil(this).close();
    }

    private Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(UserRole.class, new GsonEnumTypeAdapter<>(UserRole.NO_SUPPORT))
                .create();
    }

    private void init() {
        app = this;
        gson = createGson();
        apiVersion = getString(R.string.api_version);
        setHost(getString(R.string.app_host));
        notifyMap = new HashMap<>();
        initApp();

        IMClient.getClient().init(this);
        if (!TextUtils.isEmpty(app.token)) {
            Log.d(TAG, "bindImServerHost");
            bindImServerHost();
        }
    }

    protected AppSettingProvider getAppSettingProvider() {
        return FactoryManager.getInstance().create(AppSettingProvider.class);
    }

    private void bindImServerHost() {
        User user = getAppSettingProvider().getCurrentUser();
        if (user != null) {
            new IMServiceProvider(getBaseContext()).bindServer(user.id, user.nickname);
        }
    }

    private String getDomain() {
        Uri hostUri = Uri.parse(app.host);
        if (hostUri != null) {
            return hostUri.getHost();
        }
        return "";
    }

    public void initApp() {
        runTask = new HashMap<>();
        FactoryManager.getInstance().initContext(getBaseContext());
        File workFile = initWorkSpace();
        initImageLoaderConfig(workFile);
        loadConfig();

        mEngine = CoreEngine.create(this);
    }

    public void startMainService() {
        app.mEngine.runService(EdusohoMainService.TAG, this, null);
    }

    private void installPlugin() {
        final SharedPreferences sp = getSharedPreferences(PLUGIN_CONFIG, MODE_APPEND);
        if (sp.contains(INSTALL_PLUGIN)) {
            return;
        }
        new android.os.Handler().post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "installPlugin");
                mEngine.installApkPlugin();
                sp.edit().putBoolean(INSTALL_PLUGIN, true).commit();
            }
        });
    }

    protected void initImageLoaderConfig(File file) {
        if (file == null || !file.exists()) {
            file = new File(getCacheDir(), getResources().getString(R.string.image_cache_path));
        } else {
            file = new File(file, getResources().getString(R.string.image_cache_path));
        }
        mImageLoaderConfiguration = new ImageLoaderConfiguration
                .Builder(this)
                .memoryCacheExtraOptions((int) (screenW * 0.8f), (int) (screenH * 0.8f))
                .diskCache(new UnlimitedDiscCache(file)).imageDownloader(new BaseImageDownloader(this, Const.TIMEOUT, Const.TIMEOUT))
                .build();
        ImageLoader.getInstance().init(mImageLoaderConfiguration);
        mOptions = new DisplayImageOptions.Builder().cacheOnDisk(true).showImageForEmptyUri(R.drawable.default_course).
                showImageOnFail(R.drawable.default_course).build();
        mAvatarOptions = new DisplayImageOptions.Builder().cacheOnDisk(true).showImageForEmptyUri(R.drawable.icon_default_avatar).
                showImageOnFail(R.drawable.icon_default_avatar).build();
    }

    public HashMap<String, String> getPlatformInfo() {
        HashMap<String, String> params = new HashMap<String, String>();
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);

        params.put("deviceSn", telephonyManager.getDeviceId());
        params.put("platform", "Android " + Build.MODEL);
        params.put("version", Build.VERSION.SDK);
        params.put("screenresolution", displayMetrics.widthPixels + "x" + displayMetrics.heightPixels);
        params.put("kernel", Build.VERSION.RELEASE);
        params.put("edusohoVersion", apiVersion);

        return params;
    }

    public void registDevice(final NormalCallback normalCallback) {
        HashMap<String, String> params = getPlatformInfo();
        RequestUrl requestUrl = new RequestUrl(app.schoolHost + Const.REGIST_DEVICE);
        requestUrl.setParams(params);
        app.postUrl(requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(null, "regist device to school");
                try {
                    Boolean result = app.gson.fromJson(
                            response, new TypeToken<Boolean>() {
                            }.getType()
                    );

                    if (result) {
                        app.saveConfig();
                    }

                    if (normalCallback != null) {
                        normalCallback.success(null);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "registDevice error");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "regist failed");
            }
        });
    }

    public boolean getNetIsConnect() {
        ConnectivityManager connManager = (ConnectivityManager)
                getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable();
    }

    public boolean getNetIsWiFi() {
        ConnectivityManager connManager = (ConnectivityManager)
                getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return networkInfo != null && networkInfo.isAvailable();
    }

    public String getPluginFile(String pluginName) {
        File file = mEngine.getPluginFile(pluginName);
        return file.getAbsolutePath();
    }

    public void installApk(String file) {
        if (file == null || "".equals(file)) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);

        intent.setDataAndType(Uri.parse("file://" + file),
                "application/vnd.android.package-archive");
        this.startActivity(intent);
    }

    public String getApkVersion() {
        String version = "0.0.0";
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = packageInfo.versionName;
        } catch (Exception e) {
            Log.d(TAG, "get apk version error");
        }
        return version;
    }

    public int getApkVersionCode() {
        int version = 0;
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = packageInfo.versionCode;
        } catch (Exception e) {
            Log.d(TAG, "get apk code error");
        }
        return version;
    }

    public void setCurrentSchool(School school) {
        app.defaultSchool = school;
        app.schoolHost = school.url + "/";
        setHost(school.host);
        SchoolUtil.saveSchool(getBaseContext(), school);
        getAppSettingProvider().setCurrentSchool(school);
    }

    private void loadDefaultSchool() {
        School item = SchoolUtil.getDefaultSchool(getBaseContext());
        if (item == null) {
            return;
        }
        setHost(item.host);
        setCurrentSchool(item);
    }

    private void setHost(String host) {
        this.host = host;
        this.domain = getDomain();
    }

    public User loadUserInfo() {
        Map<String, ?> tokenMap = ApiTokenUtil.getToken(getBaseContext());
        String strUser = tokenMap.containsKey("userInfo") ? tokenMap.get("userInfo").toString() : null;
        User user = null;
        if (!TextUtils.isEmpty(strUser)) {
            user = parseJsonValue(AppUtil.encode2(strUser), new TypeToken<User>() {
            });
        }
        return user;
    }

    private void loadToken() {
        Map<String, String> tokenMap = ApiTokenUtil.getToken(getBaseContext());
        if (tokenMap.isEmpty()) {
            return;
        }
        token = tokenMap.get("token");
        apiToken = tokenMap.get("apiToken");
    }

    public void saveApiToken(String apiToken) {
        ApiTokenUtil.saveApiToken(getBaseContext(), apiToken);
        this.apiToken = apiToken;
    }

    public void saveToken(UserResult userResult) {
        ApiTokenUtil.saveToken(getBaseContext(), userResult);

        token = userResult.token == null || "".equals(userResult.token) ? "" : userResult.token;
        if (TextUtils.isEmpty(token)) {
            loginUser = null;
        } else {
            loginUser = userResult.user;
            SqliteUtil.saveUser(loginUser);
        }
        getAppSettingProvider().setUser(userResult.user);
    }

    public void removeToken() {
        ApiTokenUtil.removeToken(getBaseContext());

        SqliteUtil.clearUser(loginUser == null ? 0 : loginUser.id);
        token = null;
        loginUser = null;

        EdusohoMainService mService = getService();
        if (mService == null) {
            return;
        }
        mService.sendMessage(EdusohoMainService.EXIT_USER, null);
    }

    public boolean taskIsRun(String name) {
        Activity activity = runTask.get(name);
        return activity != null;
    }

    public void addTask(String name, Activity activity) {
        Activity oldActivity = runTask.get(name);
        runTask.put(name, activity);
        if (oldActivity != null) {
            Log.d(null, "remove activity->" + name);
            oldActivity.finish();
            runTask.remove(name);
        }
    }

    private void loadConfig() {
        SharedPreferences sp = getSharedPreferences("config", MODE_APPEND);
        config = new AppConfig();
        config.showSplash = sp.getBoolean("showSplash", true);
        config.isPublicRegistDevice = sp.getBoolean("registPublicDevice", false);
        config.startWithSchool = sp.getBoolean("startWithSchool", true);
        config.offlineType = sp.getInt("offlineType", 0);
        config.newVerifiedNotify = sp.getBoolean("newVerifiedNotify", false);
        config.msgSound = sp.getInt("msgSound", 1);
        config.msgVibrate = sp.getInt("msgVibrate", 2);
        if (config.startWithSchool) {
            loadDefaultSchool();
        }

        loadToken();
        loginUser = loadUserInfo();
    }

    public int getMsgDisturbFromCourseId(int fromId) {
        SharedPreferences sp = getSharedPreferences("msgDisturbConfig_" + loginUser.id, MODE_APPEND);
        return sp.getInt(fromId + "", 0);
    }

    public void saveMsgDisturbConfig(int fromId, int param) {
        SharedPreferences sp = getSharedPreferences("msgDisturbConfig_" + loginUser.id, MODE_APPEND);
        SharedPreferences.Editor edit = sp.edit();
        edit.putInt(fromId + "", param);
        edit.apply();
    }

    public void saveConfig() {//使用sp保存一些配置
        SharedPreferences sp = getSharedPreferences("config", MODE_APPEND);
        SharedPreferences.Editor edit = sp.edit();
        edit.putBoolean("showSplash", config.showSplash);
        edit.putBoolean("registPublicDevice", config.isPublicRegistDevice);
        edit.putBoolean("startWithSchool", config.startWithSchool);
        edit.putInt("offlineType", config.offlineType);
        edit.putInt("msgSound", config.msgSound);
        edit.putInt("msgVibrate", config.msgVibrate);
        edit.putBoolean("newVerifiedNotify", config.newVerifiedNotify);
        edit.apply();
    }

    private File initWorkSpace() {
        File workSpace = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            workSpace = new File(Environment.getExternalStorageDirectory(), "edusoho");
            if (!workSpace.exists()) {
                workSpace.mkdir();
            }
        } else {
            CommonUtil.longToast(getApplicationContext(), "设备没有内存卡,数据将保存在手机内存中！");
        }
        return workSpace;
    }

    public static File getWorkSpace() {
        File file = new File(Environment.getExternalStorageDirectory(), "/edusoho");
        return file;
    }

    public static File getChatCacheFile() {
        return app.getExternalCacheDir();
    }

    public void setDisplay(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        this.screenH = display.getHeight();
        this.screenW = display.getWidth();
    }

    public String bindToken2Url(String url, boolean addToken) {
        StringBuffer sb = new StringBuffer(app.schoolHost);
        sb.append(url);
        if (addToken) {
            sb.append("&token=").append(token);
        }
        return sb.toString();
    }

    public HashMap<String, String> initParams(String[] arges) {
        HashMap<String, String> params = new HashMap<String, String>();
        for (int i = 0; i < arges.length; i = i + 2) {
            params.put(arges[i], arges[i + 1]);
        }

        return params;
    }

    public HashMap<String, String> createParams(
            boolean addToken, RequestParamsCallback callback) {
        HashMap<String, String> params = new HashMap<String, String>();
        if (callback != null) {
            callback.addParams(params);
        }

        if (addToken) {
            params.put("token", token);
        }
        return params;
    }

    public RequestUrl bindUrl(String url, boolean addToken) {
        StringBuffer sb = new StringBuffer(app.schoolHost);
        sb.append(url);
        RequestUrl requestUrl = new RequestUrl(sb.toString());

        if (addToken) {
            requestUrl.heads.put("token", token);
        }
        return requestUrl;
    }

    public RequestUrl bindNewUrl(String url, boolean addToken) {
        StringBuffer sb = new StringBuffer(app.host);
        sb.append(url);
        RequestUrl requestUrl = new RequestUrl(sb.toString());

        if (addToken) {
            requestUrl.heads.put("Auth-Token", token);
        }
        return requestUrl;
    }

    public RequestUrl bindNewApiUrl(String url, boolean addToken) {
        RequestUrl requestUrl = new RequestUrl(app.host + url);
        if (addToken) {
            requestUrl.heads.put("X-Auth-Token", token);
        }
        return requestUrl;
    }

    public RequestUrl bindNewHostUrl(String url, boolean addToken) {
        RequestUrl requestUrl = new RequestUrl(url);
        if (addToken) {
            requestUrl.heads.put("X-Auth-Token", token);
        }
        return requestUrl;
    }

    public RequestUrl bindPushUrl(String url) {
        StringBuffer sb = new StringBuffer(Const.PUSH_HOST);
        sb.append(url);
        RequestUrl requestUrl = new RequestUrl(sb.toString());
        requestUrl.heads.put("Auth-Token", app.apiToken);
        return requestUrl;
    }

    private void bindPushUrl(String url, final NormalCallback<RequestUrl> normalCallback) {
        final StringBuffer sb = new StringBuffer(Const.PUSH_HOST);
        sb.append(url);
        if (TextUtils.isEmpty(app.apiToken)) {
            final RequestUrl requestUrl = app.bindNewUrl(Const.GET_API_TOKEN, false);
            app.getUrl(requestUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Token token = parseJsonValue(response, new TypeToken<Token>() {
                    });
                    if (token != null) {
                        RequestUrl requestUrl = new RequestUrl(sb.toString());
                        app.saveApiToken(token.token);
                        requestUrl.heads.put("Auth-Token", app.apiToken);
                        normalCallback.success(requestUrl);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "无法获取网校Token");
                }
            });
        } else {
            RequestUrl requestUrl = new RequestUrl(sb.toString());
            requestUrl.heads.put("Auth-Token", app.apiToken);
            normalCallback.success(requestUrl);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    public void startUpdateWebView(String url) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri content_url = Uri.parse(url);
        intent.setData(content_url);
        startActivity(intent);
    }


    public void addNotify(String type, Bundle bundle) {
        notifyMap.put(type, bundle);
    }

    public Set<String> getNotifys() {
        return notifyMap.keySet();
    }

    public void removeNotify(String type) {
        notifyMap.remove(type);
    }


    /**
     * 启动播放器缓存server
     *
     * @param activity
     * @return
     */
    public CacheServer startPlayCacheServer(BaseActivity activity) {
        if (mPlayCacheServer == null) {
            mPlayCacheServer = new CacheServer(activity, Const.CACHE_PROT);
            mPlayCacheServer.start();
        }

        return mPlayCacheServer;
    }

    public void pausePlayCacheServer() {
        if (mPlayCacheServer != null) {
            mPlayCacheServer.pause();
        }
    }

    public void resumePlayCacheServer() {
        if (mPlayCacheServer != null) {
            mPlayCacheServer.keepOn();
        }
    }

    public void stopPlayCacheServer() {
        if (mPlayCacheServer != null) {
            mPlayCacheServer.close();
            mPlayCacheServer = null;
        }
    }

    public <T> T parseJsonValue(String json, TypeToken<T> typeToken) {
        T value = null;
        try {
            value = gson.fromJson(
                    json, typeToken.getType());
        } catch (Exception e) {
            Log.d(TAG, "parse error");
        }

        return value;
    }

    /**
     * 判断是否为当前Activity
     *
     * @param activityName ActivityName
     * @return
     */
    public boolean isForeground(String activityName) {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> task = manager.getRunningTasks(1);
        ComponentName componentInfo = task.get(0).topActivity;
        return componentInfo.getClassName().equals(activityName);
    }

    public String getCurrentUserRole() {
        if (loginUser == null || loginUser.roles == null) {
            return "";
        }

        UserRole[] userRoles = app.loginUser.roles;
        if (TextUtils.isEmpty(loginUser.role)) {
            String[] roles = new String[userRoles.length];
            for (int i = 0; i < userRoles.length; i++) {
                UserRole role = userRoles[i];
                if (role != null) {
                    roles[i] = role.toString();
                }
            }
            if (CommonUtil.inArray(UserRole.ROLE_TEACHER.name(), roles)) {
                loginUser.role = PushUtil.ChatUserType.TEACHER;
            } else {
                loginUser.role = PushUtil.ChatUserType.FRIEND;
            }
            return loginUser.role;
        }
        return loginUser.role;
    }
}
