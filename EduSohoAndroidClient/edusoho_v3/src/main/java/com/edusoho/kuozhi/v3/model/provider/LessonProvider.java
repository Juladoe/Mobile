package com.edusoho.kuozhi.v3.model.provider;

import android.content.Context;

import com.edusoho.kuozhi.v3.entity.lesson.CourseCatalogue;
import com.edusoho.kuozhi.v3.entity.lesson.LessonItem;
import com.edusoho.kuozhi.v3.entity.lesson.LessonStatus;
import com.edusoho.kuozhi.v3.model.bal.LearnStatus;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.edusoho.kuozhi.v3.model.sys.School;
import com.edusoho.kuozhi.v3.util.ApiTokenUtil;
import com.edusoho.kuozhi.v3.util.Const;
import com.edusoho.kuozhi.v3.util.SchoolUtil;
import com.google.gson.reflect.TypeToken;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by su on 2015/12/22.
 */
public class LessonProvider extends ModelProvider {

    public LessonProvider(Context context) {
        super(context);
    }

    public ProviderListener<LessonItem> getLesson(int lessonId) {
        School school = SchoolUtil.getDefaultSchool(mContext);
        Map<String, ?> tokenMap = ApiTokenUtil.getToken(mContext);
        String token = tokenMap.get("token").toString();

        RequestUrl requestUrl = new RequestUrl(school.host + String.format(Const.LESSON, lessonId));
        requestUrl.heads.put("Auth-Token", token);

        RequestOption requestOption = buildSimpleGetRequest(
                requestUrl, new TypeToken<LessonItem>(){});

        return requestOption.build();
    }

    public ProviderListener<LinkedHashMap> getLiveRoom(String roomUrl) {
        Map<String, ?> tokenMap = ApiTokenUtil.getToken(mContext);
        String token = tokenMap.get("token").toString();

        RequestUrl requestUrl = new RequestUrl(roomUrl + "&debug=1");
        requestUrl.heads.put("Auth-Token", token);

        RequestOption requestOption = buildSimpleGetRequest(
                requestUrl, new TypeToken<LinkedHashMap>(){});

        return requestOption.build();
    }

    public ProviderListener<LessonStatus> getLearnState(int lessonId, int courseId) {
        School school = SchoolUtil.getDefaultSchool(mContext);
        Map<String, ?> tokenMap = ApiTokenUtil.getToken(mContext);
        String token = tokenMap.get("token").toString();

        String url = String.format("%s/%s?lessonId=%d&courseId=%d", school.url, Const.LESSON_STATUS, lessonId, courseId);
        RequestUrl requestUrl = new RequestUrl(url);
        requestUrl.heads.put("token", token);

        RequestOption requestOption = buildSimpleGetRequest(
                requestUrl, new TypeToken<LessonStatus>(){});

        return requestOption.build();
    }

    public ProviderListener<LearnStatus> startLearnLesson(int lessonId, int courseId) {
        School school = SchoolUtil.getDefaultSchool(mContext);
        Map<String, ?> tokenMap = ApiTokenUtil.getToken(mContext);
        String token = tokenMap.get("token").toString();

        String url = String.format("%s/%s?lessonId=%d&courseId=%d", school.url, Const.LEARN_LESSON, lessonId, courseId);
        RequestUrl requestUrl = new RequestUrl(url);
        requestUrl.heads.put("token", token);

        RequestOption requestOption = buildSimplePostRequest(
                requestUrl, new TypeToken<LearnStatus>(){});

        return requestOption.build();
    }

    public ProviderListener<LearnStatus> cancelLearnLesson(int lessonId, int courseId) {
        School school = SchoolUtil.getDefaultSchool(mContext);
        Map<String, ?> tokenMap = ApiTokenUtil.getToken(mContext);
        String token = tokenMap.get("token").toString();

        String url = String.format("%s/%s?lessonId=%d&courseId=%d", school.url, Const.UN_LEARN_COURSE, lessonId, courseId);
        RequestUrl requestUrl = new RequestUrl(url);
        requestUrl.heads.put("token", token);

        RequestOption requestOption = buildSimplePostRequest(
                requestUrl, new TypeToken<LearnStatus>(){});

        return requestOption.build();
    }

    public ProviderListener<Map<String, String>> getCourseLessonLearnStatus(int courseId) {
        School school = SchoolUtil.getDefaultSchool(mContext);
        String token = ApiTokenUtil.getTokenString(mContext);

        String url = String.format("%s/%s?courseId=%d", school.url, Const.LEARN_STATUS, courseId);
        RequestUrl requestUrl = new RequestUrl(url);
        requestUrl.heads.put("token", token);

        RequestOption requestOption = buildSimpleGetRequest(
                requestUrl, new TypeToken<Map<String, String>>(){});

        return requestOption.build();
    }

    public ProviderListener<CourseCatalogue> getCourseLessons(int courseId) {
        School school = SchoolUtil.getDefaultSchool(mContext);
        Map<String, ?> tokenMap = ApiTokenUtil.getToken(mContext);
        String token = tokenMap.get("token").toString();

        RequestUrl requestUrl = new RequestUrl(String.format("%s%s?courseId=%d", school.host, Const.LESSON_CATALOG, courseId));
        requestUrl.heads.put("token", token);

        RequestOption requestOption = buildSimpleGetRequest(
                requestUrl, new TypeToken<CourseCatalogue>(){});

        return requestOption.build();
    }
}
