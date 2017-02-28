package com.edusoho.kuozhi.v3.model.provider;

import android.content.Context;

import com.edusoho.kuozhi.v3.entity.CourseSetting;
import com.edusoho.kuozhi.v3.model.bal.course.CourseDetailsResult;
import com.edusoho.kuozhi.v3.model.bal.course.CourseMember;
import com.edusoho.kuozhi.v3.model.bal.course.CourseResult;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.model.sys.School;
import com.edusoho.kuozhi.v3.util.ApiTokenUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.SchoolUtil;
import com.edusoho.kuozhi.v3.util.volley.BaseVolleyRequest;
import com.google.gson.reflect.TypeToken;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by su on 2015/12/22.
 */
public class CourseProvider extends ModelProvider {

    public CourseProvider(Context context) {
        super(context);
    }

    public ProviderListener<CourseResult> getLearnCourses() {
        School school = SchoolUtil.getDefaultSchool(mContext);
        Map<String, ?> tokenMap = ApiTokenUtil.getToken(mContext);
        String token = tokenMap.get("token").toString();

        RequestUrl requestUrl = null;
        requestUrl = new RequestUrl(String.format("%s%srelation=learn&limit=1000", school.host, Const.MY_COURSES));
        requestUrl.heads.put("X-Auth-Token", token);

        RequestOption requestOption = buildSimpleGetRequest(
                requestUrl, new TypeToken<CourseResult>() {
                });

        return requestOption.build();
    }

    public ProviderListener createThread(
            int targetId, int lessonId, String targetType, String threadType, String type, String title, String content) {
        School school = SchoolUtil.getDefaultSchool(mContext);
        Map<String, ?> tokenMap = ApiTokenUtil.getToken(mContext);
        String token = tokenMap.get("token").toString();
        RequestUrl requestUrl = new RequestUrl(school.host + Const.CREATE_THREAD);
        requestUrl.heads.put("X-Auth-Token", token);
        requestUrl.setParams(new String[]{
                "threadType", threadType,
                "type", type,
                "title", title,
                "content", content
        });

        if ("course".equals(threadType)) {
            requestUrl.getParams().put("courseId", String.valueOf(targetId));
            if (lessonId != 0) {
                requestUrl.getParams().put("lessonId", String.valueOf(lessonId));
            }
        } else if ("common".equals(threadType)) {
            requestUrl.getParams().put("targetId", String.valueOf(targetId));
            requestUrl.getParams().put("targetType", targetType);
        }
        RequestOption requestOption = buildSimplePostRequest(
                requestUrl, new TypeToken<LinkedHashMap>() {
                });

        return requestOption.build();
    }

    public ProviderListener<CourseDetailsResult> getCourse(RequestUrl requestUrl) {
        RequestOption requestOption = buildSimplePostRequest(
                requestUrl, new TypeToken<CourseDetailsResult>() {
                });

        requestOption.getRequest().setCacheUseMode(BaseVolleyRequest.ALWAYS_USE_CACHE);
        return requestOption.build();
    }

    public ProviderListener<CourseDetailsResult> getCourse(int courseId) {
        School school = SchoolUtil.getDefaultSchool(mContext);
        Map<String, ?> tokenMap = ApiTokenUtil.getToken(mContext);
        String token = tokenMap.get("token").toString();

        RequestUrl requestUrl = null;
        requestUrl = new RequestUrl(school.url + String.format("/" +
                "%s?courseId=%d", Const.COURSE, courseId));
        requestUrl.heads.put("token", token);

        return getCourse(requestUrl);
    }

    public ProviderListener<LinkedHashMap> getMembership(int courseId, int userId) {
        School school = SchoolUtil.getDefaultSchool(mContext);
        String token = ApiTokenUtil.getTokenString(mContext);

        RequestUrl requestUrl = null;
        requestUrl = new RequestUrl(school.host + String.format(Const.ROLE_IN_COURSE, courseId, userId));
        requestUrl.heads.put("X-Auth-Token", token);

        RequestOption requestOption = buildSimpleGetRequest(
                requestUrl, new TypeToken<LinkedHashMap>() {
                });

        return requestOption.build();
    }

    public ProviderListener<String> unLearn(int courseId) {
        School school = SchoolUtil.getDefaultSchool(mContext);
        String token = ApiTokenUtil.getTokenString(mContext);

        RequestUrl requestUrl = null;
        requestUrl = new RequestUrl(String.format("%s/%s", school.url, Const.UN_LEARN_COURSE));
        requestUrl.heads.put("token", token);
        requestUrl.setParams(new String[]{
                "courseId", String.valueOf(courseId)
        });

        RequestOption requestOption = buildSimplePostRequest(
                requestUrl, new TypeToken<String>() {
                });

        return requestOption.build();
    }

    public ProviderListener<CourseMember> getMember(int courseId) {
        School school = SchoolUtil.getDefaultSchool(mContext);
        String token = ApiTokenUtil.getTokenString(mContext);

        RequestUrl requestUrl = null;
        requestUrl = new RequestUrl(String.format("%s/%s", school.url, Const.COURSE_MEMBER));
        requestUrl.heads.put("token", token);
        requestUrl.setGetParams(new String[]{
                "courseId", String.valueOf(courseId)
        });

        RequestOption requestOption = buildSimpleGetRequest(
                requestUrl, new TypeToken<CourseMember>() {
                });

        return requestOption.build();
    }

    public ProviderListener<CourseSetting> getSetting() {
        School school = SchoolUtil.getDefaultSchool(mContext);
        String token = ApiTokenUtil.getTokenString(mContext);

        RequestUrl requestUrl = new RequestUrl(String.format("%s/api/setting/course", school.host));
        requestUrl.heads.put("Auth-Token", token);

        RequestOption requestOption = buildSimpleGetRequest(
                requestUrl, new TypeToken<CourseSetting>() {
                });

        return requestOption.build();
    }
}
