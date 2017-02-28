package com.edusoho.kuozhi.v3.model.provider;

import android.content.Context;
import com.edusoho.kuozhi.v3.model.bal.push.CourseThreadPostResult;
import com.edusoho.kuozhi.v3.model.bal.thread.CourseThreadEntity;
import com.edusoho.kuozhi.v3.model.bal.thread.PostThreadResult;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.model.sys.School;
import com.edusoho.kuozhi.v3.util.ApiTokenUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.SchoolUtil;
import com.google.gson.reflect.TypeToken;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by 菊 on 2016/4/14.
 */
public class ThreadProvider extends ModelProvider {

    public ThreadProvider(Context context)
    {
        super(context);
    }

    public ProviderListener getCourseThreadInfo(int threadId, int courseId) {
        School school = SchoolUtil.getDefaultSchool(mContext);
        Map<String, ?> tokenMap = ApiTokenUtil.getToken(mContext);
        String token = tokenMap.get("token").toString();

        RequestUrl requestUrl = null;
        RequestOption requestOption = null;
        requestUrl = new RequestUrl(String.format("%s/api/courses/%d/threads/%d", school.host, courseId, threadId));
        requestUrl.heads.put("X-Auth-Token", token);
        requestOption = buildSimpleGetRequest(
                requestUrl, new TypeToken<LinkedHashMap>(){});

        return requestOption.build();
    }

    public ProviderListener getClassRoomThreadInfo(int threadId) {
        School school = SchoolUtil.getDefaultSchool(mContext);
        Map<String, ?> tokenMap = ApiTokenUtil.getToken(mContext);
        String token = tokenMap.get("token").toString();

        RequestUrl requestUrl = null;
        RequestOption requestOption = null;
        requestUrl = new RequestUrl(school.host + String.format(Const.CLASSROOM_THREAD, threadId));
        requestUrl.heads.put("X-Auth-Token", token);
        requestOption = buildSimpleGetRequest(
                requestUrl, new TypeToken<LinkedHashMap>(){});

        return requestOption.build();
    }

    public ProviderListener getThread(int threadId, String type) {
        School school = SchoolUtil.getDefaultSchool(mContext);
        Map<String, ?> tokenMap = ApiTokenUtil.getToken(mContext);
        String token = tokenMap.get("token").toString();

        RequestUrl requestUrl = null;
        RequestOption requestOption = null;
        if ("common".equals(type)) {
            requestUrl = new RequestUrl(school.host + String.format(Const.CLASSROOM_THREAD, threadId));
            requestUrl.heads.put("X-Auth-Token", token);
            requestOption = buildSimpleGetRequest(
                    requestUrl, new TypeToken<CourseThreadEntity>(){});
        } else {
            requestUrl = new RequestUrl(String.format("%s/%s",school.url, Const.GET_THREAD));
            requestUrl.setParams(new String[] {
                    "threadId", String.valueOf(threadId)
            });
            requestUrl.heads.put("token", token);
            requestOption = buildSimplePostRequest(
                    requestUrl, new TypeToken<CourseThreadEntity>(){});
        }

        return requestOption.build();
    }

    public ProviderListener getThreadPost(String targetType, int threadId) {
        School school = SchoolUtil.getDefaultSchool(mContext);
        Map<String, ?> tokenMap = ApiTokenUtil.getToken(mContext);
        String token = tokenMap.get("token").toString();
        RequestUrl requestUrl = new RequestUrl(school.host + String.format(Const.THREAD_POSTS, threadId, targetType));
        requestUrl.heads.put("X-Auth-Token", token);
        requestUrl.setParams(new String[] {
                "threadId", String.valueOf(threadId)
        });

        RequestOption requestOption = buildSimpleGetRequest(
                requestUrl, new TypeToken<CourseThreadPostResult>(){});

        return requestOption.build();
    }

    public ProviderListener createThreadPost(String targetType, int targetId, String threadType, int threadId, String content) {
        School school = SchoolUtil.getDefaultSchool(mContext);
        Map<String, ?> tokenMap = ApiTokenUtil.getToken(mContext);
        String token = tokenMap.get("token").toString();
        RequestUrl requestUrl = new RequestUrl(school.host + Const.POST_THREAD);
        requestUrl.heads.put("X-Auth-Token", token);

        if ("course".equals(targetType)) {
            requestUrl.setParams(new String[] {
                    "threadId", String.valueOf(threadId),
                    "courseId", String.valueOf(targetId),
                    "content", content,
                    "threadType", threadType,
            });
        } else {
            requestUrl.setParams(new String[] {
                    "threadId", String.valueOf(threadId),
                    "parentId", "0",
                    "content", content,
                    "threadType", threadType,
            });
        }

        RequestOption requestOption = buildSimplePostRequest(
                requestUrl, new TypeToken<PostThreadResult>(){});

        return requestOption.build();
    }
}
