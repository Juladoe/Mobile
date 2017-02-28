package com.edusoho.kuozhi.v3.model.provider;

import android.content.Context;

import com.edusoho.kuozhi.v3.model.bal.Classroom;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.google.gson.reflect.TypeToken;

/**
 * Created by Zhang on 2016/11/28.
 */
public class NetSchoolProvider extends ModelProvider  {

    public NetSchoolProvider(Context context) {
        super(context);
    }

    public ProviderListener<Object> getNetSchool(String netSchoolName) {
        RequestUrl requestUrl = new RequestUrl(String.format("%s/api/classrooms/%d", getHost(),""));
        requestUrl.getHeads().put("Auth-Token", getToken());
        /**
         * todo填充
         */
        requestUrl.setParams(new String[] {
                "", netSchoolName
        });
        RequestOption requestOption = buildSimpleGetRequest(
                requestUrl, new TypeToken<Object>(){});

        return requestOption.build();
    }
}
