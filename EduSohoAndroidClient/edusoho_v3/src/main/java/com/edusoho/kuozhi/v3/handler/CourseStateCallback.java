package com.edusoho.kuozhi.v3.handler;

/**
 * Created by suju on 17/1/18.
 */

public interface CourseStateCallback {

    boolean isExpired();

    void handlerCourseExpired();
}
