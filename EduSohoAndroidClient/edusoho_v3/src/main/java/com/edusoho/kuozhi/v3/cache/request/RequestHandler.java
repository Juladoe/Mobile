package com.edusoho.kuozhi.v3.cache.request;


import com.edusoho.kuozhi.v3.cache.request.model.Request;
import com.edusoho.kuozhi.v3.cache.request.model.Response;

/**
 * Created by howzhi on 15/4/28.
 */
public interface RequestHandler {

    void handler(Request request, Response response);
}
