package com.edusoho.longinus.data;

import android.content.Context;

import com.edusoho.kuozhi.v3.model.live.Signal;
import com.edusoho.kuozhi.v3.model.provider.ModelProvider;
import com.edusoho.kuozhi.v3.model.provider.ProviderListener;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by howzhi on 15/9/9.
 */
public class LiveRoomProvider extends ModelProvider {

    public LiveRoomProvider(Context context) {
        super(context);
    }

    /*
        liveHost contain http://
     */
    public ProviderListener<LinkedHashMap> getLiveRoom(String liveHost, String token, String roomNo) {
        RequestUrl requestUrl = new RequestUrl(String.format("%s/rooms/%s/status", liveHost, roomNo));
        requestUrl.setHeads(new String[] {
                "Auth-Token", token
        });
        RequestOption requestOption = buildSimpleGetRequest(
                requestUrl, new TypeToken<LinkedHashMap>(){});

        return requestOption.build();
    }

    public ProviderListener<LinkedHashMap> getLasterLiveNotice(String liveHost, String token, String roomNo) {
        RequestUrl requestUrl = new RequestUrl(String.format("%s/rooms/%s/announcements/latest", liveHost, roomNo));
        requestUrl.setHeads(new String[] {
                "Auth-Token", token
        });
        RequestOption requestOption = buildSimpleGetRequest(
                requestUrl, new TypeToken<LinkedHashMap>(){});

        return requestOption.build();
    }

    public ProviderListener<ArrayList> getLiveNoticeList(String liveHost, String token, String roomNo) {
        RequestUrl requestUrl = new RequestUrl(String.format("%s/rooms/%s/announcements", liveHost, roomNo));
        requestUrl.setHeads(new String[] {
                "Auth-Token", token
        });
        RequestOption requestOption = buildSimpleGetRequest(
                requestUrl, new TypeToken<ArrayList>(){});

        return requestOption.build();
    }

    public ProviderListener<LinkedHashMap> getLiveServerTime(String liveHost, String token) {
        RequestUrl requestUrl = new RequestUrl(String.format("%s/timestamp", liveHost));
        requestUrl.setHeads(new String[] {
                "Auth-Token", token
        });
        RequestOption requestOption = buildSimpleGetRequest(
                requestUrl, new TypeToken<LinkedHashMap>(){});

        return requestOption.build();
    }

    public ProviderListener<LinkedHashMap<String, Signal>> getLiveSignals(
            String liveHost, String token, long startTime, long endTime) {
        RequestUrl requestUrl = new RequestUrl(String.format("%s/signals?startTime=%d&endTime=%d", liveHost, startTime, endTime));
        requestUrl.setHeads(new String[] {
                "Auth-Token", token
        });
        RequestOption requestOption = buildSimpleGetRequest(
                requestUrl, new TypeToken<LinkedHashMap<String, Signal>>(){});

        return requestOption.build();
    }

    /*
        roomNo      | string   | 是     | 教室NO               |
        | token           | string   | 是     | 用户token           |
        | role  | string   | 是     | 用户角色          |
        | clientId | string   | 是     | 用户ID
     */
    public ProviderListener<LinkedHashMap> getLiveChatServer(
            String liveHost, String roomNo, String token) {
        RequestUrl requestUrl = new RequestUrl(String.format("%s/rooms/%s/socket_token", liveHost, roomNo));

        requestUrl.getHeads().put("Auth-Token", token);
        RequestOption requestOption = buildSimpleGetRequest(
                requestUrl, new TypeToken<LinkedHashMap>(){});

        return requestOption.build();
    }

    public ProviderListener<ArrayList> getLiveChatBannedList(
            String liveHost, String token, String roomNo) {
        RequestUrl requestUrl = new RequestUrl(String.format("%s/rooms/%s/banned_clients", liveHost, roomNo));

        requestUrl.getHeads().put("Auth-Token", token);
        RequestOption requestOption = buildSimpleGetRequest(
                requestUrl, new TypeToken<ArrayList>(){});

        return requestOption.build();
    }

    public ProviderListener<LinkedHashMap> joinLiveChatRoom(
            String liveHost, String token, String roomNo) {

        RequestUrl requestUrl = new RequestUrl(String.format("%s/rooms/%s/socket_token", liveHost, roomNo));
        requestUrl.setHeads(new String[] {
                "Auth-Token", token
        });
        RequestOption requestOption = buildSimpleGetRequest(
                requestUrl, new TypeToken<LinkedHashMap>(){});

        return requestOption.build();
    }
}
