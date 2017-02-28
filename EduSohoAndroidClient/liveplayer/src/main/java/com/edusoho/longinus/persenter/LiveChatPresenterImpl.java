package com.edusoho.longinus.persenter;

import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.VolleyError;
import com.edusoho.kuozhi.imserver.IImServerAidlInterface;
import com.edusoho.kuozhi.imserver.entity.MessageEntity;
import com.edusoho.kuozhi.imserver.ui.IMessageListView;
import com.edusoho.kuozhi.v3.core.MessageEngine;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.longinus.data.LiveMessageBody;
import com.edusoho.longinus.data.LiveRoomProvider;
import com.edusoho.longinus.ui.ILiveVideoView;
import com.edusoho.longinus.util.LiveImClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by suju on 16/10/18.
 */
public class LiveChatPresenterImpl implements ILiveChatPresenter {

    private Bundle mLiveData;
    private Context mContext;
    private ILiveVideoView mILiveVideoView;
    private IMessageListView mIMessageListView;

    public LiveChatPresenterImpl(Context context, ILiveVideoView liveVideoView, Bundle liveData) {
        this.mContext = context;
        this.mLiveData = liveData;
        this.mILiveVideoView = liveVideoView;
    }

    @Override
    public void setView(IMessageListView view) {
        this.mIMessageListView = view;
    }

    @Override
    public void setUserCanChatStatus(MessageEntity message) {
        LiveMessageBody liveMessageBody = new LiveMessageBody(message.getMsg());
        try {
            JSONObject jsonObject = new JSONObject(liveMessageBody.getData());
            if (jsonObject.has("isAllCanChat")) {
                mIMessageListView.setEnable(jsonObject.optBoolean("isAllCanChat"));
                return;
            }
            if (jsonObject.has("isCanChat")) {
                String clientId = mLiveData.get("clientId").toString();
                if (AppUtil.parseInt(clientId) != jsonObject.optInt("clientId")) {
                    return;
                }
                mIMessageListView.setEnable(jsonObject.optBoolean("isCanChat"));
                return;
            }
        } catch (JSONException e) {
        }
    }

    @Override
    public void onReplace() {
        MessageEngine.getInstance().sendMsg(Const.TOKEN_LOSE, null);
        mIMessageListView.onUserKicked();
    }

    public void checkClientIsBan(final String clientId) {
        String roomNo = mLiveData.get("roomNo").toString();
        String token = mLiveData.get("token").toString();
        String liveHost = mLiveData.get("liveHost").toString();
        new LiveRoomProvider(mContext).getLiveChatBannedList(liveHost, token, roomNo)
                .success(new NormalCallback<ArrayList>() {
                    @Override
                    public void success(ArrayList bannedList) {
                        mIMessageListView.setEnable(!findClientIdInArray(clientId, bannedList));
                    }
                });
    }

    private boolean findClientIdInArray(String clientId, ArrayList<LinkedHashMap> bannedList) {
        for (LinkedHashMap bannedMap : bannedList) {
            if (clientId.equals(bannedMap.get("clientId").toString())) {
                return true;
            }
        }

        return false;
    }

    public void reConnectChatServer() {
        Log.d("LiveChatPresenter", "reConnectChatServer");
        LiveImClient.getIMClient(mContext).destory();
        NormalCallback<LinkedHashMap> callback = new NormalCallback<LinkedHashMap>() {
            @Override
            public void success(LinkedHashMap data) {
                if (data == null) {
                    return;
                }

                String joinToken = data.get("joinToken").toString();
                mLiveData.putString("joinToken", joinToken);
                String token = data.get("loginToken").toString();
                LinkedHashMap<String, String> servers = (LinkedHashMap<String, String>) data.get("servers");
                ArrayList<String> hostList = new ArrayList<>();
                for (String host : servers.values()) {
                    hostList.add(host + "?token=" + token);
                }

                LiveImClient liveImClient = LiveImClient.getIMClient(mContext);
                String clientId = mLiveData.get("clientId").toString();
                String clientName = mLiveData.get("clientName").toString();
                liveImClient.start(
                        AppUtil.parseInt(clientId), clientName, new ArrayList<String>(), hostList);
            }
        };
        getLiveChatServer(callback, null);
    }

    private void getLiveChatServer(NormalCallback<LinkedHashMap> callback, NormalCallback<VolleyError> errorNormalCallback) {
        String roomNo = mLiveData.get("roomNo").toString();
        String token = mLiveData.get("token").toString();
        String liveHost = mLiveData.get("liveHost").toString();
        new LiveRoomProvider(mContext)
                .getLiveChatServer(liveHost, roomNo, token)
                .success(callback)
                .fail(errorNormalCallback);
    }

    @Override
    public void connectLiveChatServer() {
        NormalCallback<LinkedHashMap> callback = new NormalCallback<LinkedHashMap>() {
            @Override
            public void success(LinkedHashMap data) {
                if (data == null) {
                    return;
                }

                String joinToken = data.get("joinToken").toString();
                mLiveData.putString("joinToken", joinToken);
                String token = data.get("loginToken").toString();
                LinkedHashMap<String, String> servers = (LinkedHashMap<String, String>) data.get("servers");
                ArrayList<String> hostList = new ArrayList<>();
                for (String host : servers.values()) {
                    hostList.add(host + "?token=" + token);
                }

                LiveImClient liveImClient = LiveImClient.getIMClient(mContext);
                liveImClient.setOnConnectedCallback(new LiveImClient.OnConnectedCallback() {
                    @Override
                    public void onConnected() {
                        mILiveVideoView.hideChatRoomLoadView();
                        mILiveVideoView.addChatRoomView();
                    }
                });

                String clientId = mLiveData.get("clientId").toString();
                String clientName = mLiveData.get("clientName").toString();
                liveImClient.start(
                        AppUtil.parseInt(clientId), clientName, new ArrayList<String>(), hostList);
            }
        };
        getLiveChatServer(callback, new NormalCallback<VolleyError>() {
            @Override
            public void success(VolleyError volleyError) {
                mILiveVideoView.showChatRoomLoadView("加载聊天讨论组失败");
            }
        });
    }

    @Override
    public void joinLiveChatRoom() {
        String joinToken = mLiveData.get("joinToken").toString();
        final String clientId = mLiveData.get("clientId").toString();
        if (!TextUtils.isEmpty(joinToken)) {
            checkClientIsBan(clientId);
            joinConversation(joinToken);
            return;
        }
        String roomNo = mLiveData.get("roomNo").toString();
        String token = mLiveData.get("token").toString();
        String liveHost = mLiveData.get("liveHost").toString();
        new LiveRoomProvider(mContext).getLiveChatServer(liveHost, token, roomNo).success(new NormalCallback<LinkedHashMap>() {
            @Override
            public void success(LinkedHashMap data) {
                String joinToken = null;
                if (data == null || TextUtils.isEmpty((joinToken = data.get("joinToken").toString()))) {
                    return;
                }
                checkClientIsBan(clientId);
                joinConversation(joinToken);
            }
        });
    }

    private void joinConversation(String joinToken) {
        try {
            String conversationNo = mLiveData.get("convNo").toString();
            IImServerAidlInterface aidlInterface = LiveImClient.getIMClient(mContext).getImBinder();
            if (aidlInterface == null) {
                return;
            }
            aidlInterface.joinConversation(joinToken, conversationNo);
        } catch (RemoteException e) {
            Log.i("joinLiveChatRoom", "join error");
        }
    }
}
