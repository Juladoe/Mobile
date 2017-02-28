package com.edusoho.kuozhi.v3.model.bal.courseDynamics;

/**
 * Created by melomelon on 16/2/19.
 */
public class DynamicsThread {
    private String id;
    private String courseId;
    private String lessonId;
    private String userId;
    private String type;
    private String isStick;
    private String isElite;
    private String isClosed;
    private String title;
    private String content;
    private String postNum;
    private String hitNum;
    private String followNum;
    private String latestPostUserId;
    private String latestPostTime;
    private String createdTime;
    private String updatedTime;

    public String getId() {
        return id;
    }

    public String getCourseId() {
        return courseId;
    }

    public String getLessonId() {
        return lessonId;
    }

    public String getUserId() {
        return userId;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getCreatedTime() {
        return createdTime;
    }
}
