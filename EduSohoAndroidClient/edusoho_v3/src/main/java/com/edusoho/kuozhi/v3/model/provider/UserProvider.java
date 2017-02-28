package com.edusoho.kuozhi.v3.model.provider;

import android.content.Context;
import com.edusoho.kuozhi.v3.model.bal.User;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.model.sys.School;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.SchoolUtil;
import com.edusoho.kuozhi.v3.util.volley.BaseVolleyRequest;
import com.google.gson.reflect.TypeToken;
import java.util.LinkedHashMap;

/**
 * Created by 菊 on 2016/4/23.
 */
public class UserProvider extends ModelProvider {

    public UserProvider(Context context)
    {
        super(context);
    }

    private String coverIdsToString(int[] userIds) {
        int index = 0;
        StringBuffer stringBuffer = new StringBuffer();
        for (int userId : userIds) {
            index ++;
            stringBuffer.append(userId).append(",");
        }
        if (index > 0) {
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        }

        return stringBuffer.toString();
    }

    public ProviderListener createConvNo(int[] userIds) {
        RequestUrl requestUrl = new RequestUrl(getHost() + "/api/im/conversations");
        requestUrl.getHeads().put("Auth-Token", getToken());
        requestUrl.setParams(new String[] {
                "memberIds", coverIdsToString(userIds)
        });
        RequestOption requestOption = buildSimplePostRequest(
                requestUrl, new TypeToken<LinkedHashMap>(){});

        requestOption.getRequest().setCacheUseMode(BaseVolleyRequest.ALWAYS_USE_CACHE);
        return requestOption.build();
    }

    public ProviderListener<User> getUserInfo(int userId) {
        School school = SchoolUtil.getDefaultSchool(mContext);
        RequestUrl requestUrl = new RequestUrl(String.format("%s/%s", school.url, Const.USERINFO));
        requestUrl.setParams(new String[] {
                "userId", String.valueOf(userId)
        });
        RequestOption requestOption = buildSimplePostRequest(
                requestUrl, new TypeToken<User>(){});

        requestOption.getRequest().setCacheUseMode(BaseVolleyRequest.ALWAYS_USE_CACHE);
        return requestOption.build();
    }
}
