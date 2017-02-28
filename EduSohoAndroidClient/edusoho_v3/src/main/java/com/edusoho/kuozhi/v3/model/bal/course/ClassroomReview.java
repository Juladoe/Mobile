package com.edusoho.kuozhi.v3.model.bal.course;

import com.edusoho.kuozhi.v3.model.bal.User;

import java.io.Serializable;

/**
 * Created by Zhang on 2016/12/18.
 */

public class ClassroomReview implements Serializable {
    private String id;
    private String classroomId;
    private String title;
    private String content;
    private String rating;
    private String createdTime;
    public String parentId;
    private User user;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClassroomId() {
        return classroomId;
    }

    public void setClassroomId(String classroomId) {
        this.classroomId = classroomId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
