package com.edusoho.kuozhi.v3.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.edusoho.kuozhi.v3.EdusohoApp;
import com.edusoho.kuozhi.v3.model.bal.push.Chat;
import com.edusoho.kuozhi.v3.model.bal.push.NewsCourseEntity;
import com.edusoho.kuozhi.v3.model.bal.push.OffLineMsgEntity;
import com.edusoho.kuozhi.v3.model.bal.push.V2CustomContent;
import com.edusoho.kuozhi.v3.model.result.UserResult;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.ui.DefaultPageActivity;
import com.edusoho.kuozhi.v3.ui.base.ActionBarBaseActivity;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.PushUtil;
import com.edusoho.kuozhi.v3.util.sql.ChatDataSource;
import com.edusoho.kuozhi.v3.util.sql.NewsCourseDataSource;
import com.edusoho.kuozhi.v3.util.sql.SqliteChatUtil;
import com.google.gson.reflect.TypeToken;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by howzhi on 14-8-13.
 */
public class EdusohoMainService extends Service {

    protected WeakReference<EdusohoApp> mAppWeakReference;
    public static final String TAG = "EdusohoMainService";
    private static EdusohoMainService mService;
    private WorkHandler mWorkHandler;
    private Queue<Request<String>> mAjaxQueue;

    public static final int LOGIN_WITH_TOKEN = 11;
    public static final int EXIT_USER = 12;

    @Override
    public void onCreate() {
        super.onCreate();
        mAjaxQueue = new LinkedList<>();
        mAppWeakReference = new WeakReference<>((EdusohoApp) getApplication());
        mService = this;
        mWorkHandler = new WorkHandler(this);
    }

    public void sendMessage(int type, Object obj) {
        Message message = mWorkHandler.obtainMessage(type);
        message.obj = obj;
        message.sendToTarget();
    }

    protected EdusohoApp getEduSohoApp() {
        return mAppWeakReference.get();
    }

    public Queue<Request<String>> getAjaxQueue() {
        return mAjaxQueue;
    }

    private void loginWithToken() {
        final EdusohoApp app = getEduSohoApp();
        if (TextUtils.isEmpty(app.token)) {
            //app.pushRegister(null);
            return;
        }
        synchronized (this) {
            if (!app.getNetIsConnect()) {
                app.loginUser = app.loadUserInfo();
                return;
            }

            if (!mAjaxQueue.isEmpty()) {
                return;
            }
            RequestUrl url = app.bindUrl(Const.CHECKTOKEN, true);
            Request<String> request = app.postUrl(url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        mAjaxQueue.poll();
                        UserResult result = app.gson.fromJson(
                                response, new TypeToken<UserResult>() {
                                }.getType());

                        Bundle bundle = new Bundle();
                        if (result != null && result.user != null && (!TextUtils.isEmpty(result.token))) {
                            app.saveToken(result);
                            app.sendMessage(Const.LOGIN_SUCCESS, null);
                            bundle.putString(Const.BIND_USER_ID, result.user.id + "");
                        } else {
                            bundle.putString(Const.BIND_USER_ID, "");
                            app.removeToken();
                            app.sendMsgToTarget(Const.SWITCH_TAB, null, DefaultPageActivity.class);
                        }
                        //app.pushRegister(bundle);

                    } catch (Exception e) {
                        Log.d(TAG, e.getMessage());
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });

            mAjaxQueue.offer(request);
        }
    }

    public static EdusohoMainService getService() {
        return mService;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public static void start(ActionBarBaseActivity activity) {
        activity.runService(TAG);
    }

    public static class WorkHandler extends Handler {
        WeakReference<EdusohoMainService> mWeakReference;
        EdusohoMainService mEdusohoMainService;

        public WorkHandler(EdusohoMainService service) {
            mWeakReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            //// TODO: 2016/5/14  
        }
    }

    public void getOfflineMsgs() {
    }

    public ArrayList<Chat> save2DB(ArrayList<OffLineMsgEntity> offLineMsgEntityArrayList) {
        final EdusohoApp app = getEduSohoApp();
        if (app == null) {
            return null;
        }
        ArrayList<Chat> chatArrayList = new ArrayList<>();
        ArrayList<NewsCourseEntity> courseArrayList = new ArrayList<>();
        for (OffLineMsgEntity offlineMsgModel : offLineMsgEntityArrayList) {
            switch (offlineMsgModel.getCustom().getFrom().getType()) {
                case PushUtil.ChatUserType.TEACHER:
                case PushUtil.ChatUserType.FRIEND:
                    chatArrayList.add(new Chat(offlineMsgModel));
                    break;
                case PushUtil.CourseType.TESTPAPER_REVIEWED:
                    courseArrayList.add(new NewsCourseEntity(offlineMsgModel));
                    break;
            }
        }
        if (chatArrayList.size() > 0) {
            final ChatDataSource chatDataSource = new ChatDataSource(SqliteChatUtil.getSqliteChatUtil(EdusohoMainService.this, app.domain));
            chatDataSource.create(chatArrayList);
        }
        if (courseArrayList.size() > 0) {
            final NewsCourseDataSource newsCourseDataSource = new NewsCourseDataSource(SqliteChatUtil.getSqliteChatUtil(EdusohoMainService.this, app.domain));
            newsCourseDataSource.create(courseArrayList);
        }
        return chatArrayList;
    }

    private HashMap<Integer, ArrayList<OffLineMsgEntity>> filterLatestChats(ArrayList<OffLineMsgEntity> latestChats) {
        int size = latestChats.size();
        HashMap<Integer, ArrayList<OffLineMsgEntity>> chatHashMaps = new HashMap<>();
        for (int i = 0; i < size; i++) {
            OffLineMsgEntity offlineMsgModel = latestChats.get(i);
            V2CustomContent v2CustomContent = offlineMsgModel.getCustom();
            if (v2CustomContent.getFrom() != null) {
                String fromType = v2CustomContent.getFrom().getType();
                String toType = v2CustomContent.getTo().getType();
                if ((PushUtil.ChatUserType.TEACHER.equals(fromType) || PushUtil.ChatUserType.FRIEND.equals(fromType)) &&
                        (PushUtil.ChatUserType.USER.equals(toType)) &&
                        TextUtils.isEmpty(v2CustomContent.getType())) {
                    int fromId = v2CustomContent.getFrom().getId();
                    if (chatHashMaps.containsKey(fromId)) {
                        chatHashMaps.get(fromId).add(offlineMsgModel);
                    } else {
                        ArrayList<OffLineMsgEntity> tmpLatestChat = new ArrayList<>();
                        tmpLatestChat.add(offlineMsgModel);
                        chatHashMaps.put(fromId, tmpLatestChat);
                    }
                }
            }
        }
        return chatHashMaps;
    }

    public void setNewNotification() {
        EdusohoApp app = getEduSohoApp();
        if (app == null) {
            return;
        }
        app.config.newVerifiedNotify = true;
        app.saveConfig();
    }
}
