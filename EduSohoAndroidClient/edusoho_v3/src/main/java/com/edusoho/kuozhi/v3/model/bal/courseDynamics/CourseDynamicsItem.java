package com.edusoho.kuozhi.v3.model.bal.courseDynamics;

/**
 * Created by melomelon on 16/2/18.
 */
public class CourseDynamicsItem {
    private String id;
    private String userId;
    private String courseId;
    private String classroomId;
    private String type;
    private String objectType;
    private String objectId;
    private String message;
    private Properties properties;
    private String commentNum;
    private String likeNum;
    private String createdTime;

    public String getType() {
        return type;
    }

    public String getObjectId() {
        return objectId;
    }

    public Properties getProperties() {
        return properties;
    }

    public String getCreatedTime() {
        return createdTime;
    }
}
