package com.edusoho.kuozhi.v3.model.provider;

import android.content.Context;
import com.edusoho.kuozhi.v3.model.bal.FollowerNotificationResult;
import com.edusoho.kuozhi.v3.model.bal.SchoolApp;
import com.edusoho.kuozhi.v3.model.bal.SearchFriendResult;
import com.edusoho.kuozhi.v3.model.result.FollowResult;
import com.edusoho.kuozhi.v3.model.result.FriendResult;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.volley.BaseVolleyRequest;
import com.google.gson.reflect.TypeToken;
import java.util.List;

/**
 * Created by howzhi on 15/8/24.
 */
public class FriendProvider extends ModelProvider {

    public FriendProvider(Context context) {
        super(context);
    }

    public ProviderListener<List<SchoolApp>> getSchoolApps() {
        RequestUrl requestUrl = new RequestUrl(getHost() + Const.SCHOOL_APPS);
        requestUrl.getHeads().put("Auth-Token", getToken());

        RequestOption requestOption = buildSimpleGetRequest(
                requestUrl, new TypeToken<List<SchoolApp>>(){});

        requestOption.getRequest().setCacheUseMode(BaseVolleyRequest.AUTO_USE_CACHE);
        return requestOption.build();
    }

    public ProviderListener getFriend(RequestUrl requestUrl) {
        ProviderListener<FriendResult> responseListener = new ProviderListener<FriendResult>() {
        };
        addRequest(requestUrl, new TypeToken<FriendResult>() {
        }, responseListener, responseListener);
        return responseListener;
    }

    public ProviderListener<FriendResult> getFriendList() {
        RequestUrl requestUrl = new RequestUrl(getHost() + Const.MY_FRIEND + "?start=0&limit=10000/");
        requestUrl.getHeads().put("Auth-Token", getToken());

        RequestOption requestOption = buildSimpleGetRequest(
                requestUrl, new TypeToken<FriendResult>(){});

        requestOption.getRequest().setCacheUseMode(BaseVolleyRequest.AUTO_USE_CACHE);
        return requestOption.build();
    }

    public ProviderListener loadNotifications(RequestUrl requestUrl) {
        ProviderListener<FollowerNotificationResult> responseListener = new ProviderListener<FollowerNotificationResult>() {
        };
        addRequest(requestUrl, new TypeToken<FollowerNotificationResult>() {
        }, responseListener, responseListener);
        return responseListener;
    }

    public ProviderListener loadRelationships(RequestUrl requestUrl) {
        ProviderListener<String[]> responseListener = new ProviderListener<String[]>() {
        };
        addRequest(requestUrl, new TypeToken<String[]>() {
        }, responseListener, responseListener);
        return responseListener;
    }

    public ProviderListener followUsers(RequestUrl requestUrl){
        ProviderListener<FollowResult> responseListener = new ProviderListener<FollowResult>() {
        };
        addPostRequest(requestUrl, new TypeToken<FollowResult>() {
        }, responseListener, responseListener);
        return responseListener;
    }

    public ProviderListener getSearchFriend(RequestUrl requestUrl) {
        ProviderListener<SearchFriendResult> responseListener = new ProviderListener<SearchFriendResult>() {
        };
        addRequest(requestUrl, new TypeToken<SearchFriendResult>() {
        }, responseListener, responseListener);
        return responseListener;
    }

}
