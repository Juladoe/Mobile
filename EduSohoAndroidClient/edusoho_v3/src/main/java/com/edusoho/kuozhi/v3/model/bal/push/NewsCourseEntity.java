package com.edusoho.kuozhi.v3.model.bal.push;

import com.edusoho.kuozhi.v3.EdusohoApp;

/**
 * Created by JesseHuang on 15/9/16.
 */
public class NewsCourseEntity {
    private int id;
    private int courseId;
    private int objectId;
    private String title;
    private String content;
    private String fromType;
    private String bodyType;
    private String lessonType;
    private int userId;
    private int createdTime;
    private int lessonId;
    private int homworkResultId;
    private int questionId;
    private int threadId;
    private boolean isLessonfinished;
    private int learnStartTime;
    private int learnFinishTime;

    private String image;
    private String teacher;

    public NewsCourseEntity() {

    }

    public NewsCourseEntity(OffLineMsgEntity offlineMsgModel) {
        V2CustomContent v2CustomContent = offlineMsgModel.getCustom();
        this.id = v2CustomContent.getMsgId();
        this.courseId = v2CustomContent.getFrom().getId();
        this.objectId = v2CustomContent.getBody().getId();
        this.title = offlineMsgModel.getTitle();
        this.content = offlineMsgModel.getContent();
        this.fromType = v2CustomContent.getFrom().getType();
        this.bodyType = v2CustomContent.getBody().getType();
        this.lessonType = v2CustomContent.getBody().getLessonType();
        this.userId = EdusohoApp.app.loginUser.id;
        this.createdTime = v2CustomContent.getCreatedTime();
        this.lessonId = v2CustomContent.getBody().getLessonId();
        this.homworkResultId = v2CustomContent.getBody().getHomeworkResultId();
        this.threadId = v2CustomContent.getBody().getThreadId();
        this.isLessonfinished = v2CustomContent.getBody().getIsLessonFinished();
        this.learnStartTime = v2CustomContent.getBody().getLearnStartTime();
        this.learnFinishTime = v2CustomContent.getBody().getLearnFinishTime();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public int getObjectId() {
        return objectId;
    }

    public void setObjectId(int objectId) {
        this.objectId = objectId;
    }

    public String getTitle() {
        return title == null ? "" : title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content == null ? "" : content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFromType() {
        return fromType == null ? "" : fromType;
    }

    public void setFromType(String fromType) {
        this.fromType = fromType;
    }

    public String getBodyType() {
        return bodyType == null ? "" : bodyType;
    }

    public void setBodyType(String bodyType) {
        this.bodyType = bodyType;
    }

    public String getLessonType() {
        return lessonType == null ? "" : lessonType;
    }

    public void setLessonType(String lessonType) {
        this.lessonType = lessonType;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(int createdTime) {
        this.createdTime = createdTime;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public int getLessonId() {
        return lessonId;
    }

    public void setLessonId(int lessonId) {
        this.lessonId = lessonId;
    }

    public int getHomworkResultId() {
        return homworkResultId;
    }

    public void setHomworkResultId(int homworkResultId) {
        this.homworkResultId = homworkResultId;
    }

    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    public boolean getIsLessonfinished() {
        return isLessonfinished;
    }

    public void setIsLessonfinished(boolean isLessonfinished) {
        this.isLessonfinished = isLessonfinished;
    }

    public int getLearnStartTime() {
        return learnStartTime;
    }

    public void setLearnStartTime(int learnStartTime) {
        this.learnStartTime = learnStartTime;
    }

    public int getLearnFinishTime() {
        return learnFinishTime;
    }

    public void setLearnFinishTime(int learnFinishTime) {
        this.learnFinishTime = learnFinishTime;
    }
}
