package com.edusoho.kuozhi.v3.model.provider;

import android.content.Context;

import com.edusoho.kuozhi.v3.model.bal.article.ArticleList;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.util.volley.BaseVolleyRequest;
import com.google.gson.reflect.TypeToken;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by howzhi on 15/9/9.
 */
public class ArticleProvider extends ModelProvider {

    public ArticleProvider(Context context) {
        super(context);
    }

    public ProviderListener getMenus(RequestUrl requestUrl) {
        RequestOption requestOption = buildSimpleGetRequest(
                requestUrl, new TypeToken<List<LinkedHashMap>>(){});

        requestOption.getRequest().setCacheUseMode(BaseVolleyRequest.ALWAYS_USE_CACHE);
        return requestOption.build();
    }

    public ProviderListener getArticles(RequestUrl requestUrl) {
        return buildSimpleGetRequest(requestUrl, new TypeToken<ArticleList>(){}).build();
    }
}
