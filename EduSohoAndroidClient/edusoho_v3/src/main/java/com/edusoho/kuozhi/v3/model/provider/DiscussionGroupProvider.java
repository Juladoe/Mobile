package com.edusoho.kuozhi.v3.model.provider;

import android.content.Context;
import com.edusoho.kuozhi.v3.model.result.DiscussionGroupResult;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.volley.BaseVolleyRequest;
import com.google.gson.reflect.TypeToken;

/**
 * Created by Melomelon on 2015/9/21.
 */
public class DiscussionGroupProvider extends ModelProvider {

    public DiscussionGroupProvider(Context context) {
        super(context);
    }

    public ProviderListener getClassrooms(RequestUrl requestUrl) {
        ProviderListener<DiscussionGroupResult> providerListener = new ProviderListener<DiscussionGroupResult>() {
        };
        addRequest(requestUrl, new TypeToken<DiscussionGroupResult>() {
        }, providerListener, providerListener);
        return providerListener;
    }


    public ProviderListener<DiscussionGroupResult> getGroupList() {
        RequestUrl requestUrl = new RequestUrl(getHost() + Const.DISCUSSION_GROUP);
        requestUrl.getHeads().put("Auth-Token", getToken());

        RequestOption requestOption = buildSimpleGetRequest(
                requestUrl, new TypeToken<DiscussionGroupResult>(){});

        requestOption.getRequest().setCacheUseMode(BaseVolleyRequest.AUTO_USE_CACHE);
        return requestOption.build();
    }
}
