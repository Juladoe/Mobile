package com.edusoho.longinus.persenter;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.android.volley.VolleyError;
import com.edusoho.kuozhi.imserver.entity.MessageEntity;
import com.edusoho.kuozhi.imserver.ui.IMessageListView;
import com.edusoho.kuozhi.v3.listener.NormalCallback;
import com.edusoho.kuozhi.v3.listener.PromiseCallback;
import com.edusoho.kuozhi.v3.model.live.Signal;
import com.edusoho.kuozhi.v3.util.AppUtil;
import com.edusoho.kuozhi.v3.util.Promise;
import com.edusoho.longinus.data.LiveMessageBody;
import com.edusoho.longinus.data.LiveRoomProvider;
import com.edusoho.longinus.ui.ILiveVideoView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by suju on 16/10/18.
 */
public class LiveVideoPresenterImpl implements ILiveVideoPresenter {

    private static final int FADE_OUT = 0x02;

    private Bundle mLiveData;
    private Context mContext;
    private ILiveVideoView mILiveVideoView;
    private IMessageListView mIMessageListView;
    private Map<Long, Boolean> mNoticeCacheMap;

    public LiveVideoPresenterImpl(Context context, Bundle liveData, ILiveVideoView videoView, IMessageListView messageListView) {
        this.mContext = context;
        this.mLiveData = liveData;
        this.mILiveVideoView = videoView;
        this.mIMessageListView = messageListView;
        this.mNoticeCacheMap = new HashMap<>();
    }

    private Promise getServerTime() {
        final Promise promise = new Promise();
        String token = mLiveData.get("token").toString();
        String liveHost = mLiveData.get("liveHost").toString();
        new LiveRoomProvider(mContext).getLiveServerTime(liveHost, token)
        .success(new NormalCallback<LinkedHashMap>() {
            @Override
            public void success(LinkedHashMap data) {
                Double time = (Double) data.get("time");
                promise.resolve(time.longValue());
            }
        }).fail(new NormalCallback<VolleyError>() {
            @Override
            public void success(VolleyError obj) {
                promise.resolve((long)0);
            }
        });

        return promise;
    }

    @Override
    public void setChatRoomNetWorkStatus(int status) {
        mILiveVideoView.setRoomPrepareStatus(status);
    }

    @Override
    public void handleHistorySignals() {
        //no used
    }

    @Override
    public void onKill(MessageEntity messageEntity) {
        LiveMessageBody liveMessageBody = new LiveMessageBody(messageEntity.getMsg());
        if (liveMessageBody == null) {
            return;
        }
        try {
            String clientId = mLiveData.get("clientId").toString();
            JSONObject jsonObject = new JSONObject(liveMessageBody.getData());
            if (!TextUtils.isEmpty(clientId) && AppUtil.parseInt(clientId) == jsonObject.optInt("clientId")) {
                mILiveVideoView.onLeaveRoom();
                updateLivePlayStatus(messageEntity);
            }
        } catch (JSONException e) {
        }
    }

    @Override
    public void updateLiveNotice(final boolean alawsShow) {
        String token = mLiveData.get("token").toString();
        String liveHost = mLiveData.get("liveHost").toString();
        String roomNo = mLiveData.get("roomNo").toString();
        new LiveRoomProvider(mContext).getLasterLiveNotice(liveHost, token, roomNo)
        .success(new NormalCallback<LinkedHashMap>() {
            @Override
            public void success(LinkedHashMap notice) {
                if (notice == null || !notice.containsKey("content")) {
                    return;
                }
                long time = AppUtil.convertTimeZone2Millisecond(notice.get("time").toString());
                if (!alawsShow && notice.containsKey("time")) {
                    if (mNoticeCacheMap.containsKey(time)) {
                        return;
                    }
                    mNoticeCacheMap.put(time, true);
                    showNotice(notice.get("content").toString(), time);
                    return;
                }
                showNotice(notice.get("content").toString(), time);
            }
        });
    }

    private void showNotice(String content, long time) {
        mILiveVideoView.setNotice(content);
        mILiveVideoView.showNoticeView();
        autoHideNoticeView();
        mNoticeCacheMap.put(time, true);
    }

    private void invokeSignals(LinkedHashMap<String, Signal> signalMap) {
        Map<String, Boolean> typeFilterMap = new HashMap<>();
        List<String> keyArray = new ArrayList<>(signalMap.keySet());
        Collections.sort(keyArray, new Comparator<String>() {
            @Override
            public int compare(String s, String s2) {
                return (int) (AppUtil.parseLong(s2) - AppUtil.parseLong(s));
            }
        });
        for (String key : keyArray) {
            Signal signal = signalMap.get(key);
            if (typeFilterMap.containsKey(signal.getType())) {
                continue;
            }
            typeFilterMap.put(signal.getType(), true);
            switch (signal.getType()) {
                case "101002":
                    Map data = signal.getData();
                    String status = Boolean.TRUE.equals(data.get("isResting")) ? "pause" : "start";
                    mILiveVideoView.setLivePlayStatus(status);
                    break;
                case "103005":
                    Map allCanChatData = signal.getData();
                    mIMessageListView.setEnable(Boolean.TRUE.equals(allCanChatData.get("isAllCanChat")));
                    break;
                case "103004":
                    Map canChatData = signal.getData();
                    mIMessageListView.setEnable(Boolean.TRUE.equals(canChatData.get("isCanChat")));
                    break;
            }
        }
    }

    @Override
    public void updateLivePlayStatus(MessageEntity messageEntity) {
        if ("replace".equals(messageEntity.getCmd())) {
            mILiveVideoView.setLivePlayStatus("pause");
            return;
        }
        if ("103010".equals(messageEntity.getCmd())) {
            mILiveVideoView.checkLivePlayStatus();
            return;
        }

        if ("103007".equals(messageEntity.getCmd())) {
            mILiveVideoView.setLivePlayStatus("close");
            return;
        }

        LiveMessageBody liveMessageBody = new LiveMessageBody(messageEntity.getMsg());
        try {
            JSONObject jsonObject = new JSONObject(liveMessageBody.getData());
            mILiveVideoView.setLivePlayStatus(jsonObject.getString("status"));
        } catch (JSONException e) {
        }
    }

    @Override
    public void updateNotice(MessageEntity message) {
        LiveMessageBody liveMessageBody = new LiveMessageBody(message.getMsg());
        try {
            JSONObject jsonObject = new JSONObject(liveMessageBody.getData());
            showNotice(jsonObject.optString("info"), liveMessageBody.getTime() / 1000);
        } catch (JSONException e) {
        }
    }

    private void autoHideNoticeView() {
        mHandler.removeMessages(FADE_OUT);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(FADE_OUT), 1000 * 60);
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FADE_OUT:
                    mILiveVideoView.hideNoticeView();
            }
        }
    };
}
