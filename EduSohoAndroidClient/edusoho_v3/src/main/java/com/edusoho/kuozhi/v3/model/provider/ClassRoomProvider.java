package com.edusoho.kuozhi.v3.model.provider;

import android.content.Context;

import com.edusoho.kuozhi.v3.model.bal.Classroom;
import com.edusoho.kuozhi.v3.model.bal.course.Course;
import com.edusoho.kuozhi.v3.model.sys.RequestUrl;
import com.google.gson.reflect.TypeToken;

import java.util.List;

/**
 * Created by 菊 on 2016/5/19.
 */
public class ClassRoomProvider extends ModelProvider  {

    public ClassRoomProvider(Context context) {
        super(context);
    }

    public ProviderListener<Classroom> getClassRoom(int classRoomId) {
        RequestUrl requestUrl = new RequestUrl(String.format("%s/api/classrooms/%d", getHost(), classRoomId));
        requestUrl.getHeads().put("Auth-Token", getToken());
        requestUrl.setParams(new String[] {
                "classRoomId", String.valueOf(classRoomId)
        });
        RequestOption requestOption = buildSimpleGetRequest(
                requestUrl, new TypeToken<Classroom>(){});

        return requestOption.build();
    }

    public ProviderListener<List<Course>> getCourseList(int classRoomId) {
        RequestUrl requestUrl = new RequestUrl(
                String.format("%s/mapi_v2/ClassRoom/getClassRoomCourses?classRoomId=%d", getHost(), classRoomId));
        requestUrl.getHeads().put("token", getToken());
        RequestOption requestOption = buildSimpleGetRequest(
                requestUrl, new TypeToken<List<Course>>(){});

        return requestOption.build();
    }
}
